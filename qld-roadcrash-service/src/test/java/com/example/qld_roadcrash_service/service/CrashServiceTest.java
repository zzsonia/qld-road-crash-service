package com.example.qld_roadcrash_service.service;

import com.example.qld_roadcrash_service.client.CrashApiClient;
import com.example.qld_roadcrash_service.model.QldResponse;
import com.example.qld_roadcrash_service.model.Result;
import com.example.qld_roadcrash_service.model.CrashSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CrashServiceTest {

    private CrashApiClient mockClient;
    private CrashService crashService;

    @BeforeEach
    void setUp() {
        mockClient = Mockito.mock(CrashApiClient.class);
        crashService = new CrashService(mockClient);
    }

    @Test
    void fetchAllCrashSummaries_accumulatesMultiplePages() {
        // prepare two batches of records
        var rec1 = List.<Map<String, Object>>of(Map.<String, Object>of(
                "Crash_Type", "TypeA",
                "Crash_Month", "Jan",
                "Loc_Suburb", "Suburb1",
                "Crash_Severity", "High"
        ));

        var rec2 = List.<Map<String, Object>>of(Map.<String, Object>of(
                "Crash_Type", "TypeB",
                "Crash_Month", "Feb",
                "Loc_Suburb", "Suburb2",
                "Crash_Severity", "Low"
        ));

        QldResponse page1 = new QldResponse(true, new Result(rec1, 2));
        QldResponse page2 = new QldResponse(true, new Result(rec2, 2));

        Mockito.when(mockClient.fetchCrashData(100, 0)).thenReturn(page1);
        Mockito.when(mockClient.fetchCrashData(100, 100)).thenReturn(page2);

        List<CrashSummary> summaries = crashService.fetchAllCrashSummaries();

        assertNotNull(summaries);
        assertEquals(2, summaries.size());

        assertTrue(summaries.stream().anyMatch(s -> s.suburb().equals("Suburb1") && s.type().equals("TypeA")));
        assertTrue(summaries.stream().anyMatch(s -> s.suburb().equals("Suburb2") && s.type().equals("TypeB")));
    }
}
