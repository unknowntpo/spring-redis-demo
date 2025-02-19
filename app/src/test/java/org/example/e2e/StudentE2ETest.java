package org.example.e2e;

import org.example.dtos.StudentDTO;
import org.example.entities.Student;
import org.example.repositories.jpa.StudentRepository;
import org.example.repositories.redis.StudentRedisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("e2e")
class StudentE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentRedisRepository studentRedisRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Test methods will go here
    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushAll();
    }

    @Test
    void shouldCreateAndRetrieveUser() {
        // Given
        StudentDTO studentDto = new StudentDTO("Eric");

        // When
        ResponseEntity<StudentDTO> createResponse = restTemplate.postForEntity("/students", studentDto, StudentDTO.class);

        // Then
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        assertNotNull(createResponse.getBody().getId());

        // Verify user is in the database
        Optional<Student> savedStudent = studentRepository.findById(createResponse.getBody().getId());
        assertNotNull(savedStudent);
        assertTrue(savedStudent.isPresent());
        assertEquals(studentDto.getName(), savedStudent.get().getName());

        // Retrieve the user via API
        ResponseEntity<StudentDTO> getResponse = restTemplate.getForEntity("/students/{id}", StudentDTO.class, createResponse.getBody().getId());

        // Then
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertEquals(studentDto.getName(), getResponse.getBody().getName());
    }

    @Test
    void shouldGetFromDbWhenStudentIsNotInCache() {
        // Given
        StudentDTO studentDto = new StudentDTO("Eric");

        // When
        ResponseEntity<StudentDTO> createResponse = restTemplate.postForEntity("/students", studentDto, StudentDTO.class);

        // Then
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        assertNotNull(createResponse.getBody().getId());

        // Verify user is in the database
        Optional<Student> savedStudentOpt = studentRepository.findById(createResponse.getBody().getId());
        assertTrue(savedStudentOpt.isPresent());
        Student savedStudent = savedStudentOpt.get();
        assertEquals(studentDto.getName(), savedStudent.getName());

        // Delete from Redis
        String idHash = String.format("student:%d", savedStudent.getId());
        redisTemplate.opsForValue().getOperations().delete(idHash);
        assertFalse(redisTemplate.hasKey(idHash));

        // Retrieve the Student via API
        ResponseEntity<StudentDTO> getResponse = restTemplate.getForEntity("/students/{id}", StudentDTO.class, createResponse.getBody().getId());

        Student studentFromRedis = studentRedisRepository.getById(savedStudent.getId());
        assertNotNull(studentFromRedis);

        // Then
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertEquals(studentDto.getName(), getResponse.getBody().getName());

        Student gotStudent = studentRedisRepository.getById(savedStudent.getId());
        assertNotNull(gotStudent);

        // Make sure student has been set to cache
        assertTrue(redisTemplate.hasKey(idHash));
    }
}
