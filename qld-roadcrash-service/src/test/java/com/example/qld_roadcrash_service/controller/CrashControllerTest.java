package com.example.qld_roadcrash_service.controller;

import com.example.qld_roadcrash_service.client.CrashApiClient;
import com.example.qld_roadcrash_service.model.CrashSummary;
import com.example.qld_roadcrash_service.model.QldResponse;
import com.example.qld_roadcrash_service.model.Result;
import com.example.qld_roadcrash_service.service.CrashService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrashControllerTest {

    @Autowired
    MockMvc mockMvc;

   @InjectMocks
   private CrashController crashController;

    @Mock
    private CrashApiClient crashApiClient;

    @Mock
    private CrashService crashService;

    @Test
    void getCrashes() {

        List<Map<String, Object>> records= List.of(
                Map.of("Crash_Type", "Type A", "Crash_Month", "January", "Loc_Suburb", "Suburb A", "Crash_Severity", "High"),
                Map.of("Crash_Type", "Type B", "Crash_Month", "February", "Loc_Suburb", "Suburb B", "Crash_Severity", "Medium")
        );

        Result result= new Result(records,2);
        QldResponse qldResponse = new QldResponse(true,result);

        when(crashApiClient.fetchCrashData(100,0)).thenReturn(qldResponse);

        var response = crashController.getCrashes(0,100);
        assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    void getAllCrashes() {
    }

    @Test
    void getCrashByLocation() {

        List<CrashSummary> records= List.of(new CrashSummary(
                "Type A",  "January",  "Suburb A",  "High"),
        (new CrashSummary("Type B",  "February",  "Suburb B",  "Medium")
        ));
        when(crashService.fetchAllCrashSummaries()).thenReturn(records);

        var filtered = List.of(new CrashSummary(
                "Type A",  "January",  "Suburb A",  "High"));
        when(crashService.fetchByfilter(records,"Suburb A")).thenReturn(filtered);
        var response = crashController.getCrashByLocation("Suburb A");
        assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    void getSorting() {


        List<CrashSummary> records = List.of(new CrashSummary(
                        "Type A", "September", "Suburb B", "High"),
                (new CrashSummary("Type B", "February", "Suburb A", "Medium")
                ));
        when(crashService.fetchAllCrashSummaries()).thenReturn(records);

        var sorted = List.of(new CrashSummary(
                        "Type A", "September", "Suburb A", "High"),
                (new CrashSummary("Type B", "February", "Suburb B", "Medium")
                ));
        when(crashService.fetchBySortedOrder(records, "location")).thenReturn(sorted);
        var response = crashController.getSorting("location");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}