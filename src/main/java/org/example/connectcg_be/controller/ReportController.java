package org.example.connectcg_be.controller;

import org.example.connectcg_be.dto.ReportAdminUpdateRequest;
import org.example.connectcg_be.dto.ReportRequest;
import org.example.connectcg_be.entity.Report;
import org.example.connectcg_be.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin("*")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // =========================
    // GỬI REPORT
    // =========================
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createReport(@RequestBody ReportRequest request, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Bạn cần đăng nhập để gửi báo cáo");
        }
        String currentUsername = principal.getName();
        reportService.createReport(request, currentUsername);

        return ResponseEntity.ok("Report submitted successfully");
    }

    // =========================
    // XEM DANH SÁCH REPORT (Paginated)
    // =========================
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getReports(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String targetType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page, size, org.springframework.data.domain.Sort.by("createdAt").descending());

        if (targetType != null && status != null) {
            return ResponseEntity
                    .ok(reportService.getReportsByTargetTypeAndStatusPaginated(targetType, status, pageable));
        } else if (targetType != null) {
            return ResponseEntity.ok(reportService.getReportsByTargetTypePaginated(targetType, pageable));
        } else if (status != null) {
            return ResponseEntity.ok(reportService.getReportsByStatusPaginated(status, pageable));
        } else {
            return ResponseEntity.ok(reportService.getReportsPaginated(pageable));
        }
    }

    // =========================
    // XEM CHI TIẾT REPORT
    // =========================
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Report> getReportDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }

    // =========================
    // XỬ LÝ REPORT
    // =========================
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateReportStatus(
            @PathVariable Integer id,
            @RequestBody ReportAdminUpdateRequest request) {
        reportService.updateReport(id, request, "admin");
        return ResponseEntity.ok("Report updated");
    }
}
