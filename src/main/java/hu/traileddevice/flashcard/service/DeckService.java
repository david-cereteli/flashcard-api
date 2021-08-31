package hu.traileddevice.flashcard.service;

import hu.traileddevice.flashcard.dto.deck.DeckCreateInput;
import hu.traileddevice.flashcard.dto.deck.DeckOutput;
import hu.traileddevice.flashcard.dto.deck.DeckUpdateInput;
import hu.traileddevice.flashcard.exception.QueriedDataDoesNotExistException;
import hu.traileddevice.flashcard.model.Deck;
import hu.traileddevice.flashcard.model.User;
import hu.traileddevice.flashcard.repository.DeckRepository;
import hu.traileddevice.flashcard.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeckService {

    private final DeckRepository deckRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public DeckService(DeckRepository deckRepository, UserRepository userRepository, ModelMapper modelMapper) {
        this.deckRepository = deckRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    public List<DeckOutput> findAll() {
        return modelMapper.map(deckRepository.findAll(), new TypeToken<List<DeckOutput>>() {
        }.getType());
    }

    public DeckOutput save(Long userId, DeckCreateInput deckCreateInput) {
        Deck deckToSave = modelMapper.map(deckCreateInput, Deck.class);

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) throw new QueriedDataDoesNotExistException("No such user id: " + userId);

        User existingUser = optionalUser.get();
        existingUser.addDeck(deckToSave);
        deckToSave.setUser(existingUser);

        return modelMapper.map(deckRepository.save(deckToSave), DeckOutput.class);
    }

    public DeckOutput update(Long id, DeckUpdateInput deckUpdateInput) {
        Optional<Deck> optionalDeck = deckRepository.findById(id);
        if (optionalDeck.isEmpty()) throw new QueriedDataDoesNotExistException("No such deck id: " + id);

        Deck deckToUpdate = optionalDeck.get();
        if (deckUpdateInput.getName() != null) deckToUpdate.setName(deckUpdateInput.getName());

        Long newUserId = deckUpdateInput.getUserId();
        if (newUserId != null && !newUserId.equals(deckToUpdate.getUser().getId())) {
            Optional<User> optionalUser = userRepository.findById(newUserId);
            if (optionalUser.isEmpty()) throw new QueriedDataDoesNotExistException("No such user id: " + newUserId);

            User oldUser = deckToUpdate.getUser();
            oldUser.removeDeck(deckToUpdate);
            User newUser = optionalUser.get();
            newUser.addDeck(deckToUpdate);

            deckToUpdate.setUser(newUser);
        }

        return modelMapper.map(deckRepository.save(deckToUpdate), DeckOutput.class);
    }

    public DeckOutput findById(Long id) {
        Optional<Deck> optionalDeck = deckRepository.findById(id);
        if (optionalDeck.isEmpty()) throw new QueriedDataDoesNotExistException("No such deck id: " + id);
        return modelMapper.map(optionalDeck.get(), DeckOutput.class);
    }

    public void deleteById(Long id) {
        Optional<Deck> optionalDeck = deckRepository.findById(id);
        if (optionalDeck.isEmpty()) throw new QueriedDataDoesNotExistException("No such deck id: " + id);
        deckRepository.deleteById(id);
    }

    public List<DeckOutput> findAllOfUser(Long userId) {
        List<Deck> allByUserId = deckRepository.findAllByUserId(userId);
        if (allByUserId.isEmpty()) throw new QueriedDataDoesNotExistException("User does not exist or has no decks.");
        return modelMapper.map(allByUserId, new TypeToken<List<DeckOutput>>() {}.getType());
    }
}
