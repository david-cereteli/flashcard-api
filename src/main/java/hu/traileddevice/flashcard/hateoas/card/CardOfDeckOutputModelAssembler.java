package hu.traileddevice.flashcard.hateoas.card;

import hu.traileddevice.flashcard.controller.CardController;
import hu.traileddevice.flashcard.controller.DeckController;
import hu.traileddevice.flashcard.dto.card.CardOfDeckOutputModel;
import hu.traileddevice.flashcard.dto.card.CardOutput;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CardOfDeckOutputModelAssembler extends
        RepresentationModelAssemblerSupport<CardOutput, CardOfDeckOutputModel> {

    public CardOfDeckOutputModelAssembler() {
        super(DeckController.class, CardOfDeckOutputModel.class);
    }

    @Override
    public CardOfDeckOutputModel toModel(CardOutput entity) {
        CardOfDeckOutputModel cardOfDeckOutputModel = instantiateModel(entity);

        cardOfDeckOutputModel.setId(entity.getId());
        cardOfDeckOutputModel.setFrontContent(entity.getFrontContent());

        cardOfDeckOutputModel.add(linkTo(methodOn(CardController.class).findById(entity.getId())).withSelfRel());
        return cardOfDeckOutputModel;
    }

}
