package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.dto.ReportAdminUpdateRequest;
import org.example.connectcg_be.dto.ReportRequest;
import org.example.connectcg_be.dto.ReportResponse;
import org.example.connectcg_be.entity.Report;
import org.example.connectcg_be.entity.User;
import org.example.connectcg_be.repository.ReportRepository;
import org.example.connectcg_be.repository.UserRepository;
import org.example.connectcg_be.service.NotificationService;
import org.example.connectcg_be.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {
    private final NotificationService notificationService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private org.example.connectcg_be.repository.PostRepository postRepository;

    public ReportServiceImpl(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

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
        if ("GROUP".equals(report.getTargetType())) {
            dto.setGroupId(report.getTargetId());
        } else if ("POST".equals(report.getTargetType())) {
            postRepository.findById(report.getTargetId()).ifPresent(post -> {
                if (post.getGroup() != null) {
                    dto.setGroupId(post.getGroup().getId());
                }
            });
        }

        dto.setCreatedAt(report.getCreatedAt());

        if (report.getReporter() != null) {
            dto.setReporterUsername(report.getReporter().getUsername());
            dto.setReporterId(report.getReporter().getId());
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
        // Tìm người dùng đang báo cáo
        User reporter = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Report report = new Report();
        report.setReason(request.getReason());
        report.setTargetType(request.getTargetType().toUpperCase());
        report.setTargetId(request.getTargetId());
        report.setStatus("PENDING");
        report.setReporter(reporter);
        report.setCreatedAt(Instant.now());
        Report savedReport = reportRepository.save(report);

        // Send WebSocket notification to all admins
        List<User> admins = userRepository.findByRole("ADMIN");
        for (User admin : admins) {
            org.example.connectcg_be.dto.TungNotificationDTO dto = new org.example.connectcg_be.dto.TungNotificationDTO();
            dto.setType("REPORT_SUBMITTED");
            dto.setContent("Có báo cáo mới từ người dùng về " + request.getTargetType().toLowerCase());
            dto.setTargetType("REPORT");
            dto.setTargetId(savedReport.getId());
            notificationService.sendNotification(dto, admin, reporter);
        }
    }

    @Override
    public void updateReport(Integer id, ReportAdminUpdateRequest request, String adminUsername) {
        Report report = getReportById(id);
        String oldStatus = report.getStatus();
        report.setStatus(request.getStatus());

        User admin = null;
        // Nếu chuyển trạng thái khác PENDING, lưu vết người duyệt
        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            admin = userRepository.findByUsername(adminUsername)
                    .orElseThrow(() -> new RuntimeException("Admin not found"));
            report.setReviewer(admin);
        }

        reportRepository.save(report);

        // Send WebSocket notification to reporter about status change
        if (!oldStatus.equalsIgnoreCase(request.getStatus()) && report.getReporter() != null) {
            String statusText = "RESOLVED".equalsIgnoreCase(request.getStatus())
                    ? "đã được xử lý"
                    : "đang được xem xét";

            org.example.connectcg_be.dto.TungNotificationDTO dto = new org.example.connectcg_be.dto.TungNotificationDTO();
            dto.setType("REPORT_UPDATED");
            dto.setContent("Báo cáo của bạn " + statusText);
            dto.setTargetType("REPORT");
            dto.setTargetId(report.getId());
            notificationService.sendNotification(dto, report.getReporter(), admin);
        }
    }

    // Paginated methods
    @Override
    public org.springframework.data.domain.Page<ReportResponse> getReportsPaginated(
            org.springframework.data.domain.Pageable pageable) {
        return reportRepository.findAll(pageable).map(this::mapToDto);
    }

    @Override
    public org.springframework.data.domain.Page<ReportResponse> getReportsByStatusPaginated(String status,
            org.springframework.data.domain.Pageable pageable) {
        return reportRepository.findByStatus(status.toUpperCase(), pageable).map(this::mapToDto);
    }

    @Override
    public org.springframework.data.domain.Page<ReportResponse> getReportsByTargetTypePaginated(String targetType,
            org.springframework.data.domain.Pageable pageable) {
        return reportRepository.findByTargetType(targetType.toUpperCase(), pageable).map(this::mapToDto);
    }

    @Override
    public org.springframework.data.domain.Page<ReportResponse> getReportsByTargetTypeAndStatusPaginated(
            String targetType, String status, org.springframework.data.domain.Pageable pageable) {
        return reportRepository.findByTargetTypeAndStatus(targetType.toUpperCase(), status.toUpperCase(), pageable)
                .map(this::mapToDto);
    }
}