package hu.traileddevice.flashcard.repository;

import hu.traileddevice.flashcard.model.CardTiming;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CardTimingRepository extends JpaRepository<CardTiming, Long> {

    // retrieves first of never learned, failed, or due cards in the specified deck; oldest review first
    // time of day does not matter, due today is retrieved // see ormH2.xml or ormPSQL.xml for specific implementations
    //@Query(value = "SELECT * FROM timings INNER JOIN cards ON cards.id = timings.card_id INNER JOIN decks ON decks.id = cards.deck_id WHERE cards.deck_id = :deckId AND ((last_review_date + (repetition_interval * interval '1 day'))\\:\\:date <= CURRENT_DATE OR repetition_number = 0) ORDER BY last_review_date + (repetition_interval * interval '1 day') LIMIT 1"
    @Query(nativeQuery = true)
    Optional<CardTiming> findMostDueCardInDeck(Long deckId);

    Optional<CardTiming> findByCardId(Long cardId);
}
