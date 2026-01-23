package org.example.connectcg_be.dto;

import lombok.Data;

@Data
public class ReportRequest {
    private String targetType; // user, post, comment, group, message
    private Integer targetId;
    private String reason;
}
