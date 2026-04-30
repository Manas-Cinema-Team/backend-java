package kg.manasuniversity.cinema.service;

import kg.manasuniversity.cinema.dto.request.MovieCreateRequest;
import kg.manasuniversity.cinema.dto.response.MovieResponse;
import kg.manasuniversity.cinema.dto.response.PageResponse;
import kg.manasuniversity.cinema.dto.response.SessionResponse;
import kg.manasuniversity.cinema.entity.Movie;
import kg.manasuniversity.cinema.mapper.MovieMapper;
import kg.manasuniversity.cinema.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieMapper movieMapper;

    @Mock
    private SessionService sessionService;

    @InjectMocks
    private MovieService movieService;

    private Movie mockMovie;
    private MovieResponse mockResponse;
    private MovieCreateRequest mockCreateRequest;

    @BeforeEach
    void setUp() {
        mockMovie = new Movie("Inception", "Sci-Fi", 148);
        mockMovie.setId(1L);
        mockMovie.setDescription("A mind-bending thriller");
        mockMovie.setAgeRating("PG-13");
        mockMovie.setPosterUrl("url");
        mockMovie.setReleaseDate(LocalDate.of(2010, 7, 16));
        mockMovie.setIsActive(true);

        mockResponse = new MovieResponse(
            1L, "Inception", "A mind-bending thriller", "Sci-Fi", 148,
            "PG-13", "url", LocalDate.of(2010, 7, 16), true, null
        );

        mockCreateRequest = new MovieCreateRequest(
            "Inception", "A mind-bending thriller", "Sci-Fi", 148,
            "PG-13", "url", LocalDate.of(2010, 7, 16), true
        );
    }

    @Test
    void getAfisha_ShouldReturnPageResponse() {
        // Arrange
        int page = 1;
        int pageSize = 10;
        Pageable expectedPageable = PageRequest.of(page - 1, pageSize);
        Page<Movie> moviePage = new PageImpl<>(List.of(mockMovie), expectedPageable, 1);

        when(movieRepository.findAllByFilters(null, null, expectedPageable)).thenReturn(moviePage);
        when(movieMapper.toMovieResponse(mockMovie)).thenReturn(mockResponse);

        // Act
        PageResponse<MovieResponse> actual = movieService.getAfisha(page, pageSize, "", "   ");

        // Assert
        assertThat(actual).isNotNull();
        assertThat(actual.getCount()).isEqualTo(1);
        assertThat(actual.getResults()).hasSize(1).contains(mockResponse);
        assertThat(actual.getNext()).isNull(); // Because only 1 item, no next page
        assertThat(actual.getPrevious()).isNull();

        verify(movieRepository).findAllByFilters(null, null, expectedPageable);
    }

    @Test
    void getAllForAdmin_ShouldReturnListOfMovies() {
        // Arrange
        when(movieRepository.findAll()).thenReturn(List.of(mockMovie));
        when(movieMapper.toMovieResponse(mockMovie)).thenReturn(mockResponse);

        // Act
        List<MovieResponse> actual = movieService.getAllForAdmin();

        // Assert
        assertThat(actual).hasSize(1).contains(mockResponse);
        verify(movieRepository).findAll();
    }

    @Test
    void getById_WhenMovieExists_ShouldReturnMovieWithSessions() {
        // Arrange
        Long movieId = 1L;
        List<SessionResponse> sessions = Collections.emptyList();

        when(movieRepository.findByIdAndIsActiveTrue(movieId)).thenReturn(Optional.of(mockMovie));
        when(sessionService.getSessionsByMovie(movieId)).thenReturn(sessions);
        when(movieMapper.toMovieResponseWithSessions(mockMovie, sessions)).thenReturn(mockResponse);

        // Act
        MovieResponse actual = movieService.getById(movieId);

        // Assert
        assertThat(actual).isNotNull().isEqualTo(mockResponse);
        verify(movieRepository).findByIdAndIsActiveTrue(movieId);
        verify(sessionService).getSessionsByMovie(movieId);
    }

    @Test
    void getById_WhenMovieDoesNotExist_ShouldThrowException() {
        // Arrange
        Long movieId = 99L;
        when(movieRepository.findByIdAndIsActiveTrue(movieId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> movieService.getById(movieId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Фильм не найден");
    }

    @Test
    void createMovie_ShouldSaveMovieToRepository() {
        // Act
        movieService.createMovie(mockCreateRequest);

        // Assert
        ArgumentCaptor<Movie> movieCaptor = ArgumentCaptor.forClass(Movie.class);
        verify(movieRepository).save(movieCaptor.capture());

        Movie savedMovie = movieCaptor.getValue();
        assertThat(savedMovie.getTitle()).isEqualTo(mockCreateRequest.title());
        assertThat(savedMovie.getGenre()).isEqualTo(mockCreateRequest.genre());
        assertThat(savedMovie.getDuration()).isEqualTo(mockCreateRequest.duration());
        assertThat(savedMovie.getDescription()).isEqualTo(mockCreateRequest.description());
    }

    @Test
    void updateMovie_WhenMovieExists_ShouldUpdateAndSave() {
        // Arrange
        Long movieId = 1L;
        when(movieRepository.findById(movieId)).thenReturn(Optional.of(mockMovie));
        when(movieMapper.toMovieResponse(mockMovie)).thenReturn(mockResponse);

        // Act
        MovieResponse actual = movieService.updateMovie(movieId, mockCreateRequest);

        // Assert
        assertThat(actual).isEqualTo(mockResponse);
        verify(movieRepository).findById(movieId);
        verify(movieRepository).save(mockMovie);

        // Ensure fields were updated on the entity
        assertThat(mockMovie.getTitle()).isEqualTo(mockCreateRequest.title());
    }

    @Test
    void deleteMovie_WhenMovieExists_ShouldPerformSoftDelete() {
        // Arrange
        Long movieId = 1L;
        when(movieRepository.findById(movieId)).thenReturn(Optional.of(mockMovie));

        // Act
        movieService.deleteMovie(movieId);

        // Assert
        assertThat(mockMovie.getIsActive()).isFalse(); // Verify soft delete
        verify(movieRepository).save(mockMovie);
    }

    @Test
    void deleteMovie_WhenMovieDoesNotExist_ShouldThrowException() {
        // Arrange
        Long movieId = 99L;
        when(movieRepository.findById(movieId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> movieService.deleteMovie(movieId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Фильм не найден");

        verify(movieRepository, never()).save(any());
    }
}