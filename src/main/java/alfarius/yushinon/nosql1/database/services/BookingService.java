package alfarius.yushinon.nosql1.database.services;

import alfarius.yushinon.nosql1.database.repositories.*;
import alfarius.yushinon.nosql1.entity.*;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class BookingService {

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private TimeWindowRepository timeWindowRepository;

    @Autowired
    private BidHandlerRepository bidHandlerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedissonClient redissonClient; // <- тут начинается пиздец с кешами

    @Autowired
    private StringRedisTemplate redisTemplate;

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;

        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else {
            username = principal.toString();
        }

        return userRepository.findByUsername(username);
    }
    @CacheEvict(
            value = {"all_bookings", "user_bookings"},
            allEntries = true
    )
    public void createBooking(Bid inputBid) {
        Long serviceId = inputBid.getServiceId();
        Long timeWindowId = inputBid.getTimeWindowId();
        String lockKey = "lock:classroom:" + serviceId + (timeWindowId != null ? ":slot:" + timeWindowId : "");

        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(2, 5, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new RuntimeException("Этот слот сейчас обрабатывается другим пользователем. Повторите попытку.");
            }

            User currentUser = getCurrentUser();

            Classroom classroom = classroomRepository.findById(serviceId)
                    .orElseThrow(() -> new RuntimeException("Аудитория не найдена"));
            inputBid.setClassroom(classroom);
            inputBid.setName("Заявка на аудиторию " + classroom.getName());

            List<TimeWindow> timeWindows = new ArrayList<>();
            if (timeWindowId != null) {
                TimeWindow timeWindow = timeWindowRepository.findById(timeWindowId)
                        .orElseThrow(() -> new RuntimeException("Временной слот не найден"));
                timeWindows.add(timeWindow);
            } else if (inputBid.getTimeStart() != null && inputBid.getTimeEnd() != null) {
                TimeWindow customWindow = new TimeWindow();
                customWindow.setTimeStart(inputBid.getTimeStart());
                customWindow.setTimeEnd(inputBid.getTimeEnd());
                customWindow = timeWindowRepository.save(customWindow);
                timeWindows.add(customWindow);
            }
            inputBid.setTimeWindows(timeWindows);

            Bid savedBid = bidRepository.save(inputBid);

            BidHandler handler = new BidHandler();
            handler.setBid(savedBid);
            handler.setApplicant(currentUser);

            bidHandlerRepository.save(handler);

            String cartKey = "cart:bid:" + savedBid.getId();
            redisTemplate.opsForValue().set(cartKey, "PENDING", 5, TimeUnit.MINUTES);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Ошибка выполнения блокировки Redis", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = "user_bookings",
            key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()"
    )
    public List<Bid> getBookingsForCurrentUser() {
        User currentUser = getCurrentUser();
        List<BidHandler> handlers = bidHandlerRepository.findByApplicant(currentUser);
        List<Bid> bids = handlers.stream().map(BidHandler::getBid).toList();
        bids.forEach(bid -> bid.setStatus(getBidStatusFromRedis(bid.getId())));
        return bids;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "all_bookings", key = "'all'") // ONLY FOR MADOKA PERSON (админка)
    public List<Bid> getAllBookings() {
        List<Bid> bids = bidRepository.findAll();
        bids.forEach(bid -> bid.setStatus(getBidStatusFromRedis(bid.getId())));
        return bids;
    }

    @CacheEvict(value = {"all_bookings", "user_bookings"}, allEntries = true)
    @Transactional
    public void updateStatus(Long bookingId, String action) {
        String cartKey = "cart:bid:" + bookingId;

        if ("APPROVED".equalsIgnoreCase(action)) {
            redisTemplate.opsForValue().set("status:bid:" + bookingId, "APPROVED");
            redisTemplate.delete(cartKey);
        } else if ("REJECTED".equalsIgnoreCase(action)) {
            redisTemplate.opsForValue().set("status:bid:" + bookingId, "REJECTED");
            redisTemplate.delete(cartKey);
        }
    }

    public String getBidStatusFromRedis(Long bookingId) {
        String permanentStatus = redisTemplate.opsForValue().get("status:bid:" + bookingId);
        if (permanentStatus != null) {
            return permanentStatus;
        }

        Boolean isPendingInCart = redisTemplate.hasKey("cart:bid:" + bookingId);
        if (Boolean.TRUE.equals(isPendingInCart)) {
            return "В обработке (осталось < 5 мин)";
        }

        return "Истёк срок удержания (EXPIRED)";
    }
}