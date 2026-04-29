package kg.manasuniversity.cinema.service;

import java.util.List;

import kg.manasuniversity.cinema.dto.response.PageResponse;
import kg.manasuniversity.cinema.dto.response.SessionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
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
    private final SessionService sessionService;

    public PageResponse<MovieResponse> getAfisha(int page, int pageSize, String genre, String search) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        // если пустая строка — считаем как null
        String genreParam = (genre != null && !genre.isBlank()) ? genre : null;
        String searchParam = (search != null && !search.isBlank()) ? search : null;

        Page<Movie> moviePage = movieRepository.findAllByFilters(genreParam, searchParam, pageable);

        List<MovieResponse> results = moviePage.getContent()
                .stream()
                .map(movieMapper::toMovieResponse)
                .toList();

        String next = moviePage.hasNext() ? "/api/v1/movies/?page=" + (page + 1) + "&page_size=" + pageSize : null;
        String previous = moviePage.hasPrevious() ? "/api/v1/movies/?page=" + (page - 1) + "&page_size=" + pageSize : null;

        return new PageResponse<>(moviePage.getTotalElements(), next, previous, results);
    }

    public List<MovieResponse> getAllForAdmin() {
        return movieRepository.findAll()
                .stream()
                .map(movieMapper::toMovieResponse)
                .toList();
    }

    public MovieResponse getById(Long id) {
        Movie movie = movieRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Фильм не найден"));

        List<SessionResponse> sessions = sessionService.getSessionsByMovie(id);

        return movieMapper.toMovieResponseWithSessions(movie, sessions);
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
    public MovieResponse updateMovie(Long id, MovieCreateRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Фильм не найден c id: " + id));

        movie.setTitle(request.title());
        movie.setGenre(request.genre());
        movie.setDuration(request.duration());
        movie.setDescription(request.description());
        movie.setAgeRating(request.ageRating());
        movie.setPosterUrl(request.posterUrl());
        movie.setReleaseDate(request.releaseDate());
        movie.setIsActive(request.isActive());

        movieRepository.save(movie);
        return movieMapper.toMovieResponse(movie);
    }

    @Transactional
    public void deleteMovie(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Фильм не найден c id: " + id));

        // мягкое удаление — просто деактивируем
        movie.setIsActive(false);
        movieRepository.save(movie);
    }
}


