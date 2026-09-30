package com.academix.importer;

import com.academix.entity.ImportJob;
import com.academix.entity.Student;
import com.academix.repository.ImportJobRepository;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.time.Instant;
import java.util.*;

@Component
@RequiredArgsConstructor
public class StudentFileImporter {
    private static final int BATCH_SIZE = 500;
    private final StudentImportBatchService batchService;
    private final ImportJobRepository jobs;

    public ImportJob run(MultipartFile file) throws Exception {
        String name = Optional.ofNullable(file.getOriginalFilename()).orElse("upload");
        String lowerName = name.toLowerCase(Locale.ROOT);
        if (!(lowerName.endsWith(".csv") || lowerName.endsWith(".xlsx") || lowerName.endsWith(".xls")))
            throw new IllegalArgumentException("Only CSV and Excel files are supported");

        ImportJob job = new ImportJob();
        job.setFileName(name);
        job.setStatus("PROCESSING");
        job.setStartedAt(Instant.now());
        job = jobs.save(job);

        ImportCounters counters = new ImportCounters();
        List<Student> buffer = new ArrayList<>(BATCH_SIZE);
        Set<String> seenCodes = new HashSet<>();
        try {
            if (lowerName.endsWith(".csv")) readCsv(file, buffer, seenCodes, counters);
            else readExcel(file, buffer, seenCodes, counters);
            flush(buffer, counters);
            job.setStatus(counters.failed > 0 || counters.duplicates > 0 ? "PARTIAL_SUCCESS" : "COMPLETED");
        } catch (Exception exception) {
            job.setStatus("FAILED");
            throw exception;
        } finally {
            job.setTotalRows(counters.total);
            job.setProcessedRows(counters.total);
            job.setSuccessRows(counters.imported);
            job.setFailedRows(counters.failed);
            job.setDuplicateRows(counters.duplicates);
            job.setCompletedAt(Instant.now());
            jobs.save(job);
        }
        return job;
    }

    private void readCsv(MultipartFile file, List<Student> buffer, Set<String> seenCodes,
                         ImportCounters counters) throws Exception {
        try (var reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] headers = reader.readNext();
            if (headers == null) throw new IllegalArgumentException("The import file has no header row");
            String[] row;
            while ((row = reader.readNext()) != null) processRow(headers, row, buffer, seenCodes, counters);
        }
    }

    private void readExcel(MultipartFile file, List<Student> buffer, Set<String> seenCodes,
                           ImportCounters counters) throws Exception {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            if (workbook.getNumberOfSheets() == 0) throw new IllegalArgumentException("The workbook has no sheets");
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            if (!rows.hasNext()) throw new IllegalArgumentException("The import sheet is empty");
            String[] headers = rowValues(rows.next());
            while (rows.hasNext()) processRow(headers, rowValues(rows.next()), buffer, seenCodes, counters);
        }
    }

    private void processRow(String[] headers, String[] values, List<Student> buffer,
                            Set<String> seenCodes, ImportCounters counters) {
        counters.total++;
        try {
            Student student = map(headers, values);
            String code = student.getStudentCode();
            if (code != null && !code.isBlank() && !seenCodes.add(code)) {
                counters.duplicates++;
                return;
            }
            buffer.add(student);
            if (buffer.size() == BATCH_SIZE) flush(buffer, counters);
        } catch (IllegalArgumentException exception) {
            counters.failed++;
        }
    }

    private void flush(List<Student> buffer, ImportCounters counters) {
        if (buffer.isEmpty()) return;
        StudentImportBatchService.BatchResult result = batchService.persist(List.copyOf(buffer));
        counters.imported += result.imported();
        counters.duplicates += result.duplicates();
        buffer.clear();
    }

    private Student map(String[] headers, String[] values) {
        Map<String, String> source = new HashMap<>();
        for (int i = 0; i < Math.min(headers.length, values.length); i++)
            source.put(key(headers[i]), values[i] == null ? "" : values[i].trim());
        Student student = new Student();
        student.setStudentCode(value(source, "studentid", "studentcode", "id"));
        student.setFullName(value(source, "studentname", "fullname", "name"));
        student.setPhone(value(source, "mobileno", "mobile", "phone"));
        student.setEmail(value(source, "email", "emailaddress"));
        student.setCity(value(source, "town", "city"));
        student.setClassName(value(source, "class", "classname"));
        student.setCourse(value(source, "coursename", "course"));
        student.setBatch(value(source, "batch", "batchname"));
        student.setStatus(Optional.ofNullable(value(source, "status"))
                .filter(status -> !status.isBlank()).orElse("ACTIVE").toUpperCase(Locale.ROOT));
        if (student.getFullName() == null || student.getFullName().isBlank())
            throw new IllegalArgumentException("Student name is required");
        return student;
    }

    private String[] rowValues(Row row) {
        if (row.getLastCellNum() < 0) return new String[0];
        String[] values = new String[row.getLastCellNum()];
        DataFormatter formatter = new DataFormatter();
        for (int i = 0; i < values.length; i++) values[i] = formatter.formatCellValue(row.getCell(i));
        return values;
    }

    private String key(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private String value(Map<String, String> source, String... keys) {
        for (String key : keys) if (source.containsKey(key)) return source.get(key);
        return null;
    }

    private static final class ImportCounters {
        long total, imported, failed, duplicates;
    }
}
