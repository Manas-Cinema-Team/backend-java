package kg.manasuniversity.cinema.service;

import java.util.List;

import jakarta.transaction.Transactional;
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

    public List<MovieResponse> getAfisha() {
        return movieRepository.findAllByIsActiveTrue()
                .stream()
                .map(movieMapper::toMovieResponse)
                .toList();
    }

    public List<MovieResponse> getAllForAdmin() {
        return movieRepository.findAll()
                .stream()
                .map(movieMapper::toMovieResponse)
                .toList();
    }

    public MovieResponse getById(Long id) {
        return movieRepository.findByIdAndIsActiveTrue(id)
                .map(movieMapper::toMovieResponse)
                .orElseThrow(() -> new RuntimeException("Фильм не найден"));
    }

    @Transactional
    public void createMovie(MovieCreateRequest movieCreateRequest) {
        Movie movie = new Movie
                (movieCreateRequest.title(), movieCreateRequest.genre(), movieCreateRequest.duration());

        movie.setDescription(movieCreateRequest.description());
        movie.setAgeRating(movieCreateRequest.ageRating());
        movie.setPosterUrl(movieCreateRequest.posterUrl());
        movie.setReleaseDate(movieCreateRequest.releaseDate());
        movie.setIsActive(movieCreateRequest.isActive());

        movieRepository.save(movie);
    }

    @Transactional
    public void updateMovie(Long id, MovieCreateRequest request) {
        // 1. Ищем существующий фильм по ID
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Фильм не найден c id: " + id));

        // 2. Обновляем поля данными из request
        movie.setTitle(request.title());
        movie.setGenre(request.genre());
        movie.setDuration(request.duration());
        movie.setDescription(request.description());
        movie.setAgeRating(request.ageRating());
        movie.setPosterUrl(request.posterUrl());
        movie.setReleaseDate(request.releaseDate());
        movie.setIsActive(request.isActive());

        // 3. Сохраняем обновленный объект
        movieRepository.save(movie);
    }
    }

