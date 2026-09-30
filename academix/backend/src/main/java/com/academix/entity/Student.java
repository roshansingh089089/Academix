package com.academix.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity @Table(name="students") @Getter @Setter @NoArgsConstructor
public class Student {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="student_code", unique=true, length=50) private String studentCode;
    private String firstName; private String middleName; private String lastName; private String fullName;
    private String phone; private String alternatePhone; private String email; private String gender;
    private LocalDate dateOfBirth; private Integer age; private String fatherName; private String motherName;
    private String guardianName; private String guardianPhone; private String addressLine1; private String addressLine2;
    private String city; private String district; private String state; private String pinCode; private String schoolName;
    private String collegeName; private String className; private String course; private String batch; private String stream;
    private String academicYear; private LocalDate admissionDate; private String status = "ACTIVE"; private String source;
    @Column(columnDefinition="TEXT") private String remarks;
    private Instant createdAt; private Instant updatedAt;
    @Version private Long version;
    @PrePersist void created(){ createdAt=updatedAt=Instant.now(); normalize(); }
    @PreUpdate void updated(){ updatedAt=Instant.now(); normalize(); }
    private void normalize(){
        if (fullName==null || fullName.isBlank()) fullName=String.join(" ", firstName==null?"":firstName, middleName==null?"":middleName, lastName==null?"":lastName).trim().replaceAll("\\s+"," ");
        if(status==null || status.isBlank()) status="ACTIVE";
    }
}
