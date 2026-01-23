package org.example.connectcg_be.service;

import org.example.connectcg_be.dto.ReportAdminUpdateRequest;
import org.example.connectcg_be.dto.ReportRequest;
import org.example.connectcg_be.entity.Report;

import java.util.List;

public interface ReportService {
    void createReport(ReportRequest request, String username);
    List<Report> getReportsByStatus(String status);
    Report getReportById(Integer id);
    void updateReport(Integer id, ReportAdminUpdateRequest request, String adminUsername);
}
