package kg.manasuniversity.cinema.service;

import kg.manasuniversity.cinema.entity.*;
import kg.manasuniversity.cinema.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final TicketPriceRepository ticketPriceRepository;

    @Transactional
    public Booking confirmBooking(Long userId, Long sessionId, List<Long> seatHoldIds) {
        // 1. Проверяем наличие удержаний (SeatHold)
        List<SeatHold> holds = seatHoldRepository.findAllById(seatHoldIds);
        if (holds.isEmpty()) {
            throw new RuntimeException("Удержание мест не найдено или истекло");
        }

        // 2. Получаем актуальную цену для этого сеанса
        TicketPrice priceInfo = ticketPriceRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Цена для данного сеанса не установлена"));

        BigDecimal pricePerSeat = priceInfo.getAmount();
        BigDecimal totalAmount = pricePerSeat.multiply(new BigDecimal(holds.size()));

        // 3. Создаем основной заказ (Booking)
        Booking booking = Booking.builder()
                .user(holds.get(0).getUser())
                .session(holds.get(0).getSession())
                .totalAmount(totalAmount)
                .bookingStatus("BOOKED")
                .paymentStatus("PAID")
                .confirmedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        // 4. Переносим каждое место в BookingSeat
        List<BookingSeat> bookingSeats = holds.stream().map(hold ->
                BookingSeat.builder()
                        .booking(savedBooking)
                        .seatRow(hold.getSeatRow())
                        .seatNumber(hold.getSeatNumber())
                        .priceAtBooking(pricePerSeat)
                        .build()
        ).collect(Collectors.toList());

        bookingSeatRepository.saveAll(bookingSeats);

        // 5. Удаляем временные удержания
        seatHoldRepository.deleteAll(holds);

        return savedBooking;
    }

    // ТЕПЕРЬ МЕТОДЫ ВНЕ confirmBooking - ОНИ СТАНУТ ЦВЕТНЫМИ В РЕПОЗИТОРИИ
    public List<Booking> getBookingsBySession(Long sessionId) {
        return bookingRepository.findAllBySessionId(sessionId);
    }

    public List<BookingSeat> getOccupiedSeatsBySession(Long sessionId) {
        // Этот вызов сделает метод в BookingSeatRepository цветным
        return bookingSeatRepository.findAllByBookingSessionId(sessionId);
    }

    public List<Booking> getUserHistory(Long userId) {
        return bookingRepository.findAllByUserId(userId);
    }

}