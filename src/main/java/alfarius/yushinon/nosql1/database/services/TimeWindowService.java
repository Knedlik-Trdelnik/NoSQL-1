package alfarius.yushinon.nosql1.database.services;

import alfarius.yushinon.nosql1.database.repositories.TimeWindowRepository;
import alfarius.yushinon.nosql1.entity.TimeWindow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TimeWindowService {

    @Autowired
    private TimeWindowRepository timeWindowRepository;

    public List<TimeWindow> getAvailableSlots(Long classroomId) {
        return timeWindowRepository.findAvailableTimeWindows(classroomId);
    }
}