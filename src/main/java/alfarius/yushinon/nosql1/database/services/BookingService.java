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
    @CacheEvict(value = {"all_bookings", "user_bookings", "available_slots"}, allEntries = true)
    public void createBooking(Bid inputBid) {
        Long serviceId = inputBid.getServiceId();
        Long timeWindowId = inputBid.getTimeWindowId();

        String bookedKey = "booked:classroom:" + serviceId + ":slot:" + timeWindowId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(bookedKey))) {
            throw new RuntimeException("Этот временной слот уже успешно забронирован другим пользователем.");
        }

        String lockKey = "lock:classroom:" + serviceId + (timeWindowId != null ? ":slot:" + timeWindowId : "");
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(2, 5, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new RuntimeException("Этот слот сейчас параллельно обрабатывается. Повторите попытку.");
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
            throw new RuntimeException("Ошибка блокировки Redis", e);
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

    @CacheEvict(value = {"all_bookings", "user_bookings", "available_slots"}, allEntries = true)
    @Transactional
    public void updateBookingStatus(Long bookingId, String newStatus) {
        Bid bid = bidRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

        String cartKey = "cart:bid:" + bookingId;

        if ("APPROVED".equalsIgnoreCase(newStatus)) {
            redisTemplate.opsForValue().set("status:bid:" + bookingId, "APPROVED");
            Long classroomId = bid.getServiceId();
            if (classroomId == null && bid.getClassroom() != null) {
                classroomId = bid.getClassroom().getId();
            }
            Long timeWindowId = bid.getTimeWindowId();
            if (timeWindowId == null && bid.getTimeWindows() != null && !bid.getTimeWindows().isEmpty()) {
                timeWindowId = bid.getTimeWindows().get(0).getId();
            }
            if (classroomId != null && timeWindowId != null) {
                String bookedKey = "booked:classroom:" + classroomId + ":slot:" + timeWindowId;
                redisTemplate.opsForValue().set(bookedKey, "TAKEN");
            } else {
                System.err.println("[Redis Lock Error] Не удалось определить classroomId или timeWindowId для bid #" + bookingId);
            }

            redisTemplate.delete(cartKey);

        } else if ("REJECTED".equalsIgnoreCase(newStatus)) {
            redisTemplate.opsForValue().set("status:bid:" + bookingId, "REJECTED");
            redisTemplate.delete(cartKey);
        }
    }

    public boolean isSlotAvailable(Long classroomId, Long timeWindowId) {
        String bookedKey = "booked:classroom:" + classroomId + ":slot:" + timeWindowId;
        return !Boolean.TRUE.equals(redisTemplate.hasKey(bookedKey));
    }
}