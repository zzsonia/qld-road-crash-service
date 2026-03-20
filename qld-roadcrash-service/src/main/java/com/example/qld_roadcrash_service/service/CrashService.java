package com.example.qld_roadcrash_service.service;

import com.example.qld_roadcrash_service.client.CrashApiClient;
import com.example.qld_roadcrash_service.model.CrashSummary;
import com.example.qld_roadcrash_service.model.QldResponse;
import com.example.qld_roadcrash_service.model.Result;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CrashService {

    private final CrashApiClient crashApiClient;

    public CrashService(CrashApiClient crashApiClient) {
        this.crashApiClient = crashApiClient;
    }


    @Cacheable("qldCrashData")
    public List<CrashSummary> fetchAllCrashSummaries() {

        int limit = 100;
        int offset = 0;

        List<CrashSummary> allCrash = new ArrayList<>();

        while (true) {


            QldResponse response = crashApiClient.fetchCrashData(limit, offset);

            System.out.println("🔥 Cache MISS → Fetching crash data from API");


            var result = response.result();
            var records = result.records();

             allCrash=records.stream()
                    .map(record -> new CrashSummary(
                            (String) record.get("Crash_Type"),
                            (String) record.get("Crash_Month"),

                            (String) record.get("Loc_Suburb"),
                            (String) record.get("Crash_Severity")
                    ))
                    .toList();




        offset+=limit;
//        if(offset >= response.result().total()){
//            break;
//        }

            if(offset >= 5000){
                break;
            }
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
