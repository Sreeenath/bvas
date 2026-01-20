package com.bvas.bvas.repository;

import com.bvas.bvas.model.AuditLog;
import com.bvas.bvas.model.User;
import com.bvas.bvas.model.enums.AuditAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByPerformedBy(User user);
    
    List<AuditLog> findByAction(AuditAction action);
    
    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.entityId = :entityId ORDER BY a.actionTimestamp DESC")
    List<AuditLog> findByEntity(@Param("entityType") String entityType, @Param("entityId") Long entityId);
    
    @Query("SELECT a FROM AuditLog a WHERE a.actionTimestamp BETWEEN :startDate AND :endDate ORDER BY a.actionTimestamp DESC")
    List<AuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM AuditLog a WHERE a.performedBy = :user AND a.actionTimestamp BETWEEN :startDate AND :endDate ORDER BY a.actionTimestamp DESC")
    List<AuditLog> findByUserAndDateRange(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}