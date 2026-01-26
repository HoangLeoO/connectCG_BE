package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.dto.ReportAdminUpdateRequest;
import org.example.connectcg_be.dto.ReportRequest;
import org.example.connectcg_be.dto.ReportResponse;
import org.example.connectcg_be.entity.Report;
import org.example.connectcg_be.repository.ReportRepository;
import org.example.connectcg_be.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Override
    public List<ReportResponse> getAllReports() {
        return reportRepository.findAll().stream().map(this::mapToDto).toList();
    }

    @Override
    public List<ReportResponse> getReportsByStatus(String status) {
        return reportRepository.findByStatus(status.toUpperCase())
                .stream().map(this::mapToDto).toList();
    }

    private ReportResponse mapToDto(Report report) {
        ReportResponse dto = new ReportResponse();
        dto.setId(report.getId());
        dto.setTargetType(report.getTargetType());
        dto.setTargetId(report.getTargetId());
        dto.setReason(report.getReason());
        dto.setStatus(report.getStatus());
        dto.setCreatedAt(report.getCreatedAt());

        if (report.getReporter() != null) {
            dto.setReporterUsername(report.getReporter().getUsername());
        }
        if (report.getReviewer() != null) {
            dto.setReviewerUsername(report.getReviewer().getUsername());
        }
        return dto;
    }


    @Override
    public Report getReportById(Integer id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));
    }

    @Override
    public void createReport(ReportRequest request, String username) {
        Report report = new Report();
        report.setReason(request.getReason());
        report.setTargetType(request.getTargetType());
        report.setTargetId(request.getTargetId());
        report.setStatus("PENDING");
        reportRepository.save(report);
    }

    @Override
    public void updateReport(Integer id, ReportAdminUpdateRequest request, String adminUsername) {
        Report report = getReportById(id);
        report.setStatus(request.getStatus());
        reportRepository.save(report);
    }
}
