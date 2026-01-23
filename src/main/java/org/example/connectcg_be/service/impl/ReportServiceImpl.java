package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.dto.ReportAdminUpdateRequest;
import org.example.connectcg_be.dto.ReportRequest;
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
    public void createReport(ReportRequest request, String username) {

    }

    @Override
    public List<Report> getReportsByStatus(String status) {
        return List.of();
    }

    @Override
    public Report getReportById(Integer id) {
        return null;
    }

    @Override
    public void updateReport(Integer id, ReportAdminUpdateRequest request, String adminUsername) {

    }
}
