package hu.traileddevice.flashcard.dto.deck;

import hu.traileddevice.flashcard.dto.card.CardOutput;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DeckOutput {

    private Long id;

    private String name;

    private Long userId;

    private List<CardOutput> cards;

}
