package alfarius.yushinon.nosql1.database.controllers;

import alfarius.yushinon.nosql1.database.services.TimeWindowService;
import alfarius.yushinon.nosql1.entity.TimeWindow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/services")
public class TimeWindowController {

    @Autowired
    private TimeWindowService timeWindowService;

    @GetMapping("/{id}/available-slots")
    public ResponseEntity<List<TimeWindow>> getAvailableSlots(
            @PathVariable("id") Long classroomId) {

        List<TimeWindow> slots = timeWindowService.getAvailableSlots(classroomId);
        return ResponseEntity.ok(slots);
    }
}