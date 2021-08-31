package hu.traileddevice.flashcard.hateoas.learn;

import hu.traileddevice.flashcard.controller.LearnController;
import hu.traileddevice.flashcard.dto.learn.AnswerOutput;
import hu.traileddevice.flashcard.dto.learn.AnswerOutputModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AnswerOutputModelAssembler extends RepresentationModelAssemblerSupport<AnswerOutput, AnswerOutputModel> {

    public AnswerOutputModelAssembler() {
        super(LearnController.class, AnswerOutputModel.class);
    }

    @Override
    public AnswerOutputModel toModel(AnswerOutput entity) {
        AnswerOutputModel answerOutputModel = instantiateModel(entity);

        answerOutputModel.setId(entity.getId());
        answerOutputModel.setDeckId(entity.getDeckId());
        answerOutputModel.setBackContent(entity.getBackContent());

        answerOutputModel.add(
                linkTo(methodOn(LearnController.class).getSolutionToQuestion(entity.getId())).withSelfRel());

        return answerOutputModel;
    }

}
