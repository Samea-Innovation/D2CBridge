package com.combain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nestwave.device.model.HybridNavParameters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static org.apache.tomcat.util.codec.binary.Base64.encodeBase64String;

@Slf4j
@Service
public class CombainService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final String token;
    private final String uriBase;

    private final Boolean usable;

    private final Environment environment;

    public CombainService(RestTemplate restTemplate,
                          @Value("${partners.combain.url}") String uriBase,
                          ObjectMapper objectMapper,
                          Environment environment
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper.configure(ALLOW_UNQUOTED_FIELD_NAMES, true).
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

    public <T> ResponseEntity<T> combainAPI(HybridNavParameters hybridNavParameters, Class<T> responseType) {
        ResponseEntity<T> responseEntity;
        HttpEntity<?> requestEntity = new HttpEntity<>(hybridNavParameters);
        String uri;
        String strResponse;

        responseEntity = null;
        for (int P = 0; P<3; P++) {
            try {
                responseEntity = restTemplate.postForEntity(url, requestEntity, responseType);
                break;
            } catch (ResourceAccessException e) {
                log.error("{}", e.getMessage());
                if(P==2){
                    throw e;
                }
            }
        }
        if(responseType == byte[].class){
            strResponse = encodeBase64String((byte[])responseEntity.getBody());
        }else{
            T response = responseEntity.getBody();
            strResponse = response.toString();
            try{
                strResponse = objectMapper.writeValueAsString(response);
            }catch(JsonProcessingException e){
                log.error("Error when processing JSON: {}", e.getMessage());
            }
        }
        log.info("Received answer: status: {}, payload: {}", responseEntity.getStatusCode(), strResponse);
        return responseEntity;
    }
}
