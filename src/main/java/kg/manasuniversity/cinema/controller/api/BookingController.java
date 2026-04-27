package kg.manasuniversity.cinema.controller.api;

import kg.manasuniversity.cinema.dto.request.BookingRequest;
import kg.manasuniversity.cinema.dto.response.BookingResponse;
import kg.manasuniversity.cinema.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createHold(
            @RequestBody BookingRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getSubject();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createHold(request, email));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<BookingResponse> confirm(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getSubject();
        return ResponseEntity.ok(bookingService.confirmBooking(id, email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getSubject();
        bookingService.cancelBooking(id, email);
        return ResponseEntity.ok(Map.of("message", "Cancelled"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getSubject();
        return ResponseEntity.ok(bookingService.getBooking(id, email));
    }
}