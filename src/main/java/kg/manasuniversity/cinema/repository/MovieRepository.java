package kg.manasuniversity.cinema.repository;

import kg.manasuniversity.cinema.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {
}
