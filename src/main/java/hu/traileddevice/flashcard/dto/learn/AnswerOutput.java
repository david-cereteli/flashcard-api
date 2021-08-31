package hu.traileddevice.flashcard.dto.learn;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerOutput {

    private Long id;

    private Long deckId;

    private String backContent;

}
