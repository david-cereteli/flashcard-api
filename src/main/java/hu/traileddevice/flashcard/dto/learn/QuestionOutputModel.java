package hu.traileddevice.flashcard.dto.learn;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionOutputModel extends RepresentationModel<QuestionOutputModel> {

    @Schema(description = "Id of the card", example = "1")
    private Long id;

    @Schema(description = "Id of the deck where the card belongs", example = "1")
    private Long deckId;

    @Schema(description = "Text on the front of the card", example = "Is HATEOAS required in REST?")
    private String frontContent;

}
