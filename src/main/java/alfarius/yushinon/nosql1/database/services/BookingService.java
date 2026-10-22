package alfarius.yushinon.nosql1.database.services;

import alfarius.yushinon.nosql1.database.repositories.*;
import alfarius.yushinon.nosql1.entity.*;
import alfarius.yushinon.nosql1.entity.mongo.Actions;
import alfarius.yushinon.nosql1.entity.mongo.CertainAction;
import alfarius.yushinon.nosql1.entity.mongo.UserEmbedded;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service

public class BookingService {

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private LogService logService;

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = (principal instanceof UserDetails userDetails) ? userDetails.getUsername() : principal.toString();
        return userRepository.findByUsername(username);
    }

    public String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (principal instanceof UserDetails userDetails) ? userDetails.getUsername() : principal.toString();
    }

    @CacheEvict(value = {"all_bookings", "user_bookings", "available_slots"}, allEntries = true)
    public void createBooking(Bid inputBid) {
        User currentUser = getCurrentUser();

        UserEmbedded applicantEmb = new UserEmbedded();
        applicantEmb.setId(currentUser.getId());
        applicantEmb.setUsername(currentUser.getUsername());

        inputBid.setApplicant(applicantEmb);
        inputBid.setUpdatedAt(LocalDateTime.now());
        inputBid.setStatus("PENDING");

        Bid savedBid = bidRepository.save(inputBid);

        // TTL на 5 минут
        String cartKey = "cart:bid:" + savedBid.getId();
        redisTemplate.opsForValue().set(cartKey, "PENDING", 5, TimeUnit.MINUTES);

        // ЛОГИРОВАНИЕ 1: Поступление новой заявки
        CertainAction action = new CertainAction();
        action.setUsername(currentUser.getUsername());
        action.setUserRole(currentUser.getRole());
        action.setAction(Actions.BOOKING_CREATED);
        logService.safeCertainLog(action);
    }

    @Cacheable(value = "user_bookings", key = "#root.target.getCurrentUsername()")
    public List<Bid> getBookingsForCurrentUser() {
        User currentUser = getCurrentUser();
        List<Bid> bids = bidRepository.findAll().stream()
                .filter(b -> b.getApplicant() != null && b.getApplicant().getId().equals(currentUser.getId()))
                .toList();
        bids.forEach(bid -> bid.setStatus(getBidStatusFromRedis(bid.getId())));
        return bids;
    }

    @Cacheable(value = "all_bookings", key = "'all'")
    public List<Bid> getAllBookings() {
        List<Bid> bids = bidRepository.findAll();
        bids.forEach(bid -> bid.setStatus(getBidStatusFromRedis(bid.getId())));
        return bids;
    }

    public String getBidStatusFromRedis(String bookingId) {
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
    public void updateBookingStatus(String bookingId, String newStatus) {
        Bid bid = bidRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

        String cartKey = "cart:bid:" + bookingId;
        Actions logAction = null;
        User adminUser = getCurrentUser();
        if ("APPROVED".equalsIgnoreCase(newStatus)) {
            redisTemplate.opsForValue().set("status:bid:" + bookingId, "APPROVED");
            bid.setStatus("APPROVED");
            bid.setUpdatedAt(LocalDateTime.now());
            bidRepository.save(bid);
            redisTemplate.delete(cartKey);
            logAction = Actions.BOOKING_REQUEST_APPROVED;
        } else if ("REJECTED".equalsIgnoreCase(newStatus)) {
            redisTemplate.opsForValue().set("status:bid:" + bookingId, "REJECTED");
            bid.setStatus("REJECTED");
            bid.setUpdatedAt(LocalDateTime.now());
            bidRepository.save(bid);
            redisTemplate.delete(cartKey);
            logAction = Actions.BOOKING_REQUEST_REJECTED;
        }

        // --- ЛОГИРОВАНИЕ ДЕЙСТВИЯ АДМИНА ---
        CertainAction action = new CertainAction();
        action.setUsername(adminUser != null ? adminUser.getUsername() : getCurrentUsername());
        action.setUserRole(adminUser != null ? adminUser.getRole() : null);
        action.setAction(logAction);
        logService.safeCertainLog(action);
    }

    public boolean isSlotAvailable(Long classroomId, Long timeWindowId) {
        String bookedKey = "booked:classroom:" + classroomId + ":slot:" + timeWindowId;
        return !Boolean.TRUE.equals(redisTemplate.hasKey(bookedKey));
    }
}