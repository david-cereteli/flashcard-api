package hu.traileddevice.flashcard.repository;

import hu.traileddevice.flashcard.model.Deck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeckRepository extends JpaRepository<Deck, Long> {
    List<Deck> findAllByUserId(Long userId);
}
