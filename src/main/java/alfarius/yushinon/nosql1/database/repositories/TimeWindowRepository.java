package alfarius.yushinon.nosql1.database.repositories;

import alfarius.yushinon.nosql1.entity.TimeWindow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TimeWindowRepository extends JpaRepository<TimeWindow, Long> {
    default List<TimeWindow> findAvailableTimeWindows(Long classroomId) {
        return findAll();
    }
}