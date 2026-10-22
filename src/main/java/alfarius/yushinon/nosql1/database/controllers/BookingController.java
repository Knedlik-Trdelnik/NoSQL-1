package alfarius.yushinon.nosql1.database.controllers;


import alfarius.yushinon.nosql1.database.repositories.BidRepository;
import alfarius.yushinon.nosql1.database.services.BookingManagementService;
import alfarius.yushinon.nosql1.database.services.BookingService;
import alfarius.yushinon.nosql1.entity.Bid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingManagementService bookingManagementService;

    @Autowired
    private BidRepository bidRepository;

    @PostMapping("/bookings")
    public ResponseEntity<String> createBid(@RequestBody Bid bid) {
        bookingService.createBooking(bid);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/bookings/my")
    public ResponseEntity<List<Bid>> getMyBookings() {
        return ResponseEntity.ok(bookingService.getBookingsForCurrentUser());
    }

    @PutMapping("/bookings/{id}/with-rule")
    public ResponseEntity<Void> updateBidAndRule(
            @PathVariable String id,
            @RequestParam String status,
            @RequestParam String ruleName) {
        bookingManagementService.updateBidAndRule(id, status, ruleName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/bookings/search")
    public ResponseEntity<org.springframework.data.domain.Page<Bid>> searchBids(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String username,
            org.springframework.data.domain.Pageable pageable) {

        org.springframework.data.domain.Page<Bid> result = bidRepository.findByStatusAndApplicantUsername(status, username, pageable);
        return ResponseEntity.ok(result);
    }
}
