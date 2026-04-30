package kg.manasuniversity.cinema.service;

import kg.manasuniversity.cinema.entity.Hall;
import kg.manasuniversity.cinema.repository.HallRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HallServiceTest {

    @Mock
    private HallRepository hallRepository;

    @InjectMocks
    private HallService hallService;

    private Hall mockHall;

    @BeforeEach
    void setUp() {
        mockHall = new Hall();
        mockHall.setId(1L);
        mockHall.setName("IMAX");
        mockHall.setRows(10);
        mockHall.setSeatsPerRow(20);
    }

    @Test
    void findAll_ShouldReturnListOfHalls() {
        // Arrange
        when(hallRepository.findAll()).thenReturn(List.of(mockHall));

        // Act
        List<Hall> actualHalls = hallService.findAll();

        // Assert
        assertThat(actualHalls)
            .isNotNull()
            .hasSize(1)
            .contains(mockHall);

        verify(hallRepository, times(1)).findAll();
    }

    @Test
    void findById_WhenHallExists_ShouldReturnHall() {
        // Arrange
        Long hallId = 1L;
        when(hallRepository.findById(hallId)).thenReturn(Optional.of(mockHall));

        // Act
        Hall actualHall = hallService.findById(hallId);

        // Assert
        assertThat(actualHall).isNotNull();
        assertThat(actualHall.getId()).isEqualTo(hallId);
        assertThat(actualHall.getName()).isEqualTo("IMAX");

        verify(hallRepository, times(1)).findById(hallId);
    }

    @Test
    void findById_WhenHallDoesNotExist_ShouldThrowException() {
        // Arrange
        Long hallId = 99L;
        when(hallRepository.findById(hallId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> hallService.findById(hallId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Зал не найден с id: " + hallId);

        verify(hallRepository, times(1)).findById(hallId);
    }

    @Test
    void save_ShouldSaveAndReturnHall() {
        // Arrange
        when(hallRepository.save(any(Hall.class))).thenReturn(mockHall);

        // Act
        Hall actualHall = hallService.save(mockHall);

        // Assert
        assertThat(actualHall).isNotNull();
        assertThat(actualHall.getId()).isEqualTo(mockHall.getId());
        assertThat(actualHall.getName()).isEqualTo(mockHall.getName());

        verify(hallRepository, times(1)).save(mockHall);
    }
}