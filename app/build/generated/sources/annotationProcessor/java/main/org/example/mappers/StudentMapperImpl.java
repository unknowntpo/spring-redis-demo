package org.example.mappers;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.example.dtos.StudentDTO;
import org.example.entities.Student;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-02-18T14:40:57+0800",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.10.2.jar, environment: Java 21.0.2 (Azul Systems, Inc.)"
)
@Component
public class StudentMapperImpl implements StudentMapper {

    @Override
    public Student toEntity(StudentDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Student student = new Student();

        student.setId( dto.getId() );
        student.setName( dto.getName() );

        return student;
    }

    @Override
    public StudentDTO toDto(Student entiy) {
        if ( entiy == null ) {
            return null;
        }

        String name = null;

        name = entiy.getName();

        StudentDTO studentDTO = new StudentDTO( name );

        studentDTO.setId( entiy.getId() );

        return studentDTO;
    }

    @Override
    public List<Student> toEntity(List<StudentDTO> dtos) {
        if ( dtos == null ) {
            return null;
        }

        List<Student> list = new ArrayList<Student>( dtos.size() );
        for ( StudentDTO studentDTO : dtos ) {
            list.add( toEntity( studentDTO ) );
        }

        return list;
    }

    @Override
    public List<StudentDTO> toDto(List<Student> entities) {
        if ( entities == null ) {
            return null;
        }

        List<StudentDTO> list = new ArrayList<StudentDTO>( entities.size() );
        for ( Student student : entities ) {
            list.add( toDto( student ) );
        }

        return list;
    }
}
