package alfarius.yushinon.nosql1.database.services;

import alfarius.yushinon.nosql1.database.repositories.*;
import alfarius.yushinon.nosql1.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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

    public void createBooking(Bid inputBid) {
        User currentUser = getCurrentUser();

        Classroom classroom = classroomRepository.findById(inputBid.getServiceId())
                .orElseThrow(() -> new RuntimeException("Аудитория не найдена"));
        inputBid.setClassroom(classroom);
        inputBid.setName("Заявка на аудиторию " + classroom.getName());

        List<TimeWindow> timeWindows = new ArrayList<>();
        if (inputBid.getTimeWindowId() != null) {
            TimeWindow timeWindow = timeWindowRepository.findById(inputBid.getTimeWindowId())
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
    }

    @Transactional(readOnly = true)
    public List<Bid> getBookingsForCurrentUser() {
        User currentUser = getCurrentUser();
        List<BidHandler> handlers = bidHandlerRepository.findByApplicant(currentUser);
        return handlers.stream()
                .map(BidHandler::getBid)
                .toList();
    }
    @Transactional(readOnly = true)
    public List<Bid> getAllBookings() {
        return bidRepository.findAll();
    }
}