package kg.manasuniversity.cinema.repository;

import kg.manasuniversity.cinema.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findAllByIsActiveTrue();
    Page<Movie> findAllByIsActiveTrue(Pageable pageable); // добавили
    Optional<Movie> findByIdAndIsActiveTrue(Long id);
    long countByIsActiveTrue();

    @Query("SELECT m FROM Movie m WHERE m.isActive = true " +
            "AND (:genre IS NULL OR :genre = '' OR m.genre = :genre) " +
            "AND (:search IS NULL OR :search = '' OR LOWER(m.title) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Movie> findAllByFilters(
            @Param("genre") String genre,
            @Param("search") String search,
            Pageable pageable
    );
}