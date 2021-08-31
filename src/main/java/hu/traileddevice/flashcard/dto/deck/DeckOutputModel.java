package hu.traileddevice.flashcard.dto.deck;

import com.fasterxml.jackson.annotation.JsonInclude;
import hu.traileddevice.flashcard.dto.card.CardOfDeckOutputModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Relation(collectionRelation = "decks")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeckOutputModel extends RepresentationModel<DeckOutputModel> {

    @Schema(description = "Id of the deck", example = "1")
    private Long id;

    @Schema(description = "Name of the deck", example = "Spring")
    private String name;

    @Schema(description = "Id of the user who owns the deck", example = "1")
    private Long userId;

    @Schema(description = "List of cards in the deck")
    private List<CardOfDeckOutputModel> cards;

}
