package com.academix.dto;
import java.time.*;
public record StudentResponse(Long id,String studentCode,String fullName,String firstName,String middleName,String lastName,
 String phone,String alternatePhone,String email,String gender,LocalDate dateOfBirth,Integer age,String fatherName,
 String motherName,String guardianName,String guardianPhone,String addressLine1,String addressLine2,String city,String district,
 String state,String pinCode,String schoolName,String collegeName,String className,String course,String batch,String stream,
 String academicYear,LocalDate admissionDate,String status,String source,String remarks,Instant createdAt,Instant updatedAt,Long version) {}
