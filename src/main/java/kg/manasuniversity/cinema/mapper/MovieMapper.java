package kg.manasuniversity.cinema.mapper;

import kg.manasuniversity.cinema.dto.response.MovieResponse;
import kg.manasuniversity.cinema.entity.Movie;
import org.springframework.stereotype.Component;

@Component
public class MovieMapper {

    public MovieResponse toMovieResponse(Movie movie) {
        return new MovieResponse(
                movie.getId(),
                movie.getName()
        );
    }
}
