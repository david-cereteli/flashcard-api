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
public class AnswerOutputModel extends RepresentationModel<AnswerOutputModel> {

    @Schema(description = "Id of the card", example = "1")
    private Long id;

    @Schema(description = "Id of the deck where the card belongs", example = "1")
    private Long deckId;

    @Schema(description = "Text on the back of the card", example = "Well, it turns out it is.")
    private String backContent;

}
