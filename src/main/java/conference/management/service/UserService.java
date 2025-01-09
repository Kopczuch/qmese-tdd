package conference.management.service;

import conference.management.mapper.LectureMapper;
import conference.management.mapper.UserMapper;
import conference.management.model.Lecture;
import conference.management.model.LectureRequest;
import conference.management.model.User;
import conference.management.repository.LectureRepository;
import conference.management.repository.UserRepository;
import conference.management.repository.entity.LectureEntity;
import conference.management.repository.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;
    private final UserMapper userMapper;
    private final LectureMapper lectureMapper;

    public List<User> obtainRegisteredUsers() {
        return userRepository.findAll().stream()
            .filter(user -> !user.getLectures().isEmpty())
            .map(userMapper::toUser)
            .toList();
    }

    public List<User> obtainAllUsers() {
        return userRepository.findAll().stream()
            .map(userMapper::toUser)
            .toList();
    }

    public List<Lecture> obtainLecturesByUser(String login) {
        Optional<UserEntity> possibleUser = userRepository.findByLogin(login);
        if (possibleUser.isEmpty()) {
            throw new IllegalArgumentException("Invalid login provided.");
        }
        return possibleUser.get().getLectures().stream()
            .map(lectureMapper::toLecture)
            .toList();
    }

    @Transactional
    public User updateUser(String email, String newEmail) {
        Optional<UserEntity> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("User with the provided email does not exist.");
        }

        UserEntity userEntity = optionalUser.get();

        if (userRepository.findByEmail(newEmail).isPresent()) {
            throw new IllegalArgumentException("The provided new email is already in use.");
        }

        userEntity.setEmail(newEmail);
        UserEntity updatedEntity = userRepository.save(userEntity);

        return userMapper.toUser(updatedEntity);
    }

    @Transactional
    public void registerForLecture(String login, LectureRequest lectureRequest) {
        UserEntity userEntity = findUser(login);

        LectureEntity lectureEntity = findLecture(lectureRequest.pathNumber(), lectureRequest.lectureNumber());

        if (lectureEntity.getCapacity() <= 0) {
            throw new IllegalArgumentException("No available seats for the requested lecture.");
        }

        if (userEntity.getLectures().contains(lectureEntity)) {
            throw new IllegalArgumentException("User is already registered for this lecture.");
        }

        lectureEntity.setCapacity(lectureEntity.getCapacity() - 1);
        userEntity.getLectures().add(lectureEntity);
        lectureEntity.getUsers().add(userEntity);

        userRepository.save(userEntity);
        lectureRepository.save(lectureEntity);
    }

    @Transactional
    public void cancelReservation(String login, LectureRequest lectureRequest) {
        UserEntity userEntity = findUser(login);
        LectureEntity lectureEntity = findLecture(lectureRequest.pathNumber(), lectureRequest.lectureNumber());

        Set<LectureEntity> lectures = userEntity.getLectures();
        lectures.remove(lectureEntity);
        userEntity.setLectures(lectures);
        userRepository.save(userEntity);
    }

    private LectureEntity findLecture(Integer pathNumber, Integer lectureNumber) {
        Optional<LectureEntity> possibleLecture = lectureRepository.findByPathNumberAndLectureNumber(pathNumber, lectureNumber);
        if (possibleLecture.isEmpty()) {
            throw new IllegalArgumentException("Invalid path number or lecture number provided.");
        }
        return possibleLecture.get();
    }

    private UserEntity findUser(String login) {
        Optional<UserEntity> possibleUserByLogin = userRepository.findByLogin(login);
        if (possibleUserByLogin.isEmpty()) {
            throw new IllegalArgumentException("Invalid login provided.");
        }
        return possibleUserByLogin.get();
    }
}
