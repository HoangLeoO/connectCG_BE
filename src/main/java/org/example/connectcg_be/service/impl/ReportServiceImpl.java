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
    private ReportRepository reportRepository;

    @Autowired
    private org.example.connectcg_be.repository.PostRepository postRepository;
    private UserRepository userRepository; // 1. Cần thêm cái này

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
        // 2. Tìm người dùng đang báo cáo
        User reporter = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Report report = new Report();
        report.setReason(request.getReason());

        // Đảm bảo targetType viết hoa để khớp với Check Constraint trong DB (USER, GROUP, POST)
        report.setTargetType(request.getTargetType().toUpperCase());

        report.setTargetId(request.getTargetId());
        report.setStatus("PENDING");

        // 3. Quan trọng: Gán người báo cáo vào
        report.setReporter(reporter);
        report.setCreatedAt(Instant.now());
        reportRepository.save(report);
    }

    @Override
    public void updateReport(Integer id, ReportAdminUpdateRequest request, String adminUsername) {
        Report report = getReportById(id);
        report.setStatus(request.getStatus());

        // Nếu chuyển trạng thái khác PENDING, lưu vết người duyệt
        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            User admin = userRepository.findByUsername(adminUsername)
                    .orElseThrow(() -> new RuntimeException("Admin not found"));
            report.setReviewer(admin);
        }

        reportRepository.save(report);
    }
}