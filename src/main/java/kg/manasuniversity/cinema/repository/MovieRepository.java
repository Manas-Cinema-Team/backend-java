package kg.manasuniversity.cinema.repository;

import kg.manasuniversity.cinema.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findAllByIsActiveTrue();
    Page<Movie> findAllByIsActiveTrue(Pageable pageable); // добавили
    Optional<Movie> findByIdAndIsActiveTrue(Long id);
}