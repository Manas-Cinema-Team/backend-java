package kg.manasuniversity.cinema.service;

import kg.manasuniversity.cinema.dto.request.BookingRequest;
import kg.manasuniversity.cinema.dto.response.BookingResponse;
import kg.manasuniversity.cinema.dto.response.BookingSeatResponse;
import kg.manasuniversity.cinema.entity.*;
import kg.manasuniversity.cinema.exception.SeatHeldException;
import kg.manasuniversity.cinema.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final TicketPriceRepository ticketPriceRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Transactional
    public BookingResponse createHold(BookingRequest request, String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден/ User not found"));

        Session session = sessionRepository.findByIdAndIsActiveTrue(request.sessionId())
                .orElseThrow(() -> new RuntimeException("Сеанс не найден/ Session not found"));

        int activeHolds = seatHoldRepository.countActiveHoldsByUserAndSession(
                request.sessionId(), user.getId(), LocalDateTime.now());

        if (activeHolds > 0) {
            throw new RuntimeException("У вас уже есть активное бронирование на этот сеанс/ \n" +
                    "You already have an active booking for this session ");
        }

        TicketPrice ticketPrice = ticketPriceRepository.findBySessionId(request.sessionId())
                .orElseThrow(() -> new RuntimeException("Цена не найдена/ Price not found"));

        // проверяем что все места свободны
        List<BookingRequest.SeatRequest> conflictSeats = new ArrayList<>();
        for (BookingRequest.SeatRequest seat : request.seats()) {
            seatHoldRepository.findBySessionAndSeatRowAndSeatNumber(session, seat.row(), seat.number())
                    .ifPresent(h -> {
                        if (h.getExpiresAt().isAfter(LocalDateTime.now())) {
                            conflictSeats.add(seat);
                        }
                    });
        }

        if (!conflictSeats.isEmpty()) {
            throw new SeatHeldException(conflictSeats);
        }

        // создаём holds
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);
        for (BookingRequest.SeatRequest seat : request.seats()) {
            SeatHold hold = new SeatHold();
            hold.setSession(session);
            hold.setUser(user);
            hold.setSeatRow(seat.row());
            hold.setSeatNumber(seat.number());
            hold.setStatus(SeatStatus.HELD);
            hold.setExpiresAt(expiresAt);
            seatHoldRepository.save(hold);
        }

        // создаём booking со статусом draft
        BigDecimal total = ticketPrice.getAmount()
                .multiply(new BigDecimal(request.seats().size()));

        Booking booking = Booking.builder()
                .user(user)
                .session(session)
                .totalAmount(total)
                .bookingStatus("DRAFT")
                .paymentStatus("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        Booking saved = bookingRepository.save(booking);
        return toBookingResponse(saved, expiresAt);
    }

    @Transactional
    public BookingResponse confirmBooking(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронь не найдена/ Reservation not found"));

        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("FORBIDDEN");
        }

        booking.setBookingStatus("CONFIRMED");
        booking.setPaymentStatus("PAID");
        booking.setConfirmedAt(LocalDateTime.now());

        // переносим holds в BookingSeats
        TicketPrice ticketPrice = ticketPriceRepository.findBySessionId(booking.getSession().getId())
                .orElseThrow(() -> new RuntimeException("Цена не найдена/ Price not found"));

        List<SeatHold> holds = seatHoldRepository
                .findAllBySessionIdAndExpiresAtAfter(booking.getSession().getId(), LocalDateTime.now());

        for (SeatHold hold : holds) {
            BookingSeat seat = new BookingSeat();
            seat.setBooking(booking);
            seat.setSeatRow(hold.getSeatRow());
            seat.setSeatNumber(hold.getSeatNumber());
            seat.setPriceAtBooking(ticketPrice.getAmount());
            bookingSeatRepository.save(seat);
            seatHoldRepository.delete(hold);
        }

        Booking confirmed = bookingRepository.save(booking);
        return toBookingResponse(confirmed, null);
    }

    @Transactional
    public void cancelBooking(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронь не найдена/ Reservation not found"));

        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("FORBIDDEN");
        }

        booking.setBookingStatus("CANCELLED");
        bookingRepository.save(booking);
    }

    public BookingResponse getBooking(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронь не найдена/ Reservation not found"));

        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("FORBIDDEN");
        }

        return toBookingResponse(booking, null);
    }

    private BookingResponse toBookingResponse(Booking booking, LocalDateTime expiresAt) {
        List<BookingSeatResponse> seats = booking.getSeats() != null
                ? booking.getSeats().stream()
                .map(s -> new BookingSeatResponse(
                        s.getSeatRow(), s.getSeatNumber(), "standard", s.getPriceAtBooking()))
                .toList()
                : List.of();

        return new BookingResponse(
                booking.getId(),
                new BookingResponse.SessionShortResponse(
                        booking.getSession().getId(),
                        booking.getSession().getMovie().getTitle(),
                        booking.getSession().getHall().getName(),
                        booking.getSession().getStartDatetime()
                ),
                seats,
                booking.getTotalAmount(),
                "KGS",
                booking.getBookingStatus(),
                booking.getPaymentStatus(),
                expiresAt,
                booking.getConfirmedAt(),
                booking.getCreatedAt()
        );
    }

    public List<Booking> getBookingsBySession(Long sessionId) {
        return bookingRepository.findAllBySessionId(sessionId);
    }
}