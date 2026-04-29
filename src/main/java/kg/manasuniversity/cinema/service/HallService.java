package kg.manasuniversity.cinema.service;

import kg.manasuniversity.cinema.entity.Hall;
import kg.manasuniversity.cinema.repository.HallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HallService {
    private final HallRepository hallRepository;

    public List<Hall> findAll() {
        return hallRepository.findAll();
    }

    public Hall findById(Long id) {
        // Добавляем .orElseThrow(), чтобы вернуть Hall, а не Optional<Hall>
        return hallRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Зал не найден с id: " + id));
    }

    @Transactional
    public Hall save(Hall hall) {
        return hallRepository.save(hall);
    }
}