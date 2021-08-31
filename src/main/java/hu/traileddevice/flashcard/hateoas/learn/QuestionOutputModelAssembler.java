package hu.traileddevice.flashcard.hateoas.learn;

import hu.traileddevice.flashcard.controller.LearnController;
import hu.traileddevice.flashcard.dto.learn.QuestionOutput;
import hu.traileddevice.flashcard.dto.learn.QuestionOutputModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class QuestionOutputModelAssembler extends RepresentationModelAssemblerSupport<QuestionOutput, QuestionOutputModel> {

    public QuestionOutputModelAssembler() {
        super(LearnController.class, QuestionOutputModel.class);
    }

    @Override
    public QuestionOutputModel toModel(QuestionOutput entity) {
        QuestionOutputModel questionOutputModel = instantiateModel(entity);

        questionOutputModel.setId(entity.getId());
        questionOutputModel.setDeckId(entity.getDeckId());
        questionOutputModel.setFrontContent(entity.getFrontContent());

        questionOutputModel.add(linkTo(methodOn(LearnController.class).getDueCardFromDeck(entity.getDeckId())).withSelfRel());

        return questionOutputModel;
    }

}
