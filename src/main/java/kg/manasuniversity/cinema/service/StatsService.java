package kg.manasuniversity.cinema.service;

import kg.manasuniversity.cinema.dto.response.SessionStatsResponse;
import kg.manasuniversity.cinema.dto.response.StatsResponse;
import kg.manasuniversity.cinema.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final MovieRepository movieRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public StatsResponse getStats() {
        long totalMovies = movieRepository.countByIsActiveTrue();
        long totalSessions = sessionRepository.countByIsActiveTrue();
        long totalUsers = userRepository.count();
        long totalBookings = bookingRepository.count();
        long confirmedBookings = bookingRepository.countByBookingStatus("CONFIRMED");

        BigDecimal totalRevenue = bookingRepository.sumTotalAmountByBookingStatus("CONFIRMED");
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        List<SessionStatsResponse> topSessions = sessionRepository.findAllByIsActiveTrue()
                .stream()
                .map(session -> {
                    long bookings = bookingRepository.countBySessionId(session.getId());
                    long confirmed = bookingRepository.countBySessionIdAndBookingStatus(
                            session.getId(), "CONFIRMED");
                    BigDecimal revenue = bookingRepository.sumTotalAmountBySessionIdAndBookingStatus(
                            session.getId(), "CONFIRMED");
                    if (revenue == null) revenue = BigDecimal.ZERO;

                    return new SessionStatsResponse(
                            session.getId(),
                            session.getMovie().getTitle(),
                            session.getHall().getName(),
                            bookings,
                            confirmed,
                            revenue
                    );
                })
                .toList();

        return new StatsResponse(
                totalMovies,
                totalSessions,
                totalUsers,
                totalBookings,
                confirmedBookings,
                totalRevenue,
                topSessions
        );
    }
}