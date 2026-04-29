package kg.manasuniversity.cinema.controller.api;

import kg.manasuniversity.cinema.entity.Booking;
import kg.manasuniversity.cinema.entity.Hall;
import kg.manasuniversity.cinema.service.BookingService;
import kg.manasuniversity.cinema.service.HallService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final BookingService bookingService;

    @GetMapping("/sessions/{sessionId}/bookings")
    public ResponseEntity<List<Booking>> getBookingsBySession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(bookingService.getBookingsBySession(sessionId));
    }
}