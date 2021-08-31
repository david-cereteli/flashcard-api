package hu.traileddevice.flashcard.dto.card;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@Getter
@Setter
public class CardUpdateInput {

    @Schema(description = "Text on the front of the card", example = "Is HATEOAS required in REST?")
    @Size(min = 1, max = 750, message = "Length of text on the front of the card should be between 1-750 characters")
    private String frontContent;

    @Schema(description = "Text on the back of the card", example = "Well, it turns out it is.")
    @Size(min = 1, max = 3000, message = "Length of text on the back of the card should be between 1-3000 characters")
    private String backContent;

    @Schema(description = "Id of the deck where the card belongs", example = "1")
    @Min(value = 1, message = "Deck id must be positive")
    private Long deckId;

}
