package com.bvas.bvas.repository;

import com.bvas.bvas.model.BillSubmissionWindow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillSubmissionWindowRepository extends JpaRepository<BillSubmissionWindow, Long> {
    
    Optional<BillSubmissionWindow> findByMonthAndYear(Integer month, Integer year);
    
    List<BillSubmissionWindow> findByIsLockedTrue();
    
    List<BillSubmissionWindow> findByIsLockedFalse();
    
    boolean existsByMonthAndYear(Integer month, Integer year);
}