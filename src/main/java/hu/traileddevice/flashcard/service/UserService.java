package hu.traileddevice.flashcard.service;

import hu.traileddevice.flashcard.dto.user.UserCreateInput;
import hu.traileddevice.flashcard.dto.user.UserOutput;
import hu.traileddevice.flashcard.dto.user.UserUpdateInput;
import hu.traileddevice.flashcard.exception.DuplicateEmailException;
import hu.traileddevice.flashcard.exception.QueriedDataDoesNotExistException;
import hu.traileddevice.flashcard.model.User;
import hu.traileddevice.flashcard.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public UserService(UserRepository userRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    public List<UserOutput> findAll() {
        return modelMapper.map(userRepository.findAll(), new TypeToken<List<UserOutput>>(){}.getType());
    }

    public UserOutput save(UserCreateInput userCreateInput) {
        Optional<User> optionalDuplicateEmailUser = userRepository.findByEmail(userCreateInput.getEmail());
        if (optionalDuplicateEmailUser.isPresent()) throw new DuplicateEmailException("Email address already exists!");
        User userToSave = modelMapper.map(userCreateInput, User.class);
        return modelMapper.map(userRepository.save(userToSave), UserOutput.class);
    }

    public UserOutput update(Long id, UserUpdateInput userUpdateInput) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) throw new QueriedDataDoesNotExistException("No such user id: " + id);

        User userToUpdate = optionalUser.get();
        if (userUpdateInput.getName() != null) userToUpdate.setName(userUpdateInput.getName());
        if (userUpdateInput.getEmail() != null) {
            Optional<User> optionalDuplicateEmailUser = userRepository.findByEmail(userUpdateInput.getEmail());
            if (optionalDuplicateEmailUser.isPresent() && !optionalDuplicateEmailUser.get().getId().equals(id))
                throw new DuplicateEmailException("Email address already exists!");
            userToUpdate.setEmail(userUpdateInput.getEmail());
        }

        return modelMapper.map(userRepository.save(userToUpdate), UserOutput.class);
    }

    public UserOutput findById(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) throw new QueriedDataDoesNotExistException("No such user id: " + id);
        return modelMapper.map(optionalUser.get(), UserOutput.class);
    }

    public void deleteById(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) throw new QueriedDataDoesNotExistException("No such user id: " + id);
        userRepository.deleteById(id);
    }
}
