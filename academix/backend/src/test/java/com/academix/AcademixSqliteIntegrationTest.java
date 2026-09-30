package com.academix;

import com.academix.dto.BulkUpdateRequest;
import com.academix.dto.StudentRequest;
import com.academix.entity.ImportJob;
import com.academix.importer.StudentFileImporter;
import com.academix.repository.ImportJobRepository;
import com.academix.repository.StudentRepository;
import com.academix.service.StudentService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import javax.sql.DataSource;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AcademixSqliteIntegrationTest {
    @Autowired StudentService service;
    @Autowired StudentRepository students;
    @Autowired ImportJobRepository jobs;
    @Autowired StudentFileImporter importer;
    @Autowired DataSource dataSource;
    @Autowired MockMvc mockMvc;

    @BeforeEach
    void clearDatabase() {
        jobs.deleteAll();
        students.deleteAll();
    }

    @Test
    void crudSearchFiltersPaginationAndSortingRunOnSqlite() {
        var aarav = service.create(request("AX-002", "Aarav Singh", "Bangalore", "JEE", "ACTIVE"));
        service.create(request("AX-001", "Roshni Sharma", "Delhi", "NEET", "ACTIVE"));
        service.create(request("AX-003", "Roshan Singh", "Bangalore", "JEE", "INACTIVE"));

        assertEquals("Aarav Singh", service.get(aarav.id()).fullName());
        var updated = service.update(aarav.id(), request("AX-002", "Aarav Kumar Singh", "Bangalore", "JEE", "ACTIVE"));
        assertEquals("Aarav Kumar Singh", updated.fullName());

        var partialCaseInsensitive = service.search("rOsH", Map.of(), 0, 50, "fullName", "asc");
        assertEquals(2, partialCaseInsensitive.totalElements());

        var filtered = service.search("Singh", Map.of("city", "bangalore", "course", "jee"), 0, 50, "studentCode", "asc");
        assertEquals(2, filtered.totalElements());
        assertEquals("AX-002", filtered.content().getFirst().studentCode());

        var firstPage = service.search(null, Map.of(), 0, 2, "studentCode", "asc");
        assertEquals(2, firstPage.content().size());
        assertEquals(3, firstPage.totalElements());
        assertTrue(firstPage.hasNext());
        assertEquals("AX-001", firstPage.content().getFirst().studentCode());

        service.delete(aarav.id());
        assertEquals(2, students.count());
    }

    @Test
    void importsCsvAndXlsxAndFindsDuplicates() throws Exception {
        String csv = "Student ID,Student Name,Mobile No,Town,Course Name\n"
                + "CSV-1,Meera Nair,9999999999,Pune,NEET\n"
                + "CSV-2,Vivaan Rao,9999999999,Mumbai,JEE\n";
        ImportJob csvJob = importer.run(new MockMultipartFile("file", "students.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)));
        assertEquals("COMPLETED", csvJob.getStatus());
        assertEquals(2, csvJob.getSuccessRows());
        assertEquals(1, students.duplicatePhones().size());

        ImportJob duplicateJob = importer.run(new MockMultipartFile("file", "students-again.csv", "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)));
        assertEquals("PARTIAL_SUCCESS", duplicateJob.getStatus());
        assertEquals(0, duplicateJob.getSuccessRows());
        assertEquals(2, duplicateJob.getDuplicateRows());
        assertEquals(2, students.count());

        byte[] workbook;
        try (var book = new XSSFWorkbook(); var output = new ByteArrayOutputStream()) {
            var sheet = book.createSheet("Students");
            var header = sheet.createRow(0);
            List.of("Student ID", "Student Name", "Mobile No", "Town", "Course Name")
                    .forEach(value -> header.createCell(header.getLastCellNum() < 0 ? 0 : header.getLastCellNum()).setCellValue(value));
            var row = sheet.createRow(1);
            List.of("XLSX-1", "Zoya Khan", "8888888888", "Delhi", "Foundation")
                    .forEach(value -> row.createCell(row.getLastCellNum() < 0 ? 0 : row.getLastCellNum()).setCellValue(value));
            book.write(output);
            workbook = output.toByteArray();
        }
        ImportJob xlsxJob = importer.run(new MockMultipartFile("file", "students.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", workbook));
        assertEquals("COMPLETED", xlsxJob.getStatus());
        assertEquals(1, xlsxJob.getSuccessRows());
        assertEquals(3, students.count());
    }

    @Test
    void bulkUpdateAndDeleteRemainDatabaseBacked() {
        long first = service.create(request("B-1", "First Student", "Pune", "JEE", "ACTIVE")).id();
        long second = service.create(request("B-2", "Second Student", "Pune", "JEE", "ACTIVE")).id();

        assertEquals(2, service.bulkUpdate(new BulkUpdateRequest(List.of(first, second), "INACTIVE", "Batch Z", "NEET", "Bulk test")));
        assertEquals(2, service.search(null, Map.of("status", "inactive", "batch", "batch z"), 0, 50, "id", "asc").totalElements());
        assertEquals(2, service.bulkDelete(List.of(first, second)));
        assertEquals(0, students.count());
    }

    @Test
    void fieldSearchCombinesWithGlobalSearchFiltersSortingAndPagination() {
        service.create(request("FILTER-003", "Ananya Singh", "Pune", "JEE", "ACTIVE"));
        service.create(request("FILTER-001", "Ananya Rao", "Pune", "NEET", "ACTIVE"));
        service.create(request("FILTER-002", "Kabir Singh", "Delhi", "JEE", "INACTIVE"));

        assertEquals(2, service.search(null, Map.of("fullName", "ananya"), 0, 50, "id", "asc").totalElements());
        assertEquals(1, service.search(null, Map.of("phone", "7654", "email", "filter-001"), 0, 50, "id", "asc").totalElements());
        assertEquals(1, service.search(null, Map.of("studentCode", "003"), 0, 50, "id", "asc").totalElements());

        var combined = service.search("Singh", Map.of("city", "pune", "course", "jee", "status", "active"),
                0, 1, "studentCode", "desc");
        assertEquals(1, combined.totalElements());
        assertEquals("FILTER-003", combined.content().getFirst().studentCode());
        assertFalse(combined.hasNext());

        var cleared = service.search(null, Map.of(), 0, 2, "studentCode", "asc");
        assertEquals(3, cleared.totalElements());
        assertEquals("FILTER-001", cleared.content().getFirst().studentCode());
        assertTrue(cleared.hasNext());

        var invalidAge = assertThrows(IllegalArgumentException.class,
                () -> service.search(null, Map.of("ageFrom", "not-a-number"), 0, 50, "id", "asc"));
        assertEquals("Minimum age must be a whole number", invalidAge.getMessage());
    }

    @Test
    void schoolSupportsPartialCaseInsensitiveFieldAndGlobalSearch() {
        long id = service.create(request("SCHOOL-1", "School Student", "Pune", "JEE", "ACTIVE")).id();
        var student = students.findById(id).orElseThrow();
        student.setSchoolName("Delhi Public School");
        students.saveAndFlush(student);

        assertEquals(1, service.search(null, Map.of("schoolName", "public sch"), 0, 50, "id", "asc").totalElements());
        assertEquals(1, service.search("dELHi pubLIC", Map.of(), 0, 50, "id", "asc").totalElements());
        assertEquals(0, service.search(null, Map.of("schoolName", "unrelated"), 0, 50, "id", "asc").totalElements());
    }

    @Test
    void sqliteSafetyPragmasAreEnabled() throws Exception {
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            try (var result = statement.executeQuery("PRAGMA foreign_keys")) {
                assertTrue(result.next());
                assertEquals(1, result.getInt(1));
            }
            try (var result = statement.executeQuery("PRAGMA journal_mode")) {
                assertTrue(result.next());
                assertEquals("wal", result.getString(1).toLowerCase());
            }
            try (var result = statement.executeQuery("PRAGMA busy_timeout")) {
                assertTrue(result.next());
                assertTrue(result.getInt(1) >= 5000);
            }
        }
    }

    @Test
    void invalidFilterReturnsStructuredBadRequest() throws Exception {
        mockMvc.perform(get("/api/students").param("ageFrom", "not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Minimum age must be a whole number"));
    }

    @Test
    void filterOptionsAreDistinctSortedAndCitiesDependOnState() throws Exception {
        long puneId = service.create(request("OPT-1", "Pune Student", "Pune", "JEE", "ACTIVE")).id();
        long mumbaiId = service.create(request("OPT-2", "Mumbai Student", "Mumbai", "NEET", "INACTIVE")).id();
        long delhiId = service.create(request("OPT-3", "Delhi Student", "Delhi", "jee", "ACTIVE")).id();

        var pune = students.findById(puneId).orElseThrow();
        pune.setState("Maharashtra"); pune.setClassName("Class 10"); pune.setBatch("Alpha"); pune.setSource("Website");
        var mumbai = students.findById(mumbaiId).orElseThrow();
        mumbai.setState("Maharashtra"); mumbai.setClassName("Class 12"); mumbai.setBatch("Beta"); mumbai.setSource("Referral");
        var delhi = students.findById(delhiId).orElseThrow();
        delhi.setState("Delhi"); delhi.setClassName("Class 10"); delhi.setBatch("Alpha"); delhi.setSource("Website");
        students.saveAllAndFlush(List.of(pune, mumbai, delhi));

        var combined = service.search("Pune", Map.of(
                "course", "JE", "className", "10", "state", "Maha", "city", "Pun",
                "batch", "Alp", "status", "ACTIVE", "source", "Web"
        ), 0, 50, "studentCode", "asc");
        assertEquals(1, combined.totalElements());
        assertEquals("OPT-1", combined.content().getFirst().studentCode());

        mockMvc.perform(get("/api/students/filter-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courses.length()").value(2))
                .andExpect(jsonPath("$.classes[0]").value("Class 10"))
                .andExpect(jsonPath("$.states[0]").value("Delhi"))
                .andExpect(jsonPath("$.states[1]").value("Maharashtra"))
                .andExpect(jsonPath("$.cities.length()").value(3))
                .andExpect(jsonPath("$.batches.length()").value(2))
                .andExpect(jsonPath("$.statuses.length()").value(2))
                .andExpect(jsonPath("$.sources.length()").value(2));

        mockMvc.perform(get("/api/students/filter-options").param("state", "maha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cities.length()").value(2))
                .andExpect(jsonPath("$.cities[0]").value("Mumbai"))
                .andExpect(jsonPath("$.cities[1]").value("Pune"));
    }

    @Test
    void csvExportUsesFiltersAndSortingWithoutTablePagination() throws Exception {
        service.create(request("EXPORT-003", "Ananya Singh", "Pune", "JEE", "ACTIVE"));
        service.create(request("EXPORT-001", "Ananya Rao", "Pune", "JEE", "ACTIVE"));
        service.create(request("EXPORT-002", "Kabir Singh", "Delhi", "NEET", "INACTIVE"));

        var pending = mockMvc.perform(get("/api/students/export")
                        .param("q", "ananya")
                        .param("city", "pune")
                        .param("course", "jee")
                        .param("status", "active")
                        .param("sort", "studentCode")
                        .param("direction", "asc")
                        // Export deliberately ignores table pagination parameters.
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.request().asyncStarted())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.matchesPattern("attachment; filename=\"academix-students-.*\\.csv\"")))
                .andReturn();
        var response = mockMvc.perform(asyncDispatch(pending)).andExpect(status().isOk()).andReturn().getResponse();

        String csv = response.getContentAsString(StandardCharsets.UTF_8);
        assertTrue(csv.contains("EXPORT-001"));
        assertTrue(csv.contains("EXPORT-003"));
        assertFalse(csv.contains("EXPORT-002"));
        assertTrue(csv.indexOf("EXPORT-001") < csv.indexOf("EXPORT-003"));
        assertEquals(3, csv.lines().count()); // header plus both matches, despite size=1
    }

    @Test
    void csvExportSupportsNoFiltersAndZeroResults() throws Exception {
        service.create(request("ALL-001", "First Student", "Pune", "JEE", "ACTIVE"));
        service.create(request("ALL-002", "Second Student", "Delhi", "NEET", "INACTIVE"));

        var allPending = mockMvc.perform(get("/api/students/export")).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.request().asyncStarted()).andReturn();
        String all = mockMvc.perform(asyncDispatch(allPending)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertEquals(3, all.lines().count());

        var emptyPending = mockMvc.perform(get("/api/students/export").param("city", "not-a-real-city"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.request().asyncStarted()).andReturn();
        String empty = mockMvc.perform(asyncDispatch(emptyPending)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertEquals(1, empty.lines().count());
        assertTrue(empty.contains("Student ID"));
    }

    @Test
    void healthEndpointExposesOnlyServiceStatus() throws Exception {
        mockMvc.perform(get("/api/healthz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.database").doesNotExist());
    }

    private StudentRequest request(String code, String name, String city, String course, String status) {
        return new StudentRequest(
                code, null, null, null, name, "9876543210", null,
                code.toLowerCase() + "@example.com", null, null, null,
                null, null, null, null, null, null, city, null, null,
                null, null, null, null, course, "Batch A", null, "2026",
                null, status, "Test", null);
    }
}
