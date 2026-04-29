package kg.manasuniversity.cinema.controller.api;

import kg.manasuniversity.cinema.entity.Hall;
import kg.manasuniversity.cinema.service.HallService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/halls")
@RequiredArgsConstructor
public class HallController {

    private final HallService hallService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Hall>> getAllHalls() {
        return ResponseEntity.ok(hallService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Hall> getHallById(@PathVariable Long id) {
        return ResponseEntity.ok(hallService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Hall> createHall(@RequestBody Hall hall) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hallService.save(hall));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Hall> updateHall(@PathVariable Long id, @RequestBody Hall request) {
        Hall hall = hallService.findById(id);
        hall.setName(request.getName());
        hall.setRows(request.getRows());
        hall.setSeatsPerRow(request.getSeatsPerRow());
        hall.setSchemaMetadata(request.getSchemaMetadata());
        return ResponseEntity.ok(hallService.save(hall));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteHall(@PathVariable Long id) {
        hallService.findById(id); // проверяем что зал существует
        // TODO: проверить что нет активных сеансов перед удалением
        return ResponseEntity.ok(Map.of("message", "Hall deleted successfully"));
    }
}