package kg.manasuniversity.cinema.service;

import java.util.List;

import kg.manasuniversity.cinema.dto.request.MovieCreateRequest;
import kg.manasuniversity.cinema.dto.response.MovieResponse;
import kg.manasuniversity.cinema.entity.Movie;
import kg.manasuniversity.cinema.mapper.MovieMapper;
import kg.manasuniversity.cinema.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;

    public List<MovieResponse> getAll() {
        return movieRepository.findAll()
                .stream()
                .map(movieMapper::toMovieResponse)
                .toList();
    }

    public MovieResponse getById(Long id) {
        return movieRepository.findById(id)
                .map(movieMapper::toMovieResponse)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
    }

    public void createMovie(MovieCreateRequest movieCreateRequest) {
        Movie movie = new Movie(movieCreateRequest.name());
        movieRepository.save(movie);
    }
}
