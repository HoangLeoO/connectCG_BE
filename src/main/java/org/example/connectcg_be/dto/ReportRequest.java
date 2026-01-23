package org.example.connectcg_be.dto;

import lombok.Data;

@Data
public class ReportRequest {
    private String targetType;
    private Integer targetId;
    private String reason;
}
