package hu.traileddevice.flashcard.service;

import hu.traileddevice.flashcard.dto.card.CardOutput;
import hu.traileddevice.flashcard.dto.learn.AnswerOutput;
import hu.traileddevice.flashcard.dto.learn.QuestionOutput;
import hu.traileddevice.flashcard.exception.QueriedDataDoesNotExistException;
import hu.traileddevice.flashcard.model.Card;
import hu.traileddevice.flashcard.model.CardTiming;
import hu.traileddevice.flashcard.model.Difficulty;
import hu.traileddevice.flashcard.repository.CardRepository;
import hu.traileddevice.flashcard.repository.CardTimingRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LearnService {

    private final CardRepository cardRepository;
    private final CardTimingRepository cardTimingRepository;
    private final ModelMapper modelMapper;

    public LearnService(CardRepository cardRepository, CardTimingRepository cardTimingRepository, ModelMapper modelMapper) {
        this.cardRepository = cardRepository;
        this.cardTimingRepository = cardTimingRepository;
        this.modelMapper = modelMapper;
    }

    public QuestionOutput getDueCardFromDeck(Long deckId) {
        Optional<CardTiming> optionalCardTiming = cardTimingRepository.findMostDueCardInDeck(deckId);
        if (optionalCardTiming.isEmpty())
            throw new QueriedDataDoesNotExistException("No due cards exist");
        return modelMapper.map(optionalCardTiming.get().getCard(), QuestionOutput.class);
    }

    public AnswerOutput getSolution(Long cardId) {
        Optional<Card> optionalCard = cardRepository.findById(cardId);
        if (optionalCard.isEmpty())
            throw new QueriedDataDoesNotExistException("Unable to find card with id: " + cardId);
        return modelMapper.map(optionalCard.get(), AnswerOutput.class);
    }

    public CardOutput updateTiming(Long cardId, Difficulty difficulty) {
        Optional<CardTiming> optionalCardTiming = cardTimingRepository.findByCardId(cardId);
        if (optionalCardTiming.isEmpty())
            throw new QueriedDataDoesNotExistException("Unable to find card with id: " + cardId);

        CardTiming cardTiming = optionalCardTiming.get();
        modifyTimings(cardTiming, difficulty);
        cardTiming = cardTimingRepository.save(cardTiming);

        CardOutput cardOutput = modelMapper.map(cardTiming.getCard(), CardOutput.class);
        cardOutput.setDueDate(calculateDueDate(cardTiming));
        return cardOutput;
    }

    private LocalDate calculateDueDate(CardTiming cardTiming) {
        return cardTiming.getLastReviewDate().plusDays(cardTiming.getRepetitionInterval()).toLocalDate();
    }

    /**
     * Calculates spaced repetition timings of a CardTiming entity based on SuperMemo's SM-2 algorithm.
     * See <a href="https://en.wikipedia.org/wiki/SuperMemo#Description_of_SM-2_algorithm">https://en.wikipedia.org/wiki/SuperMemo#Description_of_SM-2_algorithm</a>
     *
     * @param cardTiming the CardTiming object to update timings of
     * @param difficulty the perceived difficulty of the question
     */
    private void modifyTimings(CardTiming cardTiming, Difficulty difficulty) {
        int currentDifficulty = difficulty.getValue();
        int repetitionNumber = cardTiming.getRepetitionNumber();
        double easinessFactor = cardTiming.getEasinessFactor();
        int repetitionInterval = cardTiming.getRepetitionInterval();

        if (currentDifficulty >= 3) {
            if (repetitionNumber == 0) repetitionInterval = 1;
            else if (repetitionNumber == 1) repetitionInterval = 6;
            else repetitionInterval = (int) Math.ceil(repetitionInterval * easinessFactor);

            repetitionNumber++;
        } else {
            repetitionNumber = 0;
            repetitionInterval = 1;
        }

        easinessFactor = easinessFactor + (0.1 - (5 - currentDifficulty) * (0.08 + (5 - currentDifficulty) * 0.02));
        if (easinessFactor < 1.3) easinessFactor = 1.3;

        cardTiming.setLastReviewDate(LocalDateTime.now());
        cardTiming.setRepetitionNumber(repetitionNumber);
        cardTiming.setEasinessFactor(easinessFactor);
        cardTiming.setRepetitionInterval(repetitionInterval);
    }
}
