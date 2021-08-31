package hu.traileddevice.flashcard.dto.deck;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeckCreateInput {

    @Schema(description = "Name of the deck", example = "Spring")
    @Size(min = 1, max = 60, message = "Deck name length should be between 1-60 characters")
    private String name;
}
