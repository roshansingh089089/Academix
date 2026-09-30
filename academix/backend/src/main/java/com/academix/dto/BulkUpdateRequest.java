package com.academix.dto;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
public record BulkUpdateRequest(@NotEmpty List<Long> ids,String status,String batch,String course,String source) {}
