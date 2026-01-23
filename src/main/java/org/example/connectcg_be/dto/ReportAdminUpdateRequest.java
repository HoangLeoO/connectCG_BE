package org.example.connectcg_be.dto;

import lombok.Data;

@Data
public class ReportAdminUpdateRequest {
    private String status;     // resolved, dismissed, reviewing
    private String adminNote;
}
