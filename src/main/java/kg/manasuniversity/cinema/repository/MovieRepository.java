package kg.manasuniversity.cinema.repository;

import kg.manasuniversity.cinema.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findAllByIsActiveTrue();
    Optional<Movie> findByIdAndIsActiveTrue(Long id);
}
//sout