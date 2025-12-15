package com.example.chatroomserver.service;

import com.example.chatroomserver.dto.SpamReportDto;
import com.example.chatroomserver.entity.SpamReport;
import com.example.chatroomserver.entity.User;
import com.example.chatroomserver.repository.SpamReportRepository;
import com.example.chatroomserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpamReportService {

    @Autowired private SpamReportRepository spamReportRepository;
    @Autowired private UserRepository userRepository;

    public void createReport(Integer reporterId, String reportedUsername, String reason) {
        User reporter = userRepository.findById(reporterId).orElseThrow();
        User reported = userRepository.findByUsername(reportedUsername);
        if (reported != null) {
            spamReportRepository.save(new SpamReport(reporter, reported, reason));
        }
    }

    // --- ADMIN FEATURES ---

    public List<SpamReportDto> getAllReports() {
        return spamReportRepository.findAll().stream()
                .map(r -> new SpamReportDto(
                        r.getId(),
                        r.getReporter().getUsername(),
                        r.getReportedUser().getUsername(),
                        r.getReason(),
                        r.getReportDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                ))
                .collect(Collectors.toList());
    }

    public void dismissReport(Integer reportId) {
        spamReportRepository.deleteById(reportId);
    }

    public void lockReportedUser(Integer reportId) {
        SpamReport report = spamReportRepository.findById(reportId).orElseThrow();
        User reportedUser = report.getReportedUser();

        // --- FIXED: Use Enum instead of String ---
        // If your Enum is defined inside User class:
        reportedUser.setStatus(User.Status.LOCKED);

        // Save the updated user status
        userRepository.save(reportedUser);

        // Resolve the ticket by deleting the report
        spamReportRepository.delete(report);
    }
}