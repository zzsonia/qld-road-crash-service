package com.example.qld_roadcrash_service.client;

import com.example.qld_roadcrash_service.model.CrashSummary;
import com.example.qld_roadcrash_service.model.QldResponse;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.NoSuchElementException;
import org.slf4j.Logger;



@Service
public class CrashApiClient {

    private final WebClient webClient;

    private static final Logger log = LoggerFactory.getLogger(CrashApiClient.class);


    @Value("${qld.api.path}")
    private String apiPath;


    @Value("${qld.api.resource-id}")
    public String resourceId;

    public CrashApiClient(WebClient webClient) {

        this.webClient = webClient;
    }

    public QldResponse fetchCrashData(int limit, int offset) {



        URI uri = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("www.data.qld.gov.au")
                .path(apiPath)
                .queryParam("resource_id", resourceId)
                .queryParam("limit", limit)
                .queryParam("offset", offset)
                .build()
                .toUri();

        log.info("Final API URL = {}", uri);

        QldResponse response = webClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class)
                                .map(body -> new RuntimeException("API error: " + body))
                )
                .bodyToMono(QldResponse.class)
                .timeout(Duration.ofSeconds(10))
                .block();

        if (response == null) {
            throw new IllegalStateException("API returned null response.");
        }

        if (response.result() == null || response.result().records().isEmpty()) {
            throw new NoSuchElementException("No crash data returned from API.");
        }

        return response;
    }

}
