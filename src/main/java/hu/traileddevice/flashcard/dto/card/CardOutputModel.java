package hu.traileddevice.flashcard.dto.card;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Relation(collectionRelation = "cards")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardOutputModel extends RepresentationModel<CardOutputModel> {

    @Schema(description = "Id of the card", example = "1")
    private Long id;

    @Schema(description = "Id of the deck where the card belongs", example = "1")
    private Long deckId;

    @Schema(description = "Text on the front of the card", example = "Is HATEOAS required in REST?")
    private String frontContent;

    @Schema(description = "Text on the back of the card", example = "Well, it turns out it is.")
    private String backContent;

    @Schema(description = "Date when the card is next due", example = "2021.08.31")
    private LocalDate dueDate;

}
