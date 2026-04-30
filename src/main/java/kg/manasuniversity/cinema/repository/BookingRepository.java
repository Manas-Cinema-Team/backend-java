package kg.manasuniversity.cinema.repository;

import kg.manasuniversity.cinema.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Найти все брони конкретного пользователя (для истории)
    List<Booking> findAllByUserId(Long userId);

    // Найти все брони на конкретный сеанс (чтобы знать занятые места)
    List<Booking> findAllBySessionId(Long sessionId);
    long countByBookingStatus(String status);
    long countBySessionId(Long sessionId);
    long countBySessionIdAndBookingStatus(Long sessionId, String status);

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.session.id = :sessionId " +
            "AND b.bookingStatus = 'DRAFT' ORDER BY b.id DESC")
    List<Booking> findDraftByUserAndSession(@Param("userId") Long userId,
                                            @Param("sessionId") Long sessionId);

    @Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE b.bookingStatus = :status")
    BigDecimal sumTotalAmountByBookingStatus(@Param("status") String status);

    @Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE b.session.id = :sessionId AND b.bookingStatus = :status")
    BigDecimal sumTotalAmountBySessionIdAndBookingStatus(@Param("sessionId") Long sessionId, @Param("status") String status);
}

