package hu.traileddevice.flashcard.hateoas.deck;

import hu.traileddevice.flashcard.controller.DeckController;
import hu.traileddevice.flashcard.controller.UserController;
import hu.traileddevice.flashcard.dto.deck.DeckOfUserOutputModel;
import hu.traileddevice.flashcard.dto.deck.DeckOutput;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class DeckOfUserOutputModelAssembler extends
        RepresentationModelAssemblerSupport<DeckOutput, DeckOfUserOutputModel> {

    public DeckOfUserOutputModelAssembler() {
        super(UserController.class, DeckOfUserOutputModel.class);
    }

    @Override
    public DeckOfUserOutputModel toModel(DeckOutput entity) {
        DeckOfUserOutputModel deckOfUserOutputModel = instantiateModel(entity);

        deckOfUserOutputModel.setId(entity.getId());
        deckOfUserOutputModel.setName(entity.getName());

        deckOfUserOutputModel.add(linkTo(methodOn(DeckController.class).findById(entity.getId())).withSelfRel());

        return deckOfUserOutputModel;
    }
}
