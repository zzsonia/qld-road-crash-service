package com.example.qld_roadcrash_service.service;

import com.example.qld_roadcrash_service.client.CrashApiClient;
import com.example.qld_roadcrash_service.model.CrashSummary;
import com.example.qld_roadcrash_service.model.QldResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CrashService {

    private final CrashApiClient crashApiClient;

    private static final Logger log = LoggerFactory.getLogger(CrashService.class);

    public CrashService(CrashApiClient crashApiClient) {
        this.crashApiClient = crashApiClient;
    }

    @Cacheable("qldCrashData")
    public List<CrashSummary> fetchAllCrashSummaries() {

        int limit = 100;
        int offset = 0;

        List<CrashSummary> allCrash = new ArrayList<>();
        log.info("🔥 Cache MISS → Fetching crash data from API");
        while (true) {

            QldResponse response = crashApiClient.fetchCrashData(limit, offset);

            var result = response.result();
            var records = result.records();

            var batch = records.stream()
                    .map(record -> new CrashSummary(
                            (String) record.get("Crash_Type"),
                            (String) record.get("Crash_Month"),
                            (String) record.get("Loc_Suburb"),
                            (String) record.get("Crash_Severity")
                    ))
                    .toList();
            // accumulate batches instead of overwriting
            allCrash.addAll(batch);

            int fetched = records.size();
            int total = result.total();
            // stop when we've fetched all records or received an empty batch
            if (fetched == 0 || offset + fetched >= total) {
                break;
            }

            offset += limit;
        }
        return allCrash;
    }

    public List<CrashSummary> fetchByfilter(List<CrashSummary> allCrashes, String location) {

        return allCrashes.stream().filter(c->c.suburb().equals(location)).toList();
    }

    public List<CrashSummary> fetchBySortedOrder(List<CrashSummary> allCrashes, String sortBy
    ) {

        return allCrashes.stream().sorted(Comparator.comparing(c->switch (sortBy){

            case "location" ->c.suburb();
            case "severity" -> c.severity();
            default -> c.severity();
         })).toList();
    }

}
