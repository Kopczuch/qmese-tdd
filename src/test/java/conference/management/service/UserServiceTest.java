package conference.management.service;

import conference.management.model.LectureRequest;
import conference.management.repository.LectureRepository;
import conference.management.repository.UserRepository;
import conference.management.repository.entity.LectureEntity;
import conference.management.repository.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LectureRepository lectureRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterForLecture_successful() {
        // Arrange
        String login = "testUser";
        LectureRequest lectureRequest = new LectureRequest(1, 1);
        UserEntity userEntity = new UserEntity();
        userEntity.setLogin(login);
        userEntity.setEmail("test@example.com");

        LectureEntity lectureEntity = new LectureEntity();
        lectureEntity.setLectureId(1);
        lectureEntity.setPathNumber(1);
        lectureEntity.setLectureNumber(1);
        lectureEntity.setCapacity(5);
        lectureEntity.setUsers(Set.of());

        when(userRepository.findByLogin(login)).thenReturn(Optional.of(userEntity));
        when(lectureRepository.findByPathNumberAndLectureNumber(1, 1))
                .thenReturn(Optional.of(lectureEntity));

        // Act
        userService.registerForLecture(login, lectureRequest);

        // Assert
        assertTrue(userEntity.getLectures().contains(lectureEntity));
        assertEquals(4, lectureEntity.getCapacity());
        verify(userRepository).save(userEntity);
        verify(lectureRepository).save(lectureEntity);
    }

    @Test
    void testRegisterForLecture_noSeatsAvailable() {
        // Arrange
        String login = "testUser";
        LectureRequest lectureRequest = new LectureRequest(1, 1);
        UserEntity userEntity = new UserEntity();
        userEntity.setLogin(login);
        userEntity.setEmail("test@example.com");

        LectureEntity lectureEntity = new LectureEntity();
        lectureEntity.setLectureId(1);
        lectureEntity.setPathNumber(1);
        lectureEntity.setLectureNumber(1);
        lectureEntity.setCapacity(0);
        lectureEntity.setUsers(Set.of());

        when(userRepository.findByLogin(login)).thenReturn(Optional.of(userEntity));
        when(lectureRepository.findByPathNumberAndLectureNumber(1, 1))
                .thenReturn(Optional.of(lectureEntity));

        // Act
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                userService.registerForLecture(login, lectureRequest));

        // Assert
        assertEquals("No available seats for the requested lecture.", exception.getMessage());
        verify(userRepository, never()).save(userEntity);
        verify(lectureRepository, never()).save(lectureEntity);
    }

    @Test
    void testUpdateUserEmail_successful() {
        // Arrange
        String login = "testUser";
        String newEmail = "newemail@example.com";
        UserEntity userEntity = new UserEntity();
        userEntity.setLogin(login);
        userEntity.setEmail("oldemail@example.com");

        when(userRepository.findByLogin(login)).thenReturn(Optional.of(userEntity));

        // Act
        userService.updateUser("oldemail@example.com", newEmail);

        // Assert
        assertEquals(newEmail, userEntity.getEmail());
        verify(userRepository).save(userEntity);
    }

    @Test
    void testUpdateUserEmail_userNotFound() {
        // Arrange
        String login = "nonexistentUser";
        String newEmail = "newemail@example.com";

        when(userRepository.findByLogin(login)).thenReturn(Optional.empty());

        // Act
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                userService.updateUser(login, newEmail));

        // Assert
        assertEquals("Invalid login provided.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }
}
