package com.combain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nestwave.device.model.HybridNavParameters;
import com.nestwave.device.service.GnssServiceResponse;
import com.nestwave.model.GnssPositionResults;
import com.nestwave.service.PartnerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static org.apache.tomcat.util.codec.binary.Base64.encodeBase64String;

@Slf4j
public class CombainService implements PartnerService {

    public final String uri = "https://apiv2.combain.com";

    private final RestTemplate restTemplate;
    public final ObjectMapper objectMapper;


    CombainService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper.configure(ALLOW_UNQUOTED_FIELD_NAMES, true).
                setSerializationInclusion(NON_NULL).
                enable(INDENT_OUTPUT);
    }


    public <T> ResponseEntity<T> combainAPI(HybridNavParameters hybridNavParameters, Class<T> responseType) {
        ResponseEntity<T> responseEntity;
        HttpEntity<?> requestEntity = new HttpEntity<>(hybridNavParameters);
        String uri;
        String strResponse;

        responseEntity = null;
        for (int P = 0; P<3; P++) {
            try {
                responseEntity = restTemplate.postForEntity(this.uri, requestEntity, responseType);
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

    @Override
    public GnssServiceResponse onGnssPosition(int customerId, long deviceId, long IMEI, GnssPositionResults gnssPositionResults) {
        return null;
    }
}
