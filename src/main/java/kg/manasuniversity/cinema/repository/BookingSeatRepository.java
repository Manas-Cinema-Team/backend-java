package kg.manasuniversity.cinema.repository;

import kg.manasuniversity.cinema.entity.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {
    List<BookingSeat> findAllByBookingSessionId(Long bookingId);

    @Query("SELECT bs FROM BookingSeat bs WHERE bs.booking.session.id = :sessionId " +
            "AND bs.booking.bookingStatus = 'CONFIRMED'")
    List<BookingSeat> findAllConfirmedBySessionId(@Param("sessionId") Long sessionId);
}
