package com.example.qld_roadcrash_service.client;

import com.example.qld_roadcrash_service.model.CrashSummary;
import com.example.qld_roadcrash_service.model.QldResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class CrashApiClient {

    private final WebClient webClient;


    @Value("${qld.api.path}")
    private String apiPath;


    @Value("${qld.api.resource-id}")
    public String resourceId;

    public CrashApiClient(WebClient webClient) {

        this.webClient = webClient;
    }

    public QldResponse fetchCrashData(int limit, int offset) {

        return webClient.get()
                .uri(uriBuilder -> {
                    URI uri = uriBuilder
                            .scheme("https")
                            .host("www.data.qld.gov.au")
                            .path(apiPath)
                            .queryParam("resource_id", resourceId)
                            .queryParam("limit", limit)
                            .queryParam("offset", offset)
                            .build();

                    System.out.println("Final API URL = " + uri);

                    return uri;
                })
                .retrieve()
                .bodyToMono(QldResponse.class)
                .block();
    }

}
