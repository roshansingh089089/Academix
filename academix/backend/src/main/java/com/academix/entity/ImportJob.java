package com.academix.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="import_jobs") @Getter @Setter @NoArgsConstructor
public class ImportJob {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 private String fileName; private long totalRows; private long processedRows; private long successRows; private long updatedRows;
 private long failedRows; private long duplicateRows; private String status; private Instant startedAt; private Instant completedAt;
}
