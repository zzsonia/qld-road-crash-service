package com.example.qld_roadcrash_service.model;

public record CrashSummary(
        String type,
        String crashMonth,
        String suburb,
        String severity

) {}