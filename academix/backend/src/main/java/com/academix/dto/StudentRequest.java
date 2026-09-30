package com.academix.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record StudentRequest(String studentCode, String firstName, String middleName, String lastName,
 @NotBlank @Size(max=255) String fullName, @Size(max=30) String phone, String alternatePhone,
 @Email String email, String gender, LocalDate dateOfBirth, Integer age, String fatherName, String motherName,
 String guardianName, String guardianPhone, String addressLine1, String addressLine2, String city, String district,
 String state, String pinCode, String schoolName, String collegeName, String className, String course, String batch,
 String stream, String academicYear, LocalDate admissionDate, String status, String source, String remarks) {}
