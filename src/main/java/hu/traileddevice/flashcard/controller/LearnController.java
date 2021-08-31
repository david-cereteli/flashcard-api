package hu.traileddevice.flashcard.controller;

import hu.traileddevice.flashcard.dto.card.CardOutput;
import hu.traileddevice.flashcard.dto.card.CardOutputModel;
import hu.traileddevice.flashcard.dto.learn.AnswerOutputModel;
import hu.traileddevice.flashcard.dto.learn.QuestionOutputModel;
import hu.traileddevice.flashcard.hateoas.card.CardOutputModelAssembler;
import hu.traileddevice.flashcard.hateoas.learn.AnswerOutputModelAssembler;
import hu.traileddevice.flashcard.hateoas.learn.QuestionOutputModelAssembler;
import hu.traileddevice.flashcard.model.Difficulty;
import hu.traileddevice.flashcard.service.LearnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/learn")
@Tag(name = "Learning", description = "Memorize flashcards")
@Validated
public class LearnController {

    private final LearnService learnService;
    private final QuestionOutputModelAssembler questionOutputModelAssembler;
    private final AnswerOutputModelAssembler answerOutputModelAssembler;
    private final CardOutputModelAssembler cardOutputModelAssembler;

    public LearnController(LearnService learnService, QuestionOutputModelAssembler questionOutputModelAssembler,
                           AnswerOutputModelAssembler answerOutputModelAssembler,
                           CardOutputModelAssembler cardOutputModelAssembler) {
        this.learnService = learnService;
        this.questionOutputModelAssembler = questionOutputModelAssembler;
        this.answerOutputModelAssembler = answerOutputModelAssembler;
        this.cardOutputModelAssembler = cardOutputModelAssembler;
    }

    @GetMapping(value = "/{deckId}")
    @Operation(summary = "Find a due card in a deck",
            description = "Retrieve the question on the most due card by its deck id from the database",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Question retrieved",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = QuestionOutputModel.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data",
                            content = @Content(mediaType = "application/problem+json")),
                    @ApiResponse(responseCode = "404", description = "There are no due cards in this deck",
                            content = @Content(mediaType = "application/problem+json"))
            })
    public QuestionOutputModel getDueCardFromDeck(
            @Parameter(description = "Id of the deck", example = "1")
            @Min(value = 1, message = "Deck id must be positive")
            @PathVariable Long deckId
    ) {
        QuestionOutputModel questionOutputModel =
                questionOutputModelAssembler.toModel(learnService.getDueCardFromDeck(deckId));
        questionOutputModel.add(
                linkTo(methodOn(LearnController.class).getSolutionToQuestion(questionOutputModel.getId())).withRel("get-solution")
        );
        return questionOutputModel;
    }

    @GetMapping("/solution/{cardId}")
    @Operation(summary = "Get the back of a card",
            description = "Retrieve the solution to a question on a card by its card id from the database",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Answer retrieved",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = AnswerOutputModel.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data",
                            content = @Content(mediaType = "application/problem+json")),
                    @ApiResponse(responseCode = "404", description = "Card not found",
                            content = @Content(mediaType = "application/problem+json"))
            })
    public AnswerOutputModel getSolutionToQuestion(
            @Parameter(description = "Id of the card", example = "1")
            @Min(value = 1, message = "Card id must be positive")
            @PathVariable Long cardId
    ) {
        AnswerOutputModel answerOutputModel = answerOutputModelAssembler.toModel(learnService.getSolution(cardId));
        answerOutputModel.add(
                linkTo(methodOn(LearnController.class).updateTimings(cardId, null)).withRel("update-timings")
        );
        return answerOutputModel;
    }

    @PutMapping("/{cardId}")
    @Operation(summary = "Set difficulty of card by id",
            description = "Update timing data of a card by its id in the database",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Card updated",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = CardOutputModel.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data",
                            content = @Content(mediaType = "application/problem+json")),
                    @ApiResponse(responseCode = "404", description = "Card not found",
                            content = @Content(mediaType = "application/problem+json"))
            })
    public ResponseEntity<CardOutputModel> updateTimings(
            @Parameter(description = "Id of the card", example = "1")
            @Min(value = 1, message = "Card id must be positive")
            @PathVariable Long cardId,
            @RequestParam Difficulty difficulty
    ) {

        CardOutput cardOutput = learnService.updateTiming(cardId, difficulty);
        CardOutputModel cardOutputModel = cardOutputModelAssembler.toModel(cardOutput);

        Link linkToCard = cardOutputModel.getRequiredLink(IanaLinkRelations.SELF);
        cardOutputModel.removeLinks(); // remove findById link (from reused card assembler)
        cardOutputModel.add( // add proper self link
                linkTo(methodOn(LearnController.class).updateTimings(cardId, difficulty)).withSelfRel()
        );

        cardOutputModel.add(
                linkTo(methodOn(LearnController.class).getDueCardFromDeck(cardOutput.getDeckId())).withRel("get-next-due-card")
        );

        return ResponseEntity
                .ok()
                .header("location",
                        linkToCard.toUri().toString()) // update requires location header link to modified object
                .body(cardOutputModel);
    }

}
