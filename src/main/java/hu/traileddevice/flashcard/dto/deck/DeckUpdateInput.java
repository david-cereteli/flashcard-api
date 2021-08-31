package hu.traileddevice.flashcard.dto.deck;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class DeckUpdateInput extends DeckCreateInput {

    @Schema(description = "Id of the user who owns the deck", example = "1")
    @Min(value = 1, message = "User id must be positive")
    private Long userId;

}
