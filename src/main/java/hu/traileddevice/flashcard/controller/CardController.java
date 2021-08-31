package hu.traileddevice.flashcard.controller;

import hu.traileddevice.flashcard.dto.card.CardCreateInput;
import hu.traileddevice.flashcard.dto.card.CardOfDeckOutputModel;
import hu.traileddevice.flashcard.dto.card.CardOutputModel;
import hu.traileddevice.flashcard.dto.card.CardUpdateInput;
import hu.traileddevice.flashcard.hateoas.card.CardOfDeckOutputModelAssembler;
import hu.traileddevice.flashcard.hateoas.card.CardOutputModelAssembler;
import hu.traileddevice.flashcard.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/card")
@Tag(name = "Card CRUD", description = "Create, read, update, and delete cards.")
@Validated
public class CardController {

    private final CardService cardService;
    private final CardOutputModelAssembler cardOutputModelAssembler;
    private final CardOfDeckOutputModelAssembler cardOfDeckOutputModelAssembler;

    public CardController(CardService cardService, CardOutputModelAssembler cardOutputModelAssembler,
                          CardOfDeckOutputModelAssembler cardOfDeckOutputModelAssembler) {
        this.cardService = cardService;
        this.cardOutputModelAssembler = cardOutputModelAssembler;
        this.cardOfDeckOutputModelAssembler = cardOfDeckOutputModelAssembler;
    }

    @GetMapping
    @Operation(summary = "Get all cards", description = "Retrieve all cards from the database",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cards retrieved",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = CardOutputModel.class)))
            })
    public CollectionModel<CardOutputModel> findAll() {
        return cardOutputModelAssembler.toCollectionModel(cardService.findAll());
    }

    @PostMapping
    @Operation(summary = "Create a card", description = "Add a new card to the database",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Card created",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = CardOutputModel.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data",
                            content = @Content(mediaType = "application/problem+json"))
            })
    public ResponseEntity<CardOutputModel> save(
            @Parameter(description = "Id of the deck where the card belongs", example = "1")
            @Min(value = 1, message = "Deck id must be positive")
            @RequestParam Long deckId,
            @Valid @RequestBody CardCreateInput cardCreateInput
    ) {
        CardOutputModel cardOutputModel = cardOutputModelAssembler.toModel(cardService.save(deckId, cardCreateInput));

        return ResponseEntity
                .created(cardOutputModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(cardOutputModel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update card by id", description = "Update a card specified by its id in the database",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card updated",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = CardOutputModel.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data",
                            content = @Content(mediaType = "application/problem+json")),
                    @ApiResponse(responseCode = "404", description = "Card not found",
                            content = @Content(mediaType = "application/problem+json"))
            })
    public ResponseEntity<CardOutputModel> update(
            @Parameter(description = "Id of the card", example = "1")
            @Min(value = 1, message = "Card id must be positive")
            @PathVariable Long id,
            @Valid @RequestBody CardUpdateInput cardUpdateInput
    ) {
        CardOutputModel cardOutputModel = cardOutputModelAssembler.toModel(cardService.update(id, cardUpdateInput));

        return ResponseEntity
                .ok()
                .header("location",
                        cardOutputModel.getRequiredLink(IanaLinkRelations.SELF).toUri().toString())
                .body(cardOutputModel);
    }

    @GetMapping(value = "/{id}")
    @Operation(summary = "Find card by id", description = "Retrieve a specific card by its id from the database",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card retrieved",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = CardOutputModel.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid card id",
                            content = @Content(mediaType = "application/problem+json")),
                    @ApiResponse(responseCode = "404", description = "Card not found",
                            content = @Content(mediaType = "application/problem+json"))
            })
    public CardOutputModel findById(
            @Parameter(description = "Id of the card", example = "1")
            @Min(value = 1, message = "Card id must be positive")
            @PathVariable Long id
    ) {
        return cardOutputModelAssembler.toModel(cardService.findById(id));
    }

    @DeleteMapping(value = "/{id}")
    @Operation(summary = "Delete a card by its id", description = "Remove a card specified by its id from the database",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Card removed"),
                    @ApiResponse(responseCode = "400", description = "Invalid card id",
                            content = @Content(mediaType = "application/problem+json")),
                    @ApiResponse(responseCode = "404", description = "Card not found",
                            content = @Content(mediaType = "application/problem+json"))
            })
    public ResponseEntity<Void> deleteById(
            @Parameter(description = "Id of the card", example = "1")
            @Min(value = 1, message = "Card id must be positive")
            @PathVariable Long id
    ) {
        cardService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/deck/{deckId}", produces = {"application/hal+json"})
    @Operation(summary = "Get all cards of a deck",
            description = "Retrieve all decks of a user specified by his id from the database",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Decks retrieved",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = CardOutputModel.class)))
            })
    public CollectionModel<CardOfDeckOutputModel> findAllOfDeck(
            @Parameter(description = "Id of the deck who owns the cards", example = "1")
            @Min(value = 1, message = "Deck id must be positive")
            @PathVariable Long deckId
    ) {
        CollectionModel<CardOfDeckOutputModel> cardOfDeckOutputModels =
                cardOfDeckOutputModelAssembler.toCollectionModel(cardService.findAllOfDeck(deckId));
        cardOfDeckOutputModels.add(linkTo(methodOn(CardController.class).findAllOfDeck(deckId)).withSelfRel());
        return cardOfDeckOutputModels;
    }
}
