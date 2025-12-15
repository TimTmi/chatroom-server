package com.example.chatroomserver.repository;

import com.example.chatroomserver.entity.SpamReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpamReportRepository extends JpaRepository<SpamReport, Integer> {}