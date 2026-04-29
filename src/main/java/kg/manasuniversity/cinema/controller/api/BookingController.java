package kg.manasuniversity.cinema.controller.api;

import kg.manasuniversity.cinema.dto.request.BookingRequest;
import kg.manasuniversity.cinema.dto.request.SeatHoldRequest;
import kg.manasuniversity.cinema.dto.response.BookingResponse;
import kg.manasuniversity.cinema.dto.response.SeatHoldResponse;
import kg.manasuniversity.cinema.entity.Booking;
import kg.manasuniversity.cinema.entity.BookingSeat;
import kg.manasuniversity.cinema.service.BookingService;
import kg.manasuniversity.cinema.service.SeatHoldService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final SeatHoldService seatHoldService; // Добавляем сервис удержания

    /**
     * ШАГ 1: Удержание мест (Пункт 6.6 ТЗ)
     */
    @PostMapping
    public ResponseEntity<SeatHoldResponse> hold(@RequestBody SeatHoldRequest request) {
        try {
            SeatHoldResponse response = seatHoldService.holdMultipleSeats(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * ШАГ 2: Подтверждение брони (Пункт 6.8 ТЗ)
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<BookingResponse> confirm(@RequestBody BookingRequest request) {
        try {
            Booking booking = bookingService.confirmBooking(
                    request.userId(),
                    request.sessionId(),
                    request.seatHoldIds()
            );

            List<BookingResponse.SeatInfo> seatInfos = booking.getSeats().stream()
                    .map(s -> new BookingResponse.SeatInfo(s.getSeatRow(), s.getSeatNumber(), s.getPriceAtBooking()))
                    .toList();

            BookingResponse response = new BookingResponse(
                    booking.getId(),
                    booking.getSession().getMovie().getTitle(),
                    booking.getSession().getStartDatetime(), // Проверь: d или D в сущности Session
                    booking.getTotalAmount(),
                    booking.getBookingStatus(),
                    booking.getConfirmedAt(),
                    seatInfos
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    // BookingController.java
    @GetMapping("/my-history/{userId}") // В будущем userId будет браться из JWT
    public ResponseEntity<List<Booking>> getUserHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getUserHistory(userId));
    }
}