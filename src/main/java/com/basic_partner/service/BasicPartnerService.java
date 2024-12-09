package com.basic_partner.service;

import com.basic_partner.model.BasicPartnerPostData;
import com.basic_partner.util.BasicPartnerDataMapper;
import com.nestwave.device.service.GnssServiceResponse;
import com.nestwave.device.service.NavigationService;
import com.nestwave.model.GnssPositionResults;
import com.nestwave.service.PartnerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class BasicPartnerService implements PartnerService {

    @Autowired
    private RestTemplate restTemplate;
    @Value("${partners.basic.url}")
    private String url;

    @Autowired
    private NavigationService navigationService;

    @Autowired
    private BasicPartnerDataMapper mapper;

    public BasicPartnerService() {}

    @PostConstruct
    public void initialize() {
        /*
            Insert code needed at service startup.
        */

        // Enable the plugin by registering it.
        if(url != null && !url.isEmpty()) {
            log.info("Basic Service enabled. URL: {}", url);
            navigationService.register(this);
        }
    }

    @Override
    public GnssServiceResponse onGnssPosition(int customerId, long deviceId, long IMEI, GnssPositionResults gnssPositionResults) {
        BasicPartnerPostData data = mapper.mapPostData(deviceId, IMEI, customerId, gnssPositionResults);

        ResponseEntity<byte[]> response = submitData(data);

        return new GnssServiceResponse(response.getStatusCode(), response.getBody());
    }

    private ResponseEntity<byte[]> submitData(BasicPartnerPostData data) {
        log.info("Submitting data to '{}'", this.url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<BasicPartnerPostData> requestEntity = new HttpEntity<>(data, headers);

        return restTemplate.postForEntity(this.url, requestEntity, byte[].class, data);
    }
}
