package alfarius.yushinon.nosql1.database.services;

import alfarius.yushinon.nosql1.database.repositories.BidHandlerRepository;
import alfarius.yushinon.nosql1.database.repositories.BidRepository;
import alfarius.yushinon.nosql1.database.repositories.ClassroomRepository;
import alfarius.yushinon.nosql1.database.repositories.TimeWindowRepository;
import alfarius.yushinon.nosql1.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

    public void createBooking(Bid inputBid) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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
}