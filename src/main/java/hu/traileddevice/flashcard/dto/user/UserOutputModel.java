package hu.traileddevice.flashcard.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import hu.traileddevice.flashcard.dto.deck.DeckOfUserOutputModel;
import hu.traileddevice.flashcard.validation.GmailValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Relation(collectionRelation = "users")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserOutputModel extends RepresentationModel<UserOutputModel> {

    @Schema(description = "Id of the user", example = "1")
    private Long id;

    @Schema(description = "Name of the user", example = "John Doe")
    private String name;

    @Schema(description = "The email address of the user", example = "test@gmail.com")
    private String email;

    @Schema(description = "The list of decks owned by the user")
    private List<DeckOfUserOutputModel> decks;
}
