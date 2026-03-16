package com.example.qld_roadcrash_service.controller;

import com.example.qld_roadcrash_service.client.CrashApiClient;
import com.example.qld_roadcrash_service.exception.GlobalExceptionHandler;
import com.example.qld_roadcrash_service.model.CrashSummary;
import com.example.qld_roadcrash_service.model.QldResponse;
import com.example.qld_roadcrash_service.service.CrashService;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
public class CrashController {

    private final CrashApiClient crashApiClient;

    private final CrashService crashService;

    public CrashController(CrashApiClient crashApiClient, CrashService crashService){
        this.crashApiClient=crashApiClient;
        this.crashService=crashService;

    }

    @GetMapping("/crashes")
        public QldResponse getCrashes(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size){
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Page must be >= 0 and size must be > 0");
        }
        int offset = page * size;

            return crashApiClient.fetchCrashData(size, offset);
    }

    @GetMapping("/crashes/all")
    public List<CrashSummary> getAllCrashes() {
        return crashService.fetchAllCrashSummaries();
    }

    @GetMapping("/crashes/locations")
    public List<CrashSummary> getCrashByLocation(@RequestParam String location){

        if(location==null||location.isEmpty()){
            throw new IllegalArgumentException("Location parameter is required and cannot be empty.");
        }
        var allCrashes = crashService.fetchAllCrashSummaries();
        var filtered= crashService.fetchByfilter(allCrashes,location);

        if (filtered.isEmpty()) {
            throw new NoSuchElementException("No crashes found for location: " + location);
        }
        return filtered;
    }

    @GetMapping("/crashes/sorting")
    public List<CrashSummary> getSorting(@RequestParam(defaultValue = "severity") String sortBy
        ){

        validateSortBy(sortBy);
        var allCrashes = crashService.fetchAllCrashSummaries();
        return crashService.fetchBySortedOrder(allCrashes,sortBy);
    }

    private static final List<String> ALLOWED_SORTED_FIELDS= List.of("severity","month");

    private void validateSortBy(String sortBy){
        if(!ALLOWED_SORTED_FIELDS.contains(sortBy.toLowerCase())){
            throw new IllegalArgumentException("Invalid sortBy parameter. Allowed values are: " + ALLOWED_SORTED_FIELDS);
        }
    }
}
