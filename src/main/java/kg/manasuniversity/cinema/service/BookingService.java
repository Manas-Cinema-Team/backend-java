package kg.manasuniversity.cinema.service;

import kg.manasuniversity.cinema.dto.request.BookingRequest;
import kg.manasuniversity.cinema.dto.response.BookingResponse;
import kg.manasuniversity.cinema.dto.response.BookingSeatResponse;
import kg.manasuniversity.cinema.entity.*;
import kg.manasuniversity.cinema.exception.ActiveHoldExistsException;
import kg.manasuniversity.cinema.exception.HoldExpiredException;
import kg.manasuniversity.cinema.exception.SeatHeldException;
import kg.manasuniversity.cinema.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
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

        // если у пользователя уже есть активный draft на этот сеанс — отдаём ACTIVE_HOLD_EXISTS
        List<Booking> drafts = bookingRepository.findDraftByUserAndSession(user.getId(), request.sessionId());
        for (Booking existing : drafts) {
            if (existing.getExpiresAt() != null && existing.getExpiresAt().isAfter(Instant.now())) {
                throw new ActiveHoldExistsException(existing.getId());
            }
        }

        TicketPrice ticketPrice = ticketPriceRepository.findBySessionId(request.sessionId())
                .orElseThrow(() -> new RuntimeException("Цена не найдена/ Price not found"));

        // проверяем, что нет конфликтующих holds от других пользователей либо подтверждённых мест
        List<BookingRequest.SeatRequest> conflictSeats = new ArrayList<>();
        Instant now = Instant.now();
        for (BookingRequest.SeatRequest seat : request.seats()) {
            seatHoldRepository.findBySessionAndSeatRowAndSeatNumber(session, seat.row(), seat.number())
                    .ifPresent(h -> {
                        if (h.getExpiresAt().isAfter(now) && !h.getUser().getId().equals(user.getId())) {
                            conflictSeats.add(seat);
                        }
                    });

            boolean alreadyBooked = bookingSeatRepository.findAllConfirmedBySessionId(request.sessionId())
                    .stream()
                    .anyMatch(b -> b.getSeatRow().equals(seat.row())
                            && b.getSeatNumber().equals(seat.number()));
            if (alreadyBooked && !conflictSeats.contains(seat)) {
                conflictSeats.add(seat);
            }
        }

        if (!conflictSeats.isEmpty()) {
            throw new SeatHeldException(conflictSeats);
        }

        // создаём holds
        Instant expiresAt = Instant.now().plusSeconds(10 * 60);
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

        BigDecimal total = ticketPrice.getAmount()
                .multiply(new BigDecimal(request.seats().size()));

        Booking booking = Booking.builder()
                .user(user)
                .session(session)
                .totalAmount(total)
                .bookingStatus("DRAFT")
                .paymentStatus("PENDING")
                .expiresAt(expiresAt)
                .build();

        Booking saved = bookingRepository.save(booking);

        // материализуем seats для ответа (price_at_booking фиксируется уже здесь)
        List<BookingSeat> draftSeats = new ArrayList<>();
        for (BookingRequest.SeatRequest s : request.seats()) {
            BookingSeat seat = new BookingSeat();
            seat.setBooking(saved);
            seat.setSeatRow(s.row());
            seat.setSeatNumber(s.number());
            seat.setPriceAtBooking(ticketPrice.getAmount());
            draftSeats.add(seat);
        }
        bookingSeatRepository.saveAll(draftSeats);
        saved.setSeats(draftSeats);

        return toBookingResponse(saved);
    }

    @Transactional
    public BookingResponse confirmBooking(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронь не найдена/ Reservation not found"));

        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("FORBIDDEN");
        }

        if ("CONFIRMED".equals(booking.getBookingStatus())) {
            return toBookingResponse(booking);
        }

        if (booking.getExpiresAt() == null || booking.getExpiresAt().isBefore(Instant.now())) {
            throw new HoldExpiredException();
        }

        booking.setBookingStatus("CONFIRMED");
        booking.setPaymentStatus("PAID");
        booking.setConfirmedAt(Instant.now());

        // удаляем holds — место подтверждено навсегда
        List<SeatHold> holds = seatHoldRepository
                .findAllBySessionIdAndExpiresAtAfter(booking.getSession().getId(), Instant.now());
        for (SeatHold hold : holds) {
            if (hold.getUser().getId().equals(booking.getUser().getId())) {
                seatHoldRepository.delete(hold);
            }
        }

        Booking confirmed = bookingRepository.save(booking);
        return toBookingResponse(confirmed);
    }

    @Transactional
    public void cancelBooking(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронь не найдена/ Reservation not found"));

        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("FORBIDDEN");
        }

        // освобождаем места: удаляем holds этого пользователя для этого сеанса
        List<SeatHold> holds = seatHoldRepository
                .findAllBySessionIdAndExpiresAtAfter(booking.getSession().getId(), Instant.now());
        for (SeatHold hold : holds) {
            if (hold.getUser().getId().equals(booking.getUser().getId())) {
                seatHoldRepository.delete(hold);
            }
        }

        booking.setBookingStatus("CANCELLED");
        booking.setExpiresAt(null);
        bookingRepository.save(booking);
    }

    public BookingResponse getBooking(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронь не найдена/ Reservation not found"));

        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("FORBIDDEN");
        }

        return toBookingResponse(booking);
    }

    private BookingResponse toBookingResponse(Booking booking) {
        List<BookingSeatResponse> seats = booking.getSeats() != null
                ? booking.getSeats().stream()
                .map(s -> new BookingSeatResponse(
                        s.getSeatRow(), s.getSeatNumber(), "standard", s.getPriceAtBooking()))
                .toList()
                : List.of();

        Instant expiresAt = "DRAFT".equals(booking.getBookingStatus())
                ? booking.getExpiresAt()
                : null;

        return new BookingResponse(
                booking.getId(),
                new BookingResponse.SessionShortResponse(
                        booking.getSession().getId(),
                        new BookingResponse.MovieRef(booking.getSession().getMovie().getTitle()),
                        new BookingResponse.HallRef(booking.getSession().getHall().getName()),
                        booking.getSession().getStartDatetime()
                ),
                seats,
                booking.getTotalAmount(),
                "KGS",
                bookingStatusToContract(booking.getBookingStatus()),
                paymentStatusToContract(booking.getPaymentStatus()),
                expiresAt,
                Instant.now(),
                booking.getConfirmedAt(),
                booking.getCreatedAt()
        );
    }

    private String bookingStatusToContract(String status) {
        if (status == null) return "draft";
        return status.toLowerCase();
    }

    private String paymentStatusToContract(String status) {
        if (status == null) return "pending";
        return status.toLowerCase();
    }

    public List<Booking> getBookingsBySession(Long sessionId) {
        return bookingRepository.findAllBySessionId(sessionId);
    }
}
