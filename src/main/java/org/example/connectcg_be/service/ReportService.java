package org.example.connectcg_be.service;

import org.example.connectcg_be.dto.ReportAdminUpdateRequest;
import org.example.connectcg_be.dto.ReportRequest;
import org.example.connectcg_be.dto.ReportResponse;
import org.example.connectcg_be.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReportService {
    void createReport(ReportRequest request, String username);

    Report getReportById(Integer id);

    void updateReport(Integer id, ReportAdminUpdateRequest request, String adminUsername);

    List<ReportResponse> getAllReports();

    List<ReportResponse> getReportsByStatus(String status);

    // Paginated methods
    Page<ReportResponse> getReportsPaginated(Pageable pageable);

    Page<ReportResponse> getReportsByStatusPaginated(String status, Pageable pageable);

    Page<ReportResponse> getReportsByTargetTypePaginated(String targetType, Pageable pageable);

    Page<ReportResponse> getReportsByTargetTypeAndStatusPaginated(String targetType, String status, Pageable pageable);
}
