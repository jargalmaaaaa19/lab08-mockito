package mn.edu.must.sqat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseRegistrationServiceTest {

    @Mock
    StudentRepository repository;

    @Mock
    EmailSender emailSender;

    CourseRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new CourseRegistrationService(repository, emailSender);
    }

    // A1: Идэвхтэй оюутан бүртгүүлэхэд "OK" буцаж, saveRegistration дуудагдаж, и-мэйл илгээгдэнэ
    @Test
    void register_activeStudent_returnsOkSavesAndSendsEmail() {
        Student student = new Student("S123", "bold@must.edu.mn", true, Collections.emptyList());

        when(repository.findStudent("S123")).thenReturn(student);
        when(repository.courseExists("CS101")).thenReturn(true);
        when(repository.prerequisites("CS101")).thenReturn(Collections.emptyList());

        String result = service.register("S123", "CS101");

        assertEquals("OK", result);
        verify(repository).saveRegistration("S123", "CS101");
        verify(emailSender).send(eq("bold@must.edu.mn"), anyString(), contains("CS101"));
    }

    // A2: Байхгүй оюутанд "ERROR_NO_STUDENT"; сан руу хадгалаагүй, и-мэйл яваагүй
    @Test
    void register_nonExistentStudent_returnsErrorNoStudent() {
        when(repository.findStudent("S999")).thenReturn(null);

        String result = service.register("S999", "CS101");

        assertEquals("ERROR_NO_STUDENT", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }

    // A3: Идэвхгүй оюутанд "ERROR_INACTIVE_STUDENT"; и-мэйлтэй огт харьцаагүй
    @Test
    void register_inactiveStudent_returnsErrorInactiveStudent() {
        Student student = new Student("S456", "sara@must.edu.mn", false, Collections.emptyList());
        when(repository.findStudent("S456")).thenReturn(student);

        String result = service.register("S456", "CS101");

        assertEquals("ERROR_INACTIVE_STUDENT", result);
        verifyNoInteractions(emailSender);
    }

    // A4: Өмнөх холбоо хичээл дутуу бол "ERROR_PREREQUISITES"; хадгалаагүй
    @Test
    void register_missingPrerequisites_returnsErrorPrerequisites() {
        Student student = new Student("S123", "bold@must.edu.mn", true, Collections.emptyList());

        when(repository.findStudent("S123")).thenReturn(student);
        when(repository.courseExists("CS102")).thenReturn(true);
        when(repository.prerequisites("CS102")).thenReturn(List.of("CS101"));

        String result = service.register("S123", "CS102");

        assertEquals("ERROR_PREREQUISITES", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
    }

    // A5: И-мэйл илгээгч exception шидсэн ч бүртгэл "OK", хадгалалт хийгдсэн
    @Test
    void register_emailSenderThrows_stillReturnsOkAndSaves() {
        Student student = new Student("S123", "bold@must.edu.mn", true, Collections.emptyList());

        when(repository.findStudent("S123")).thenReturn(student);
        when(repository.courseExists("CS101")).thenReturn(true);
        when(repository.prerequisites("CS101")).thenReturn(Collections.emptyList());
        doThrow(new RuntimeException("SMTP error"))
                .when(emailSender).send(anyString(), anyString(), anyString());

        String result = service.register("S123", "CS101");

        assertEquals("OK", result);
        verify(repository).saveRegistration("S123", "CS101");
    }

    // A6: null оролтод IllegalArgumentException; хамаарлуудтай огт харьцаагүй
    @Test
    void register_nullArguments_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> service.register(null, "CS101"));
        assertThrows(IllegalArgumentException.class, () -> service.register("S123", null));
        verifyNoInteractions(repository, emailSender);
    }

    // A7: Илгээгдсэн и-мэйлийн биед хичээлийн код орсныг шалгах
    @Test
    void register_success_emailBodyContainsCourseCode() {
        Student student = new Student("S123", "bold@must.edu.mn", true, Collections.emptyList());

        when(repository.findStudent("S123")).thenReturn(student);
        when(repository.courseExists("CS101")).thenReturn(true);
        when(repository.prerequisites("CS101")).thenReturn(Collections.emptyList());

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        service.register("S123", "CS101");

        verify(emailSender).send(eq("bold@must.edu.mn"), anyString(), bodyCaptor.capture());
        assertTrue(bodyCaptor.getValue().contains("CS101"));
    }

    // A8: Гурван алдааны ангийг нэг параметржүүлсэн тестээр шалгах
    @ParameterizedTest
    @CsvSource({
            "NO_STUDENT, ERROR_NO_STUDENT",
            "INACTIVE, ERROR_INACTIVE_STUDENT",
            "PREREQUISITES, ERROR_PREREQUISITES"
    })
    void register_errorScenario_returnsExpectedErrorCode(String scenario, String expectedError) {
        lenient().when(repository.courseExists("CS102")).thenReturn(true);
        lenient().when(repository.prerequisites("CS102")).thenReturn(List.of("CS101"));

        switch (scenario) {
            case "NO_STUDENT" ->
                    lenient().when(repository.findStudent("S123")).thenReturn(null);
            case "INACTIVE" ->
                    lenient().when(repository.findStudent("S123"))
                            .thenReturn(new Student("S123", "t@must.edu.mn", false, Collections.emptyList()));
            case "PREREQUISITES" ->
                    lenient().when(repository.findStudent("S123"))
                            .thenReturn(new Student("S123", "t@must.edu.mn", true, Collections.emptyList()));
            default -> throw new IllegalArgumentException("Unknown scenario: " + scenario);
        }

        String result = service.register("S123", "CS102");

        assertEquals(expectedError, result);
    }
}
