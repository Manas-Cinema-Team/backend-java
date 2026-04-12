package kg.manasuniversity.cinema.controller.api;

import java.util.List;

import kg.manasuniversity.cinema.dto.request.MovieCreateRequest;
import kg.manasuniversity.cinema.dto.response.MovieResponse;
import kg.manasuniversity.cinema.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<MovieResponse>> getAllMovies() {
        return ResponseEntity.ok(movieService.getAll());
    }

    @PostMapping
    public ResponseEntity<Void> createMovie(@RequestBody MovieCreateRequest movieCreateRequest) {
        movieService.createMovie(movieCreateRequest);
        return ResponseEntity.ok().build();
    }
}
