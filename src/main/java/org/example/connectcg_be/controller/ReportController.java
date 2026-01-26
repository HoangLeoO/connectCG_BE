package org.example.connectcg_be.controller;

import org.example.connectcg_be.dto.ReportAdminUpdateRequest;
import org.example.connectcg_be.dto.ReportRequest;
import org.example.connectcg_be.dto.ReportResponse;
import org.example.connectcg_be.entity.Report;
import org.example.connectcg_be.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<?> createReport(@RequestBody ReportRequest request) {
        reportService.createReport(request, "anonymous");
        return ResponseEntity.ok("Report submitted successfully");
    }

    // =========================
    // XEM DANH SÁCH REPORT
    // =========================
    @GetMapping
    public ResponseEntity<List<ReportResponse>> getReports(
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(
                status == null
                        ? reportService.getAllReports()
                        : reportService.getReportsByStatus(status)
        );
    }


    // =========================
    // XEM CHI TIẾT REPORT
    // =========================
    @GetMapping("/{id}")
    public ResponseEntity<Report> getReportDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }

    // =========================
    // XỬ LÝ REPORT
    // =========================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReportStatus(
            @PathVariable Integer id,
            @RequestBody ReportAdminUpdateRequest request
    ) {
        reportService.updateReport(id, request, "admin");
        return ResponseEntity.ok("Report updated");
    }
}
