package hu.traileddevice.flashcard.dto.user;

import hu.traileddevice.flashcard.dto.deck.DeckOutput;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserOutput {

    private Long id;

    private String name;

    private String email;

    private List<DeckOutput> decks;
}
