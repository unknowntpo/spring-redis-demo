package org.example.repositories.redis;

import org.example.entities.Student;
import org.springframework.data.repository.CrudRepository;

public interface StudentRedisRepository extends CrudRepository<Student, Integer> {

    Student getById(Integer id);
}
