package com.example.qld_roadcrash_service.client;

import com.example.qld_roadcrash_service.model.QldResponse;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Service
public class CrashApiClient {

    private final WebClient webClient;

    private static final Logger log = LoggerFactory.getLogger(CrashApiClient.class);

    @Value("${qld.api.base-url:https://www.data.qld.gov.au}")
    private String baseUrl;

    @Value("${qld.api.path}")
    private String apiPath;


    @Value("${qld.api.resource-id}")
    public String resourceId;

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    public CrashApiClient(WebClient webClient) {

        this.webClient = webClient;
    }

    public QldResponse fetchCrashData(int limit, int offset) {

        // Build the full URI from configured baseUrl and path
        URI uri = UriComponentsBuilder.fromUriString(baseUrl)
                .path(apiPath)
                .queryParam("resource_id", resourceId)
                .queryParam("limit", limit)
                .queryParam("offset", offset)
                .build()
                .toUri();

        log.info("Final API URL = {}", uri);

        try {
            Mono<QldResponse> mono = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(QldResponse.class)
                    .timeout(REQUEST_TIMEOUT)
                    .retryWhen(Retry.backoff(2, Duration.ofMillis(200)).filter(throwable -> {
                        log.warn("Retrying due to: {}", throwable.toString());
                        return true;
                    }));

            QldResponse response = mono.block();

            if (response == null) {
                throw new IllegalStateException("API returned null response.");
            }

            if (response.result() == null || response.result().records().isEmpty()) {
                throw new NoSuchElementException("No crash data returned from API.");
            }

            return response;
        }  catch (Exception e) {
            log.error("Failed to fetch crash data from {}: {}", uri, e.toString());
            throw new IllegalStateException("Failed to fetch crash data", e);
        }
    }

}
