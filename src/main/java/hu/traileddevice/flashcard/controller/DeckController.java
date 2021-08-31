package hu.traileddevice.flashcard.controller;

import hu.traileddevice.flashcard.dto.deck.DeckCreateInput;
import hu.traileddevice.flashcard.dto.deck.DeckOfUserOutputModel;
import hu.traileddevice.flashcard.dto.deck.DeckOutputModel;
import hu.traileddevice.flashcard.dto.deck.DeckUpdateInput;
import hu.traileddevice.flashcard.hateoas.deck.DeckOfUserOutputModelAssembler;
import hu.traileddevice.flashcard.hateoas.deck.DeckOutputModelAssembler;
import hu.traileddevice.flashcard.service.DeckService;
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
@RequestMapping("/deck")
@Tag(name = "Deck CRUD", description = "Create, read, update, and delete decks.")
@Validated
public class DeckController {

    private final DeckService deckService;
    private final DeckOutputModelAssembler deckOutputModelAssembler;
    private final DeckOfUserOutputModelAssembler deckOfUserOutputModelAssembler;

    public DeckController(DeckService deckService, DeckOutputModelAssembler deckOutputModelAssembler, DeckOfUserOutputModelAssembler deckOfUserOutputModelAssembler) {
        this.deckService = deckService;
        this.deckOutputModelAssembler = deckOutputModelAssembler;
        this.deckOfUserOutputModelAssembler = deckOfUserOutputModelAssembler;
    }

    @GetMapping
    @Operation(summary = "Get all decks", description = "Retrieve all decks from the database",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Decks retrieved",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = DeckOutputModel.class)))
            })
    public CollectionModel<DeckOutputModel> findAll() {
        return deckOutputModelAssembler.toCollectionModel(deckService.findAll());
    }

    @PostMapping
    @Operation(summary = "Create a deck", description = "Add a new deck to the database",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Deck created",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = DeckOutputModel.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data",
                            content = @Content(mediaType = "application/problem+json"))
            })
    public ResponseEntity<DeckOutputModel> save(
            @Parameter(description = "Id of the user who owns the deck", example = "1")
            @Min(value = 1, message = "User id must be positive")
            @RequestParam Long userId,
            @Valid @RequestBody DeckCreateInput deckCreateInput
    ) {
        DeckOutputModel deckOutputModel = deckOutputModelAssembler.toModel(deckService.save(userId, deckCreateInput));

        return ResponseEntity
                .created(deckOutputModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(deckOutputModel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update deck by id", description = "Update a deck specified by its id in the database",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Deck updated",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = DeckOutputModel.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data",
                            content = @Content(mediaType = "application/problem+json")),
                    @ApiResponse(responseCode = "404", description = "Deck not found",
                            content = @Content(mediaType = "application/problem+json"))
            })
    public ResponseEntity<DeckOutputModel> update(
            @Parameter(description = "Id of the deck", example = "1")
            @Min(value = 1, message = "Deck id must be positive")
            @PathVariable Long id,
            @Valid @RequestBody DeckUpdateInput deckUpdateInput
    ) {
        DeckOutputModel deckOutputModel = deckOutputModelAssembler.toModel(deckService.update(id, deckUpdateInput));

        return ResponseEntity
                .ok()
                .header("location",
                        deckOutputModel.getRequiredLink(IanaLinkRelations.SELF).toUri().toString())
                .body(deckOutputModel);
    }

    @GetMapping(value = "/{id}")
    @Operation(summary = "Find deck by id", description = "Retrieve a specific deck by its id from the database",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Deck retrieved",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = DeckOutputModel.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid deck id",
                            content = @Content(mediaType = "application/problem+json")),
                    @ApiResponse(responseCode = "404", description = "Deck not found",
                            content = @Content(mediaType = "application/problem+json"))
            })
    public DeckOutputModel findById(
            @Parameter(description = "Id of the deck", example = "1")
            @Min(value = 1, message = "Deck id must be positive")
            @PathVariable Long id
    ) {
        return deckOutputModelAssembler.toModel(deckService.findById(id));
    }

    @DeleteMapping(value = "/{id}")
    @Operation(summary = "Delete a deck by its id", description = "Remove a deck specified by its id from the database",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Deck removed"),
                    @ApiResponse(responseCode = "400", description = "Invalid deck id",
                            content = @Content(mediaType = "application/problem+json")),
                    @ApiResponse(responseCode = "404", description = "Deck not found",
                            content = @Content(mediaType = "application/problem+json"))
            })
    public ResponseEntity<Void> deleteById(
            @Parameter(description = "Id of the deck", example = "1")
            @Min(value = 1, message = "Deck id must be positive")
            @PathVariable Long id
    ) {
        deckService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/user/{userId}", produces = {"application/hal+json"})
    @Operation(summary = "Get all decks of a user",
            description = "Retrieve all decks of a user specified by his id from the database",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Decks retrieved",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = DeckOutputModel.class))),
                    @ApiResponse(responseCode = "404", description = "User or decks not found",
                            content = @Content(mediaType = "application/problem+json"))
            })
    public CollectionModel<DeckOfUserOutputModel> findAllOfUser(
            @Parameter(description = "Id of the user who owns the decks", example = "1")
            @Min(value = 1, message = "User id must be positive")
            @PathVariable Long userId
    ) {
        CollectionModel<DeckOfUserOutputModel> deckOfUserOutputModels =
                deckOfUserOutputModelAssembler.toCollectionModel(deckService.findAllOfUser(userId));
        deckOfUserOutputModels.add(linkTo(methodOn(DeckController.class).findAllOfUser(userId)).withSelfRel());
        return deckOfUserOutputModels;
    }
}
