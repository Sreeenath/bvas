package com.bvas.bvas.service;

import com.bvas.bvas.model.BillSubmissionWindow;
import com.bvas.bvas.model.User;
import com.bvas.bvas.repository.BillSubmissionWindowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BillSubmissionWindowService {

    private final BillSubmissionWindowRepository submissionWindowRepository;

    @Transactional
    public BillSubmissionWindow createOrUpdateWindow(Integer month, Integer year, 
                                                      Boolean isLocked, String lockReason, 
                                                      User lockedBy) {
        BillSubmissionWindow window = submissionWindowRepository
            .findByMonthAndYear(month, year)
            .orElse(new BillSubmissionWindow());

        window.setMonth(month);
        window.setYear(year);
        window.setIsLocked(isLocked);
        window.setLockReason(lockReason);
        window.setLockedBy(lockedBy);

        if (isLocked) {
            window.setLockedAt(LocalDateTime.now());
            window.setUnlockedAt(null);
        } else {
            window.setUnlockedAt(LocalDateTime.now());
        }

        return submissionWindowRepository.save(window);
    }

    public BillSubmissionWindow getWindow(Integer month, Integer year) {
        return submissionWindowRepository
            .findByMonthAndYear(month, year)
            .orElse(null);
    }

    public boolean isWindowLocked(Integer month, Integer year) {
        BillSubmissionWindow window = getWindow(month, year);
        return window != null && window.getIsLocked();
    }
}