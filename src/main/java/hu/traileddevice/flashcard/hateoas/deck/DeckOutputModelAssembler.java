package hu.traileddevice.flashcard.hateoas.deck;

import hu.traileddevice.flashcard.controller.CardController;
import hu.traileddevice.flashcard.controller.DeckController;
import hu.traileddevice.flashcard.dto.card.CardOfDeckOutputModel;
import hu.traileddevice.flashcard.dto.card.CardOutput;
import hu.traileddevice.flashcard.dto.deck.DeckOutput;
import hu.traileddevice.flashcard.dto.deck.DeckOutputModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class DeckOutputModelAssembler extends RepresentationModelAssemblerSupport<DeckOutput, DeckOutputModel> {

    public DeckOutputModelAssembler() {
        super(DeckController.class, DeckOutputModel.class);
    }

    @Override
    public DeckOutputModel toModel(DeckOutput entity) {
        DeckOutputModel deckOutputModel = instantiateModel(entity);

        deckOutputModel.setId(entity.getId());
        deckOutputModel.setName(entity.getName());
        deckOutputModel.setUserId(entity.getUserId());
        deckOutputModel.setCards(toCardOfDeckOutputModel(entity.getCards()));

        deckOutputModel.add(linkTo(methodOn(DeckController.class).findById(entity.getId())).withSelfRel());

        if (entity.getCards().isEmpty()) {
            deckOutputModel.add(
                    linkTo(methodOn(CardController.class).save(entity.getId(), null)).withRel("create-card"));
        }

        return deckOutputModel;
    }

    @Override
    public CollectionModel<DeckOutputModel> toCollectionModel(Iterable<? extends DeckOutput> entities) {
        CollectionModel<DeckOutputModel> deckOutputModels = super.toCollectionModel(entities);

        deckOutputModels.add(linkTo(methodOn(DeckController.class).findAll()).withSelfRel());

        return deckOutputModels;
    }

    private List<CardOfDeckOutputModel> toCardOfDeckOutputModel(List<CardOutput> cards) {

        if (cards.isEmpty())
            return Collections.emptyList();

        return cards.stream()
                .map(cardOutput -> CardOfDeckOutputModel
                                .builder()
                                .id(cardOutput.getId())
                                .frontContent(cardOutput.getFrontContent())
                                .build()
                )
                .collect(Collectors.toList());

    }

}
