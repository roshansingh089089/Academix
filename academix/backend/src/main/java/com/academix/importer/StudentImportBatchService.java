package com.academix.importer;

import com.academix.entity.Student;
import com.academix.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StudentImportBatchService {
    private final StudentRepository students;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BatchResult persist(List<Student> batch) {
        Set<String> codes = new HashSet<>();
        batch.stream().map(Student::getStudentCode).filter(Objects::nonNull)
                .filter(code -> !code.isBlank()).forEach(codes::add);

        Set<String> existingCodes = new HashSet<>();
        if (!codes.isEmpty()) {
            students.findAllByStudentCodeIn(codes).stream()
                    .map(Student::getStudentCode).forEach(existingCodes::add);
        }

        List<Student> newStudents = batch.stream()
                .filter(student -> student.getStudentCode() == null
                        || student.getStudentCode().isBlank()
                        || !existingCodes.contains(student.getStudentCode()))
                .toList();
        students.saveAllAndFlush(newStudents);
        return new BatchResult(newStudents.size(), batch.size() - newStudents.size());
    }

    public record BatchResult(long imported, long duplicates) {}
}
