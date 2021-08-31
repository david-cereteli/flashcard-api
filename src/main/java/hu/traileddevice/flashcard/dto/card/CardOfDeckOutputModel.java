package hu.traileddevice.flashcard.dto.card;

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
public class CardOfDeckOutputModel extends RepresentationModel<CardOfDeckOutputModel> {

    @Schema(description = "Id of the card", example = "1")
    private Long id;

    @Schema(description = "Text on the front of the card", example = "Is HATEOAS required in REST?")
    private String frontContent;
}
