package alfarius.yushinon.nosql1.database.controllers;


import alfarius.yushinon.nosql1.database.services.BookingService;
import alfarius.yushinon.nosql1.entity.Bid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/services")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/bookings")
    public ResponseEntity<String> createBid(@RequestBody Bid bid) {
        bookingService.createBooking(bid);
        return ResponseEntity.ok().build();
    }
}
