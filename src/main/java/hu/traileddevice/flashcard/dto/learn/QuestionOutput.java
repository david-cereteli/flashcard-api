package hu.traileddevice.flashcard.dto.learn;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionOutput {

    private Long id;

    private Long deckId;

    private String frontContent;

}
