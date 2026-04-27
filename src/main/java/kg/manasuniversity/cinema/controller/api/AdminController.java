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
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final BookingService bookingService;
    private final HallService hallService; // Внедряем HallService


    @GetMapping("/sessions/{sessionId}/bookings")
    public ResponseEntity<List<Booking>> getBookingsBySession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(bookingService.getBookingsBySession(sessionId));
    }

    // --- УПРАВЛЕНИЕ ЗАЛАМИ (Пункт 6.9 и 14 ТЗ) ---

    // Получить список всех залов для админки
    @GetMapping("/halls")
    public ResponseEntity<List<Hall>> getAllHalls() {
        // Теперь findAll() в HallService стал активным!
        return ResponseEntity.ok(hallService.findAll());
    }

    // Создать или обновить зал (включая schema_metadata)
    @PostMapping("/halls")
    public ResponseEntity<Hall> saveHall(@RequestBody Hall hall) {
        // Если у тебя в HallService есть метод save
        return ResponseEntity.ok(hallService.save(hall));
    }

    // Получить конкретный зал для редактирования
    @GetMapping("/halls/{id}")
    public ResponseEntity<Hall> getHallById(@PathVariable Long id) {
        // Теперь findById() в HallService стал активным!
        return ResponseEntity.ok(hallService.findById(id));
    }
}