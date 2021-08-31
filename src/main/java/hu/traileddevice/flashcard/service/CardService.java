package hu.traileddevice.flashcard.service;

import hu.traileddevice.flashcard.dto.card.CardCreateInput;
import hu.traileddevice.flashcard.dto.card.CardOutput;
import hu.traileddevice.flashcard.dto.card.CardUpdateInput;
import hu.traileddevice.flashcard.exception.DuplicateFrontContentException;
import hu.traileddevice.flashcard.exception.QueriedDataDoesNotExistException;
import hu.traileddevice.flashcard.model.Card;
import hu.traileddevice.flashcard.model.CardTiming;
import hu.traileddevice.flashcard.model.Deck;
import hu.traileddevice.flashcard.repository.CardRepository;
import hu.traileddevice.flashcard.repository.CardTimingRepository;
import hu.traileddevice.flashcard.repository.DeckRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final CardTimingRepository cardTimingRepository;
    private final DeckRepository deckRepository;
    private final ModelMapper modelMapper;

    public CardService(CardRepository cardRepository, CardTimingRepository cardTimingRepository,
                       DeckRepository deckRepository, ModelMapper modelMapper) {
        this.cardRepository = cardRepository;
        this.cardTimingRepository = cardTimingRepository;
        this.deckRepository = deckRepository;
        this.modelMapper = modelMapper;
    }

    public List<CardOutput> findAll() {
        return modelMapper.map(cardRepository.findAll(), new TypeToken<List<CardOutput>>() {
        }.getType());
    }

    public CardOutput save(Long deckId, CardCreateInput cardCreateInput) {

        Card cardToSave = modelMapper.map(cardCreateInput, Card.class);

        Optional<Deck> optionalDeck = deckRepository.findById(deckId);
        if (optionalDeck.isEmpty()) throw new QueriedDataDoesNotExistException("No such deck id: " + deckId);

        Deck existingDeck = optionalDeck.get();
        existingDeck.addCard(cardToSave);
        cardToSave.setDeck(existingDeck);

        Optional<Card> duplicateFrontContent = cardRepository.findByFrontContent(cardToSave.getFrontContent());
        if (duplicateFrontContent.isPresent())
            throw new DuplicateFrontContentException("Front content already exists!");

        cardToSave = cardRepository.save(cardToSave);

        CardTiming cardTiming = cardTimingRepository.save(new CardTiming(cardToSave));
        cardToSave.setCardTiming(cardTiming);

        return modelMapper.map(cardToSave, CardOutput.class);
    }

    public CardOutput update(Long id, CardUpdateInput cardUpdateInput) {
        Optional<Card> optionalCard = cardRepository.findById(id);
        if (optionalCard.isEmpty()) throw new QueriedDataDoesNotExistException("No such card id: " + id);

        Card cardToUpdate = optionalCard.get();
        if (cardUpdateInput.getFrontContent() != null) cardToUpdate.setFrontContent(cardUpdateInput.getFrontContent());
        if (cardUpdateInput.getBackContent() != null) cardToUpdate.setBackContent(cardUpdateInput.getBackContent());

        Long newDeckId = cardUpdateInput.getDeckId();
        if (newDeckId != null && !newDeckId.equals(cardToUpdate.getDeck().getId())) {
            Optional<Deck> optionalDeck = deckRepository.findById(newDeckId);
            if (optionalDeck.isEmpty()) throw new QueriedDataDoesNotExistException("No such deck id: " + newDeckId);

            Deck oldDeck = cardToUpdate.getDeck();
            oldDeck.removeCard(cardToUpdate);
            Deck newDeck = optionalDeck.get();
            newDeck.addCard(cardToUpdate);

            cardToUpdate.setDeck(newDeck);
        }

        Optional<Card> duplicateFrontContent = cardRepository.findByFrontContent(cardToUpdate.getFrontContent());
        if (duplicateFrontContent.isPresent())
            throw new DuplicateFrontContentException("Front content already exists!");

        return modelMapper.map(cardRepository.save(cardToUpdate), CardOutput.class);
    }

    public CardOutput findById(Long id) {
        Optional<Card> optionalCard = cardRepository.findById(id);
        if (optionalCard.isEmpty()) throw new QueriedDataDoesNotExistException("No such card id: " + id);
        return modelMapper.map(optionalCard.get(), CardOutput.class);
    }

    public void deleteById(Long id) {
        Optional<Card> optionalCard = cardRepository.findById(id);
        if (optionalCard.isEmpty()) throw new QueriedDataDoesNotExistException("No such card id: " + id);
        cardRepository.deleteById(id);
    }

    public List<CardOutput> findAllOfDeck(Long deckId) {
        List<Card> allByDeckId = cardRepository.findAllByDeckId(deckId);
        if (allByDeckId.isEmpty()) throw new QueriedDataDoesNotExistException("Deck does not exist or is empty.");
        return modelMapper.map(allByDeckId, new TypeToken<List<CardOutput>>() {}.getType());
    }
}
