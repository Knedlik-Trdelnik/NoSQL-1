package alfarius.yushinon.nosql1.database.controllers;

import alfarius.yushinon.nosql1.database.services.BookingService;
import alfarius.yushinon.nosql1.entity.Bid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class AdminBookingController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("/bookings/all")
    public ResponseEntity<List<Bid>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    // Обновить статус заявки
    @PutMapping("/bookings/{id}/status")
    public ResponseEntity<Void> updateBookingStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        //bookingService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }
}