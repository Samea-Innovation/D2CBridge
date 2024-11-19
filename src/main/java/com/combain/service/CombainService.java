package com.combain.service;

import com.combain.model.CombainRequest;
import com.combain.model.CombainResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

@Slf4j
@Service
public class CombainService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final String token;
    private final String uriBase;

    private final Boolean usable;

    private final Environment environment;

    public CombainService(
            RestTemplate restTemplate,
            @Value("${partners.combain.url}")
            String uriBase,
            ObjectMapper objectMapper,
            Environment environment
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper.
                configure(ALLOW_UNQUOTED_FIELD_NAMES, true).
                setSerializationInclusion(NON_NULL).
                enable(INDENT_OUTPUT);
        this.environment = environment;
        this.uriBase = uriBase;
        this.token = environment.getProperty("partners.combain.token");

        this.usable = token != null && !token.isEmpty();
        if (this.usable)
            log.info("Registered Combain Service");
    }

    public Boolean isUsable() {
        return this.usable;
    }

    public CombainResponse locate(CombainRequest requestBody) {
        String url = this.uriBase + this.token;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String json;
        try {
            json = objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        log.info(json);
        HttpEntity<String> request = new HttpEntity<>(json, headers);

        ResponseEntity<String> responseEntity = null;
        json = null;
        int tryCount = 0;
        int maxTries = 3;
        while (json == null) {
            try {
                responseEntity = restTemplate.postForEntity(url, request, String.class);

                log.info("Answer Combain:");
                log.info("Status Code: {}", responseEntity.getStatusCode());

                json = responseEntity.getBody();

            } catch (HttpClientErrorException e) {
                log.info("Answer Combain:");
                log.info("Status Code: {}", e.getStatusCode());
                switch (e.getStatusCode()) {
                    case BAD_REQUEST:
                    case NOT_FOUND:
                    case FORBIDDEN:
                    case INTERNAL_SERVER_ERROR:
                        json = e.getResponseBodyAsString();
                        break;
                    default:
                        log.error(e.getMessage());
                        if (++tryCount == maxTries) throw e;
                        break;
                }
            }
        }
        log.info("Body: {}", json);

        try {
            return objectMapper.readValue(json, CombainResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
