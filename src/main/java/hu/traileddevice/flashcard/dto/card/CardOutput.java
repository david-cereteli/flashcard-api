package hu.traileddevice.flashcard.dto.card;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CardOutput {

    private Long id;

    private Long deckId;

    private String frontContent;

    private String backContent;

    private LocalDate dueDate;

}
