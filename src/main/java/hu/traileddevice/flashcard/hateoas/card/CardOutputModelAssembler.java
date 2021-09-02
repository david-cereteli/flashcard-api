package hu.traileddevice.flashcard.hateoas.card;

import hu.traileddevice.flashcard.controller.CardController;
import hu.traileddevice.flashcard.controller.DeckController;
import hu.traileddevice.flashcard.dto.card.CardOutput;
import hu.traileddevice.flashcard.dto.card.CardOutputModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CardOutputModelAssembler extends RepresentationModelAssemblerSupport<CardOutput, CardOutputModel> {

    public CardOutputModelAssembler() {
        super(DeckController.class, CardOutputModel.class);
    }

    @Override
    public CardOutputModel toModel(CardOutput entity) {
        CardOutputModel cardOutputModel = instantiateModel(entity);

        cardOutputModel.setId(entity.getId());
        cardOutputModel.setDeckId(entity.getDeckId());
        cardOutputModel.setFrontContent(entity.getFrontContent());
        cardOutputModel.setBackContent(entity.getBackContent());
        cardOutputModel.setDueDate(entity.getDueDate());

        cardOutputModel.add(linkTo(methodOn(CardController.class).findById(entity.getId())).withSelfRel());

        return cardOutputModel;
    }

    @Override
    public CollectionModel<CardOutputModel> toCollectionModel(Iterable<? extends CardOutput> entities) {
        CollectionModel<CardOutputModel> cardOutputModels = super.toCollectionModel(entities);

        cardOutputModels.add(linkTo(methodOn(CardController.class).findAll()).withSelfRel());

        return cardOutputModels;
    }

}
