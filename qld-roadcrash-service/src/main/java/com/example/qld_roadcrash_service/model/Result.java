package com.example.qld_roadcrash_service.model;

import java.util.List;
import java.util.Map;

public record Result(List<Map<String, Object>> records,
                     int total
) {
}
