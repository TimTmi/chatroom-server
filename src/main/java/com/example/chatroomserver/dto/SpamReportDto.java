package com.example.chatroomserver.dto;

public class SpamReportDto {
    private Integer id;
    private String reporter;
    private String reported;
    private String reason;
    private String time;

    public SpamReportDto(Integer id, String reporter, String reported, String reason, String time) {
        this.id = id;
        this.reporter = reporter;
        this.reported = reported;
        this.reason = reason;
        this.time = time;
    }

    // Getters
    public Integer getId() { return id; }
    public String getReporter() { return reporter; }
    public String getReported() { return reported; }
    public String getReason() { return reason; }
    public String getTime() { return time; }
}