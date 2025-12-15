package com.example.chatroomserver.controller;

import com.example.chatroomserver.dto.SpamReportDto;
import com.example.chatroomserver.service.SpamReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class SpamReportController {

    @Autowired private SpamReportService spamReportService;

    @PostMapping
    public ResponseEntity<String> submitReport(@RequestBody Map<String, Object> payload) {
        try {
            Integer reporterId = Integer.parseInt(payload.get("reporterId").toString());
            String reportedUsername = (String) payload.get("reportedUsername");
            String reason = (String) payload.get("reason");
            spamReportService.createReport(reporterId, reportedUsername, reason);
            return ResponseEntity.ok("Submitted");
        } catch (Exception e) { return ResponseEntity.badRequest().build(); }
    }

    // --- NEW ADMIN ENDPOINTS ---

    @GetMapping
    public List<SpamReportDto> getAllReports() {
        return spamReportService.getAllReports();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> dismissReport(@PathVariable Integer id) {
        spamReportService.dismissReport(id);
        return ResponseEntity.ok("Dismissed");
    }

    @PostMapping("/{id}/lock")
    public ResponseEntity<String> lockUser(@PathVariable Integer id) {
        spamReportService.lockReportedUser(id);
        return ResponseEntity.ok("User Locked");
    }
}