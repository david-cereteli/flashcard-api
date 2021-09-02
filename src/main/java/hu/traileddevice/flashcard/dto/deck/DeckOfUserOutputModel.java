package hu.traileddevice.flashcard.dto.deck;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Relation(collectionRelation = "decks of user")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeckOfUserOutputModel extends RepresentationModel<DeckOfUserOutputModel> {

    @Schema(description = "Id of the deck", example = "1")
    private Long id;

    @Schema(description = "Name of the deck", example = "Spring")
    private String name;

}
