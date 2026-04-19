package kg.manasuniversity.cinema.controller.api;

import kg.manasuniversity.cinema.entity.BookingSeat;
import kg.manasuniversity.cinema.entity.Hall;
import kg.manasuniversity.cinema.entity.SeatHold;
import kg.manasuniversity.cinema.entity.Session;
import kg.manasuniversity.cinema.service.BookingService;
import kg.manasuniversity.cinema.service.HallService;
import kg.manasuniversity.cinema.service.SeatHoldService;
import kg.manasuniversity.cinema.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class HallController {

    private final BookingService bookingService;
    private final SeatHoldService seatHoldService;
    private final HallService hallService;
    private final SessionService sessionService;

    /**
     * ПУНКТ 14 ТЗ: GET /api/sessions/{id}/seats/
     * Теперь метод возвращает и схему зала, и статусы мест.
     */
    @GetMapping("/{id}/seats")
    public ResponseEntity<Map<String, Object>> getHallStatus(@PathVariable Long id) {
        // Теперь этот вызов работает, так как мы добавили метод findById в SessionService
        Session session = sessionService.findById(id);

        // Теперь getHall() доступен, так как session — это Entity
        Hall hallFromSession = session.getHall();

        // Оживляем HallService
        Hall detailedHall = hallService.findById(hallFromSession.getId());

        List<BookingSeat> occupied = bookingService.getOccupiedSeatsBySession(id);
        List<SeatHold> held = seatHoldService.getActiveHolds(id);

        return ResponseEntity.ok(Map.of(
                "hallName", detailedHall.getName(),
                "schemaMetadata", detailedHall.getSchemaMetadata(),
                "occupiedSeats", occupied,
                "heldSeats", held
        ));
    }
}