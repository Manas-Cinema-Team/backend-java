package kg.manasuniversity.cinema.service;

import kg.manasuniversity.cinema.dto.request.BookingRequest;
import kg.manasuniversity.cinema.dto.response.BookingResponse;
import kg.manasuniversity.cinema.entity.Booking;
import kg.manasuniversity.cinema.entity.BookingSeat;
import kg.manasuniversity.cinema.entity.Hall;
import kg.manasuniversity.cinema.entity.Movie;
import kg.manasuniversity.cinema.entity.Role;
import kg.manasuniversity.cinema.entity.SeatHold;
import kg.manasuniversity.cinema.entity.SeatStatus;
import kg.manasuniversity.cinema.entity.Session;
import kg.manasuniversity.cinema.entity.TicketPrice;
import kg.manasuniversity.cinema.entity.User;
import kg.manasuniversity.cinema.exception.SeatHeldException;
import kg.manasuniversity.cinema.repository.BookingRepository;
import kg.manasuniversity.cinema.repository.BookingSeatRepository;
import kg.manasuniversity.cinema.repository.SeatHoldRepository;
import kg.manasuniversity.cinema.repository.SessionRepository;
import kg.manasuniversity.cinema.repository.TicketPriceRepository;
import kg.manasuniversity.cinema.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingSeatRepository bookingSeatRepository;
    @Mock
    private SeatHoldRepository seatHoldRepository;
    @Mock
    private TicketPriceRepository ticketPriceRepository;
    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingService bookingService;

    private User mockUser;
    private Session mockSession;
    private TicketPrice mockTicketPrice;
    private Booking mockBooking;

    private final String userEmail = "test@manas.edu.kg";

    @BeforeEach
    void setUp() {
        mockUser = new User(userEmail, "Test User", Role.USER);
        mockUser.setId(1L);

        Movie mockMovie = new Movie("Inception", "A mind-bending thriller", 148);

        Hall mockHall = new Hall();
        mockHall.setName("VIP Hall");

        mockSession = new Session();
        mockSession.setId(10L);
        mockSession.setMovie(mockMovie);
        mockSession.setHall(mockHall);
        mockSession.setStartDatetime(Instant.now().plus(1, ChronoUnit.DAYS));

        mockTicketPrice = new TicketPrice();
        mockTicketPrice.setAmount(BigDecimal.valueOf(500));

        mockBooking = Booking.builder()
            .id(100L)
            .user(mockUser)
            .session(mockSession)
            .totalAmount(BigDecimal.valueOf(1000))
            .bookingStatus("DRAFT")
            .paymentStatus("PENDING")
            .build();
    }

    @Test
    void createHold_WhenSuccessful_ShouldCreateHoldsAndDraftBooking() {
        // Arrange
        BookingRequest request = new BookingRequest(10L, List.of(
            new BookingRequest.SeatRequest(1, 1),
            new BookingRequest.SeatRequest(1, 2)
        ));

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockUser));
        when(sessionRepository.findByIdAndIsActiveTrue(10L)).thenReturn(Optional.of(mockSession));
        when(seatHoldRepository.countActiveHoldsByUserAndSession(anyLong(), anyLong(), any(Instant.class))).thenReturn(0);
        when(ticketPriceRepository.findBySessionId(10L)).thenReturn(Optional.of(mockTicketPrice));
        when(seatHoldRepository.findBySessionAndSeatRowAndSeatNumber(any(), anyInt(), anyInt())).thenReturn(Optional.empty());
        when(bookingRepository.save(any(Booking.class))).thenReturn(mockBooking);

        // Act
        BookingResponse response = bookingService.createHold(request, userEmail);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.bookingStatus()).isEqualTo("DRAFT");

        // Verify SeatHolds were saved
        ArgumentCaptor<SeatHold> holdCaptor = ArgumentCaptor.forClass(SeatHold.class);
        verify(seatHoldRepository, times(2)).save(holdCaptor.capture());

        List<SeatHold> savedHolds = holdCaptor.getAllValues();
        assertThat(savedHolds).hasSize(2);
        assertThat(savedHolds.getFirst().getSeatRow()).isEqualTo(1);
        assertThat(savedHolds.getFirst().getStatus()).isEqualTo(SeatStatus.HELD);

        // Verify Booking was saved with correct amount (500 * 2 = 1000)
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        assertThat(bookingCaptor.getValue().getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    @Test
    void createHold_WhenUserHasActiveHolds_ShouldThrowException() {
        // Arrange
        BookingRequest request = new BookingRequest(10L, List.of(new BookingRequest.SeatRequest(1, 1)));

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockUser));
        when(sessionRepository.findByIdAndIsActiveTrue(10L)).thenReturn(Optional.of(mockSession));
        when(seatHoldRepository.countActiveHoldsByUserAndSession(anyLong(), anyLong(), any(Instant.class))).thenReturn(1); // Active holds exist

        // Act & Assert
        assertThatThrownBy(() -> bookingService.createHold(request, userEmail))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("У вас уже есть активное бронирование");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createHold_WhenSeatIsAlreadyHeld_ShouldThrowSeatHeldException() {
        // Arrange
        BookingRequest request = new BookingRequest(10L, List.of(new BookingRequest.SeatRequest(1, 1)));

        SeatHold existingHold = new SeatHold();
        existingHold.setExpiresAt(Instant.now().plusSeconds(600)); // Expires in future

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockUser));
        when(sessionRepository.findByIdAndIsActiveTrue(10L)).thenReturn(Optional.of(mockSession));
        when(seatHoldRepository.countActiveHoldsByUserAndSession(anyLong(), anyLong(), any())).thenReturn(0);
        when(ticketPriceRepository.findBySessionId(10L)).thenReturn(Optional.of(mockTicketPrice));
        when(seatHoldRepository.findBySessionAndSeatRowAndSeatNumber(mockSession, 1, 1)).thenReturn(Optional.of(existingHold));

        // Act & Assert
        assertThatThrownBy(() -> bookingService.createHold(request, userEmail))
            .isInstanceOf(SeatHeldException.class);

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void confirmBooking_WhenSuccessful_ShouldUpdateStatusAndConvertHolds() {
        // Arrange
        Long bookingId = 100L;
        SeatHold mockHold = new SeatHold();
        mockHold.setSeatRow(1);
        mockHold.setSeatNumber(1);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(mockBooking));
        when(ticketPriceRepository.findBySessionId(mockSession.getId())).thenReturn(Optional.of(mockTicketPrice));
        when(seatHoldRepository.findAllBySessionIdAndExpiresAtAfter(eq(mockSession.getId()), any(Instant.class)))
            .thenReturn(List.of(mockHold));
        when(bookingRepository.save(mockBooking)).thenReturn(mockBooking);

        // Act
        BookingResponse response = bookingService.confirmBooking(bookingId, userEmail);

        // Assert
        assertThat(mockBooking.getBookingStatus()).isEqualTo("CONFIRMED");
        assertThat(mockBooking.getPaymentStatus()).isEqualTo("PAID");
        assertThat(mockBooking.getConfirmedAt()).isNotNull();

        // Verify hold was converted to BookingSeat and deleted
        ArgumentCaptor<BookingSeat> seatCaptor = ArgumentCaptor.forClass(BookingSeat.class);
        verify(bookingSeatRepository).save(seatCaptor.capture());
        assertThat(seatCaptor.getValue().getSeatRow()).isEqualTo(1);
        assertThat(seatCaptor.getValue().getPriceAtBooking()).isEqualTo(mockTicketPrice.getAmount());

        verify(seatHoldRepository).delete(mockHold);
        verify(bookingRepository).save(mockBooking);
    }

    @Test
    void confirmBooking_WhenUserEmailDoesNotMatch_ShouldThrowForbidden() {
        // Arrange
        Long bookingId = 100L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(mockBooking));

        // Act & Assert
        assertThatThrownBy(() -> bookingService.confirmBooking(bookingId, "wrong@email.com"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("FORBIDDEN");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void cancelBooking_WhenSuccessful_ShouldUpdateStatusToCancelled() {
        // Arrange
        Long bookingId = 100L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(mockBooking));

        // Act
        bookingService.cancelBooking(bookingId, userEmail);

        // Assert
        assertThat(mockBooking.getBookingStatus()).isEqualTo("CANCELLED");
        verify(bookingRepository).save(mockBooking);
    }

    @Test
    void getBooking_WhenUserOwnsBooking_ShouldReturnResponse() {
        // Arrange
        Long bookingId = 100L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(mockBooking));

        // Act
        BookingResponse response = bookingService.getBooking(bookingId, userEmail);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(bookingId);
        assertThat(response.bookingStatus()).isEqualTo("DRAFT");
    }
}