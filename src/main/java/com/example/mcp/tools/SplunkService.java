package com.example.mcp.tools;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;


@Service
public class SplunkService {

    private final WebClient restClient; // 8089 – normal Splunk REST
    private final WebClient hecClient;  // 8088 – HEC
    private final Duration timeout;
    private final boolean useHec;

    public SplunkService(
            @Value("${splunk.host}") String host,                  // e.g. https://127.0.0.1:8089
            @Value("${splunk.token}") String token,                // MCP/REST JWT
            @Value("${splunk.hec.host:https://127.0.0.1:8088}") String hecHost,
            @Value("${splunk.hec.token}") String hecToken,         // HEC token from Splunk UI
            @Value("${splunk.use-hec:false}") boolean useHec,
            @Value("${splunk.timeout-seconds:30}") long timeoutSeconds) {

        this.useHec = useHec;
        this.timeout = Duration.ofSeconds(timeoutSeconds);

        HttpClient insecureClient = createInsecureHttpClient();

        this.restClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(insecureClient))
                .baseUrl(host)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();

        this.hecClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(insecureClient))
                .baseUrl(hecHost)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Splunk " + hecToken)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    private HttpClient createInsecureHttpClient() {
        try {
            SslContext sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();

            return HttpClient.create().secure(sslSpec -> sslSpec.sslContext(sslContext));
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure insecure SSL", e);
        }
    }

    public Mono<String> listIndexes() {
        return restClient.get()
            .uri("/services/data/indexes?output_mode=json")
            .retrieve()
            .bodyToMono(JsonNode.class)
            .map(json -> {
                List<String> names = new ArrayList<>();
                json.path("entry").forEach(entry ->
                    names.add(entry.path("name").asText())
                );
                return String.join(", ", names);
            })
            .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> createSearchJob(String query) {
        return restClient.post()
                .uri("/services/search/jobs")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("search=" + query)
                .retrieve()
                .bodyToMono(String.class)
                .map(xml -> {
                    int s = xml.indexOf("<sid>") + 5;
                    int e = xml.indexOf("</sid>");
                    if (s < 5 || e < 0) return "SID_NOT_FOUND";
                    return xml.substring(s, e);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> getSearchResults(String sid) {
        return restClient.get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/services/search/jobs/{sid}/results")
                                .queryParam("output_mode", "json")
                                .build(sid)
                )
                .retrieve()
                .bodyToMono(String.class)
                .subscribeOn(Schedulers.boundedElastic());
    }


    public Mono<String> sendEvent(String index, String event) {

        if (!useHec) {
            return Mono.error(new IllegalStateException("HEC disabled."));
        }

        Map<String, Object> payload = Map.of(
                "index", index,
                "event", event
        );

        return hecClient.post()
                .uri("/services/collector/event")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .subscribeOn(Schedulers.boundedElastic());
    }

}

