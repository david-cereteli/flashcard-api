package hu.traileddevice.flashcard.service;

import hu.traileddevice.flashcard.dto.card.CardOutput;
import hu.traileddevice.flashcard.dto.learn.AnswerOutput;
import hu.traileddevice.flashcard.dto.learn.QuestionOutput;
import hu.traileddevice.flashcard.model.Card;
import hu.traileddevice.flashcard.model.CardTiming;
import hu.traileddevice.flashcard.model.Difficulty;
import hu.traileddevice.flashcard.repository.CardRepository;
import hu.traileddevice.flashcard.repository.CardTimingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearnServiceUT {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardTimingRepository cardTimingRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private LearnService learnService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getDueCardFromDeck_existingDeckIdAndDueCard_returnsQuestionOutput() {
        final Long deckId = 1L;
        final CardTiming cardTiming = mock(CardTiming.class);
        final Card card = mock(Card.class);
        final QuestionOutput questionOutput = mock(QuestionOutput.class);
        when(cardTimingRepository.findMostDueCardInDeck(deckId)).thenReturn(Optional.of(cardTiming));
        when(cardTiming.getCard()).thenReturn(card);
        when(modelMapper.map(card, QuestionOutput.class)).thenReturn(questionOutput);

        assertEquals(questionOutput, learnService.getDueCardFromDeck(deckId));
    }

    @Test
    void getSolution_existingCardId_returnsAnswerOutput() {
        final Long cardId = 1L;
        final Card card = mock(Card.class);
        final AnswerOutput answerOutput = mock(AnswerOutput.class);
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(modelMapper.map(card, AnswerOutput.class)).thenReturn(answerOutput);

        assertEquals(answerOutput, learnService.getSolution(cardId));
    }

    @Test
    void updateTiming_neverLearnedCard_repetitionNumberSetTo1AndLastReviewDateUpdated() {
        final Long cardId = 1L;
        final CardTiming cardTiming = mock(CardTiming.class);
        final Difficulty difficulty = Difficulty.SUCCESS_EASY;
        final LocalDateTime beforeTestTime = LocalDateTime.now();
        final Card card = mock(Card.class);
        final CardOutput cardOutput = mock(CardOutput.class);
        ArgumentCaptor<LocalDateTime> savedTimeArgument = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<Integer> repetitionNumberArgument = ArgumentCaptor.forClass(Integer.class);
        when(cardTimingRepository.findByCardId(cardId)).thenReturn(Optional.of(cardTiming));
        when(cardTiming.getRepetitionNumber()).thenReturn(0);
        when(cardTiming.getEasinessFactor()).thenReturn(2.5);
        when(cardTiming.getRepetitionInterval()).thenReturn(1);
        when(cardTimingRepository.save(cardTiming)).thenReturn(cardTiming);
        when(cardTiming.getCard()).thenReturn(card);
        when(cardTiming.getLastReviewDate()).thenReturn(beforeTestTime);
        when(modelMapper.map(card, CardOutput.class)).thenReturn(cardOutput);


        assertEquals(cardOutput, learnService.updateTiming(cardId, difficulty));

        verify(cardTiming, times(1)).setRepetitionNumber(repetitionNumberArgument.capture());
        verify(cardTiming, times(1)).setLastReviewDate(savedTimeArgument.capture());

        assertEquals(1, repetitionNumberArgument.getValue());
        assertTrue(savedTimeArgument.getValue().isAfter(beforeTestTime));
    }
}