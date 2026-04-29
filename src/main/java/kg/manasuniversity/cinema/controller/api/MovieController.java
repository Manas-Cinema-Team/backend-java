package kg.manasuniversity.cinema.controller.api;

import kg.manasuniversity.cinema.dto.request.MovieCreateRequest;
import kg.manasuniversity.cinema.dto.response.MovieResponse;
import kg.manasuniversity.cinema.dto.response.PageResponse;
import kg.manasuniversity.cinema.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
public class MovieController {
    private final MovieService movieService;

    @GetMapping
    public ResponseEntity<PageResponse<MovieResponse>> getAfisha(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(name = "page_size", defaultValue = "20") int pageSize,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(movieService.getAfisha(page, pageSize, genre, search));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<MovieResponse>> getAllMoviesForAdmin() {
        return ResponseEntity.ok(movieService.getAllForAdmin());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> createMovie(@RequestBody MovieCreateRequest movieCreateRequest) {
        movieService.createMovie(movieCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieResponse> updateMovie(
            @PathVariable Long id,
            @RequestBody MovieCreateRequest movieCreateRequest) {
        return ResponseEntity.ok(movieService.updateMovie(id, movieCreateRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok(Map.of("message", "Film deactivated successfully"));
    }

}