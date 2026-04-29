package kg.manasuniversity.cinema.mapper;

import kg.manasuniversity.cinema.dto.response.MovieResponse;
import kg.manasuniversity.cinema.entity.Movie;
import org.springframework.stereotype.Component;
import kg.manasuniversity.cinema.dto.response.SessionResponse;

import java.util.List;

@Component
public class MovieMapper {

    public MovieResponse toMovieResponse(Movie movie) {
        if (movie == null) return null;
        return new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getDescription(),
                movie.getGenre(),
                movie.getDuration(),
                movie.getAgeRating(),
                movie.getPosterUrl(),
                movie.getReleaseDate(),
                movie.getIsActive(),
                null // sessions — только для detail запроса
        );
    }

    public MovieResponse toMovieResponseWithSessions(Movie movie, List<SessionResponse> sessions) {
        if (movie == null) return null;
        return new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getDescription(),
                movie.getGenre(),
                movie.getDuration(),
                movie.getAgeRating(),
                movie.getPosterUrl(),
                movie.getReleaseDate(),
                movie.getIsActive(),
                sessions
        );
    }
}