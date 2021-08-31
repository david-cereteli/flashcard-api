package hu.traileddevice.flashcard.dto.card;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CardCreateInput {

    @Schema(description = "Text on the front of the card", example = "Is HATEOAS required in REST?")
    @Size(min = 1, max = 750, message = "Length of text on the front of the card should be between 1-750 characters")
    @NotNull(message = "Front content must not be null.")
    private String frontContent;

    @Schema(description = "Text on the back of the card", example = "Well, it turns out it is.")
    @Size(min = 1, max = 3000, message = "Length of text on the back of the card should be between 1-3000 characters")
    @NotNull(message = "Back content must not be null.")
    private String backContent;
}
