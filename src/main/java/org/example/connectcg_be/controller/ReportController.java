//package org.example.connectcg_be.controller;
//import org.example.connectcg_be.dto.ReportAdminUpdateRequest;
//import org.example.connectcg_be.dto.ReportRequest;
//import org.example.connectcg_be.entity.Report;
//import org.example.connectcg_be.service.ReportService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.bind.annotation.*;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/reports")
//@CrossOrigin("*")
//public class ReportController {
//
//    private final ReportService reportService;
//
//    public ReportController(ReportService reportService) {
//        this.reportService = reportService;
//    }
//
//    // =========================
//    // USER: GỬI REPORT
//    // =========================
//    @PostMapping
//    public ResponseEntity<?> createReport(
//            @RequestBody ReportRequest request,
//            @AuthenticationPrincipal UserDetails userDetails
//    ) {
//        reportService.createReport(request, userDetails.getUsername());
//        return ResponseEntity.ok("Report submitted successfully");
//    }
//
//    // =========================
//    // ADMIN: XEM DANH SÁCH REPORT
//    // =========================
//    @GetMapping("/admin")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<List<Report>> getReports(
//            @RequestParam(required = false, defaultValue = "pending") String status
//    ) {
//        return ResponseEntity.ok(reportService.getReportsByStatus(status));
//    }
//
//    // =========================
//    // ADMIN: XEM CHI TIẾT 1 REPORT
//    // =========================
//    @GetMapping("/admin/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<Report> getReportDetail(@PathVariable Integer id) {
//        return ResponseEntity.ok(reportService.getReportById(id));
//    }
//
//    // =========================
//    // ADMIN: XỬ LÝ REPORT
//    // =========================
//    @PutMapping("/admin/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<?> updateReportStatus(
//            @PathVariable Integer id,
//            @RequestBody ReportAdminUpdateRequest request,
//            @AuthenticationPrincipal UserDetails admin
//    ) {
//        reportService.updateReport(id, request, admin.getUsername());
//        return ResponseEntity.ok("Report updated");
//    }
//}
