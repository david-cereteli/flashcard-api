package hu.traileddevice.flashcard.hateoas.user;

import hu.traileddevice.flashcard.controller.DeckController;
import hu.traileddevice.flashcard.controller.UserController;
import hu.traileddevice.flashcard.dto.deck.DeckOfUserOutputModel;
import hu.traileddevice.flashcard.dto.deck.DeckOutput;
import hu.traileddevice.flashcard.dto.user.UserOutput;
import hu.traileddevice.flashcard.dto.user.UserOutputModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UserOutputModelAssembler extends RepresentationModelAssemblerSupport<UserOutput, UserOutputModel> {

    public UserOutputModelAssembler() {
        super(UserController.class, UserOutputModel.class);
    }

    @Override
    public UserOutputModel toModel(UserOutput entity) {
        UserOutputModel userOutputModel = instantiateModel(entity);

        userOutputModel.setId(entity.getId());
        userOutputModel.setName(entity.getName());
        userOutputModel.setEmail(entity.getEmail());
        userOutputModel.setDecks(toDeckOfUserOutputModel(entity.getDecks()));

        userOutputModel.add(linkTo(methodOn(UserController.class).findById(entity.getId())).withSelfRel());

        if (entity.getDecks().isEmpty()) {
            userOutputModel.add(
                    linkTo(methodOn(DeckController.class).save(entity.getId(), null)).withRel("create-deck"));
        }

        return userOutputModel;
    }

    @Override
    public CollectionModel<UserOutputModel> toCollectionModel(Iterable<? extends UserOutput> entities) {
        CollectionModel<UserOutputModel> userOutputModels = super.toCollectionModel(entities);

        userOutputModels.add(linkTo(methodOn(UserController.class).findAll()).withSelfRel());

        return userOutputModels;
    }

    private List<DeckOfUserOutputModel> toDeckOfUserOutputModel(List<DeckOutput> decks) {

        if (decks.isEmpty())
            return Collections.emptyList();

        return decks.stream()
                .map(deckOutput -> DeckOfUserOutputModel
                        .builder()
                        .id(deckOutput.getId())
                        .name(deckOutput.getName())
                        .build()
                        .add(linkTo(methodOn(DeckController.class).findById(deckOutput.getId())).withSelfRel()))
                .collect(Collectors.toList());

    }
}
