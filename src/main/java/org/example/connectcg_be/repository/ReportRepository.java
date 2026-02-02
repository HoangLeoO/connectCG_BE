package org.example.connectcg_be.repository;

import org.example.connectcg_be.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Integer> {
    List<Report> findByStatus(String status);

    // Paginated methods
    Page<Report> findAll(Pageable pageable);

    Page<Report> findByStatus(String status, Pageable pageable);

    Page<Report> findByTargetType(String targetType, Pageable pageable);

    Page<Report> findByTargetTypeAndStatus(String targetType, String status, Pageable pageable);
}
