package hu.traileddevice.flashcard.repository;

import hu.traileddevice.flashcard.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findAllByDeckId(Long deckId);

    Optional<Card> findByFrontContent(String frontContent);
}
