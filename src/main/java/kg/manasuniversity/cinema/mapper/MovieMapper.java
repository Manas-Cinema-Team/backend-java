package kg.manasuniversity.cinema.mapper;

import kg.manasuniversity.cinema.dto.response.MovieResponse;
import kg.manasuniversity.cinema.entity.Movie;
import org.springframework.stereotype.Component;

@Component
public class MovieMapper {

    public MovieResponse toMovieResponse(Movie movie) {
        if (movie == null) { return null; }
        return new MovieResponse(
                movie.getId(),
                movie.getTitle(),         // Исправлено с getName() на getTitle()
                movie.getDescription(),   // Добавлено по ТЗ
                movie.getGenre(),         // Добавлено по ТЗ
                movie.getDuration(),      // Добавлено по ТЗ
                movie.getAgeRating(),     // Добавлено по ТЗ
                movie.getPosterUrl(),     // Добавлено по ТЗ
                movie.getReleaseDate(),   // Добавлено по ТЗ
                movie.getIsActive()


        );
    }
}
