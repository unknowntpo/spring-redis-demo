package org.example.services;

import org.example.dtos.StudentDTO;
import org.example.entities.Student;
import org.example.mappers.StudentMapper;
import org.example.repositories.jpa.StudentRepository;
import org.example.repositories.redis.StudentRedisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {
    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentRedisRepository studentRedisRepository;

    /**
     * Should use Mappers.getMapper to get the right mapper
     * Ref: https://www.reddit.com/r/javahelp/comments/nfcm9y/comment/gykyq3p/?utm_source=share&utm_medium=web3x&utm_name=web3xcss&utm_term=1&utm_content=share_button
     */
    @Spy
    private StudentMapper studentMapper = Mappers.getMapper(StudentMapper.class);

    @InjectMocks
    private StudentService studentService;

    @Test
    void testGetById() {
        Integer studentId = 100;
        String name = "mock name";
        Student mockStudent = new Student();
        mockStudent.setId(studentId);
        mockStudent.setName(name);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(mockStudent));
        when(studentRedisRepository.save(mockStudent)).thenReturn(mockStudent);

        StudentDTO mockStudentDto = new StudentDTO(name);
        mockStudentDto.setId(studentId);

        assertEquals(mockStudentDto, studentService.getById(studentId));
    }

    @Test
    void testSave() {
        // svc: creatSttudent (dto) , return dto
        // repo: save(student), return savedStudent
        // assert dto is returned, name is correct, id is mocked id

        String name = "Eric";
        StudentDTO studentDto = new StudentDTO(name);
        studentDto.setName(name);

        Student student = studentMapper.toEntity(studentDto);
        assertEquals(name, student.getName());

        Student mockStudent = new Student();
        Integer mockId = 333;
        mockStudent.setId(mockId);
        mockStudent.setName(name);

        when(studentRepository.save(student)).thenReturn(mockStudent);
        when(studentRedisRepository.save(mockStudent)).thenReturn(mockStudent);

        StudentDTO savedStudentDto = studentService.save(studentDto);

        assertEquals(mockId, savedStudentDto.getId());
        assertEquals(name, savedStudentDto.getName());
    }
}
