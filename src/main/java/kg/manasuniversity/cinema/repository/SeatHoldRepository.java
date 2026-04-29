package kg.manasuniversity.cinema.repository;

import kg.manasuniversity.cinema.entity.SeatHold;
import kg.manasuniversity.cinema.entity.SeatStatus;
import kg.manasuniversity.cinema.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {

    Optional<SeatHold> findBySessionAndSeatRowAndSeatNumber(Session session, Integer seatRow, Integer seatNumber);

    List<SeatHold> findAllByExpiresAtBeforeAndStatus(Instant dateTime, SeatStatus status);

    List<SeatHold> findAllBySessionIdAndExpiresAtAfter(Long sessionId, Instant now);

    // добавляем — считаем занятые места (held + booked)
    @Query("SELECT COUNT(s) FROM SeatHold s WHERE s.session.id = :sessionId " +
            "AND s.status IN ('HELD', 'BOOKED') AND s.expiresAt > :now")
    int countActiveBySessionId(@Param("sessionId") Long sessionId, @Param("now") Instant now);

    @Query("SELECT COUNT(s) FROM SeatHold s WHERE s.session.id = :sessionId " +
            "AND s.user.id = :userId AND s.expiresAt > :now")
    int countActiveHoldsByUserAndSession(
            @Param("sessionId") Long sessionId,
            @Param("userId") Long userId,
            @Param("now") Instant now
    );

    @Query("SELECT s FROM SeatHold s WHERE s.session.id = :sessionId " +
            "AND s.status = 'HELD' AND s.expiresAt < :now")
    List<SeatHold> findExpiredHoldsBySession(
            @Param("sessionId") Long sessionId,
            @Param("now") Instant now
    );
}
