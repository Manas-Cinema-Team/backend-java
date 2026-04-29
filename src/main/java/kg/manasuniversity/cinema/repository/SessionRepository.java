package kg.manasuniversity.cinema.repository;

import kg.manasuniversity.cinema.entity.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    @Query("SELECT s FROM Session s WHERE s.hall.id = :hallId AND s.isActive = true " +
            "AND ((s.startDatetime < :end AND s.endDatetime > :start))")
    List<Session> findOverlappingSessions(
            @Param("hallId") Long hallId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    List<Session> findAllByIsActiveTrue();

    List<Session> findAllByMovieIdAndIsActiveTrue(Long movieId);

    Optional<Session> findByIdAndIsActiveTrue(Long id);
    long countByIsActiveTrue();

    @Query("SELECT s FROM Session s WHERE s.isActive = true " +
            "AND (:date IS NULL OR CAST(s.startDatetime AS date) = :date) " +
            "AND (:movieId IS NULL OR s.movie.id = :movieId) " +
            "AND (:hallId IS NULL OR s.hall.id = :hallId)")
    Page<Session> findAllByFilters(
            @Param("date") LocalDate date,
            @Param("movieId") Long movieId,
            @Param("hallId") Long hallId,
            Pageable pageable
    );
}