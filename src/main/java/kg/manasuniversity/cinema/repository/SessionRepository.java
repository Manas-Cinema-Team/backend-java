package kg.manasuniversity.cinema.repository;

import kg.manasuniversity.cinema.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    @Query("SELECT s FROM Session s WHERE s.hall.id = :hallId AND s.isActive = true " +
            "AND ((s.startDatetime < :end AND s.endDatetime > :start))")
    List<Session> findOverlappingSessions(
            @Param("hallId") Long hallId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}