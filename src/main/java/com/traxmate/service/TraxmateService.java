/******************************************************************************
 * Copyright 2022 - NESTWAVE SAS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 *****************************************************************************/
package com.traxmate.service;

import com.nestwave.service.PartnerService;
import com.nestwave.device.service.GnssServiceResponse;
import com.nestwave.device.service.NavigationService;
import com.nestwave.model.GnssPositionResults;
import com.traxmate.model.TraxmateCapture;
import com.traxmate.util.TraxmateSubmitDataMapper;
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

import java.util.List;

import static java.lang.Long.toUnsignedString;
import static org.springframework.http.HttpStatus.CONTINUE;

@Slf4j
@Service
public class TraxmateService implements PartnerService {
	@Autowired
	private RestTemplate restTemplate;
	@Value("${partners.traxmate.url}")
	private String url;
	@Value("${partners.traxmate.api.submitPosition}")
	private String endpoint;

	@Value("${partners.traxmate.token}")
	private String token;

	@Value("${partners.traxmate.customerIdList}")
	private List<Integer> customerIdList;

	@Autowired
	private TraxmateSubmitDataMapper mapper;

	@Autowired
	private NavigationService navigationService;

	public TraxmateService() {}

	@PostConstruct
	public void initialize() {
		if(token == null || token.isEmpty())
			return;
		log.info("Registering Traxmate service.");

		if (url == null || url.isEmpty()) {
			log.error("No URL supplied at startup!");
			return;
		}
		if (endpoint == null || endpoint.isEmpty()) {
			log.warn("No endpoint supplied at startup! Using '@{deviceId}'.");
			endpoint = "@{deviceId}";
		}
		if (!endpoint.contains("@{deviceId}")) {
			log.error("Endpoint '{}' needs to contain '@{deviceId}'!", endpoint);
			return;
		}
		// Enable the plugin by registering it.
		navigationService.register(this);
	}

	@Override
	public GnssServiceResponse onGnssPosition(
			int customerId,
			long deviceId,
			long IMEI,
			GnssPositionResults gnssPositionResults
	) {
		if (!customerIdList.contains(customerId))
			return new GnssServiceResponse(CONTINUE, "Not for us!".getBytes());
		log.debug("Customer ID: {}", customerId);

		TraxmateCapture data = mapper.mapData(deviceId, IMEI, customerId, gnssPositionResults);

		ResponseEntity<byte[]> response = submitData(deviceId, data);

		return new GnssServiceResponse(response.getStatusCode(), response.getBody());
	}

	private ResponseEntity<byte[]> submitData(long deviceId, TraxmateCapture data) {
		log.info("Submitting data to '{}'", this.url);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<TraxmateCapture> requestEntity = new HttpEntity<>(data, headers);

		String uri = url + endpoint.replace("@{deviceId}", toUnsignedString(deviceId));
		log.debug(uri);

		return restTemplate.postForEntity(uri, requestEntity, byte[].class, data);
	}
}
