package com.academix.exporter;

import com.academix.entity.Student;
import com.opencsv.CSVWriter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

@Component
public class StudentExporter {
    public void csv(Consumer<Consumer<Student>> studentSource, OutputStream output) throws IOException {
        try (var writer = new CSVWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
            writer.writeNext(new String[]{"Student ID", "Name", "Phone", "Email", "City", "Class", "Course", "Batch", "Status"});
            studentSource.accept(student -> writer.writeNext(new String[]{
                    student.getStudentCode(), student.getFullName(), student.getPhone(), student.getEmail(),
                    student.getCity(), student.getClassName(), student.getCourse(), student.getBatch(), student.getStatus()
            }));
        }
    }
}
