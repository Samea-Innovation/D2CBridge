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
package com.nestwave.device.service;

import com.combain.model.CombainRequest;
import com.combain.model.CombainResponse;
import com.combain.service.CombainService;
import com.combain.util.mapper.HybridNavigationParametersWithCombainRequestMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nestwave.device.model.*;
import com.nestwave.device.repository.position.PositionRecord;
import com.nestwave.device.repository.position.PositionRepository;
import com.nestwave.device.repository.thintrack.ThinTrackPlatformBarometerStatusRecord;
import com.nestwave.device.repository.thintrack.ThinTrackPlatformStatusRecord;
import com.nestwave.device.repository.thintrack.ThinTrackPlatformStatusRepository;
import com.nestwave.device.util.JwtTokenUtil;
import com.nestwave.model.GnssPositionResults;
import com.nestwave.model.Payload;
import com.nestwave.service.PartnerService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.List;

import static java.util.Arrays.copyOf;
import static org.springframework.http.HttpStatus.*;

@Slf4j
@Service
public class NavigationService extends GnssService{
	private final PositionRepository positionRepository;
	private final ThinTrackPlatformStatusRepository thintrackPlatformStatusRepository;
	private PartnerService[] partnerServices;

	private final CombainService combainService;

	public NavigationService(JwtTokenUtil jwtTokenUtil,
                             PositionRepository positionRepository,
                             ThinTrackPlatformStatusRepository thintrackPlatformStatusRepository,
                             @Value("${navigation.base_url}") String uri,
                             ObjectMapper objectMapper,
                             RestTemplate restTemplate,
							 CombainService combainService
    ) {
		super(jwtTokenUtil, uri, restTemplate, objectMapper);
		this.positionRepository = positionRepository;
		this.thintrackPlatformStatusRepository = thintrackPlatformStatusRepository;
        this.combainService = combainService;
        partnerServices = new PartnerService[0];
  }

	public boolean supports(String apiVer){
		return apiVer != null && apiVer.compareTo("v1.4") >= 0;
	}

	public void register(PartnerService partnerService){
		int len = partnerServices.length;

		partnerServices = copyOf(partnerServices, len + 1);
		partnerServices[len] = partnerService;
	}

	public GnssServiceResponse gnssPosition(String apiVer, byte[] rawResults, String clientIpAddr, boolean noc){
		NavigationParameters navigationParameters;
		Payload payload;
	    GnssServiceResponse response;
	    String api;

	    if(noc){
		    api = "gnssPosition";
	    }else{
		    api = "gnssDevicePosition";
	    }
		if(apiVer.compareTo("v1.7") < 0){
			payload = new Payload(rawResults, 4);
		}else{
			payload = new Payload(rawResults);
		}
		navigationParameters = new NavigationParameters(payload);
		ResponseEntity<GnssPositionResults> responseEntity = remoteApi(apiVer, api, navigationParameters, clientIpAddr, GnssPositionResults.class);
		byte[] jsonResponse = serializeResponse(responseEntity);
		if(jsonResponse == null){
			response = new GnssServiceResponse(INTERNAL_SERVER_ERROR, "Cloud not serialize navigation results:\n" + responseEntity);
		}else{
			GnssPositionResults navResults = responseEntity.getBody();
			response = savePosition(payload, navResults);
		}
		return response;
    }

	public GnssServiceResponse dropPositionsFromDatabase(long deviceId)
	{
		log.debug("Drop all positions for deviceId = {}", deviceId);
		positionRepository.dropAllPositionRecordsWithId(deviceId);
		return new GnssServiceResponse(HttpStatus.OK, (byte[])null);
	}

	public GnssServiceResponse dropPlatformStatusFromDatabase(long deviceId)
	{
		log.debug("Drop all platform status records for deviceId = {}", deviceId);
		thintrackPlatformStatusRepository.dropAllRecordsWithId(deviceId);
		return new GnssServiceResponse(HttpStatus.OK, (byte[])null);
	}

	public GnssServiceResponse locate(String apiVer, Payload payload, String clientIpAddr){
		HybridNavPayload hybridNavPayload;
		HybridNavigationParameters hybridNavigationParameters;
		ResponseEntity<GnssPositionResults> responseEntity;
		GnssServiceResponse response;
		GnssPositionResults navResults = null;
		String api = "locate";

		try{
			hybridNavPayload = new HybridNavPayload(payload);
		}catch(InvalidHybridNavPayloadException e){
			return new GnssServiceResponse(NOT_ACCEPTABLE, e.getMessage());
		}
		ThinTrackPlatformStatusRecord[] thinTrackPlatformStatusRecords = ThinTrackPlatformStatusRecord.of(payload.deviceId, null, hybridNavPayload);
		hybridNavigationParameters = new HybridNavigationParameters(payload, hybridNavPayload, thinTrackPlatformStatusRecords);
		for(ThinTrackPlatformStatusRecord record : thinTrackPlatformStatusRecords){
			if(record instanceof ThinTrackPlatformBarometerStatusRecord){
				String[] features = {"PAAN"};
				hybridNavigationParameters.features = features;
				break;
			}
		}
		try{
			log.info("hybridNavigationParameters = {}", objectMapper.writeValueAsString(hybridNavigationParameters));
		}catch(Exception e){
			log.error("Error when processing JSON: {}", e.getMessage());
		}
		responseEntity = remoteApi(apiVer, api, hybridNavigationParameters, clientIpAddr, GnssPositionResults.class);
		byte[] jsonResponse = serializeResponse(responseEntity);
		if(jsonResponse == null){
			return new GnssServiceResponse(INTERNAL_SERVER_ERROR, "Cloud not serialize navigation results:\n" + responseEntity);
		}
		navResults = responseEntity.getBody();
		for(ThinTrackPlatformStatusRecord thinTrackPlatformStatusRecord : thinTrackPlatformStatusRecords){
			thinTrackPlatformStatusRecord.setKey(payload.deviceId, navResults.utcTime);
			log.info("ThinkTrack platform status: {}", thinTrackPlatformStatusRecord);
			if(thinTrackPlatformStatusRecord != null){
				navResults.thintrackPlatformStatus = thintrackPlatformStatusRepository.insertNewRecord(thinTrackPlatformStatusRecord);
			}
		}
		response = savePosition(payload, navResults);
		if(response.status == OK && response.message != null){
			response = new GnssServiceResponse(OK, hybridNavPayload.addTechno(navResults.technology, response.message));
		}
		return response;
	}

	public GnssServiceResponse sameaLocate(String apiVer, Payload payload, String clientIpAddr) {
		// NextNav parameters
		HybridNavPayload hybridNavPayload;
		HybridNavigationParameters hybridNavigationParameters;
        try {
			hybridNavPayload = new HybridNavPayload(payload);
		} catch(InvalidHybridNavPayloadException e) {
			return new GnssServiceResponse(NOT_ACCEPTABLE, e.getMessage());
		}
		ThinTrackPlatformStatusRecord[] thinTrackPlatformStatusRecords = ThinTrackPlatformStatusRecord.of(payload.deviceId, null, hybridNavPayload);
		hybridNavigationParameters = new HybridNavigationParameters(payload, hybridNavPayload, thinTrackPlatformStatusRecords);
		for (ThinTrackPlatformStatusRecord record : thinTrackPlatformStatusRecords) {
			if (record instanceof ThinTrackPlatformBarometerStatusRecord) {
				String[] features = {"PAAN"};
				hybridNavigationParameters.features = features;
				break;
			}
		}
		try {
			log.info("hybridNavigationParameters = {}", objectMapper.writeValueAsString(hybridNavigationParameters));
		} catch(Exception e) {
			log.error("Error when processing JSON: {}", e.getMessage());
		}

		// Get NextNav positioning
		String nextnavEndpoint = "gnssPosition";
        ResponseEntity<GnssPositionResults> responseEntity;
		GnssPositionResults navResults = null;
		GnssServiceResponse response;

		if (hybridNavigationParameters.rawMeas != null && hybridNavigationParameters.rawMeas.length != 0)
			try {
				responseEntity = remoteApi(apiVer, nextnavEndpoint, hybridNavigationParameters, clientIpAddr, GnssPositionResults.class);
				byte[] jsonResponse = serializeResponse(responseEntity);
				if (jsonResponse == null) {
					throw new NullPointerException("Cloud not serialize navigation results:\n" + responseEntity);
//					response = new GnssServiceResponse(INTERNAL_SERVER_ERROR, "Cloud not serialize navigation results:\n" + responseEntity);
				}
				navResults = responseEntity.getBody();
				if (navResults != null) navResults.technology = "GNSS";
				log.info("NextNav answer: {}", navResults);
			} catch (Exception e) {
				log.error(e.getMessage());
			}

		// Get partners positioning
		boolean hasHybrid = hybridNavigationParameters.hybrid != null &&
				(hybridNavigationParameters.hybrid.cellTowers != null
				|| hybridNavigationParameters.hybrid.wifiAccessPoints != null
				|| hybridNavigationParameters.hybrid.bluetoothBeacons != null);

		// pas de réponse nextnav ?
		// pas de précision ? bluetooth: 50, wifi: 100, cell: 500
		// hybrid ?
		if (hasHybrid && combainService.isUsable()) {
			log.info("Payload has Hybrid");

			boolean helpNeeded = navResults == null || isNotPreciseEnough(hybridNavigationParameters.hybrid, navResults.confidence);
			if (helpNeeded) {
				log.info("Trying Combain");
				// Appel Combain

				CombainRequest combainRequest = HybridNavigationParametersWithCombainRequestMapper.toCombainRequest(hybridNavigationParameters.hybrid);

				if (navResults != null && navResults.confidence < 2500) {
					CombainRequest.Gps gps = new CombainRequest.Gps();
					gps.setLatitude(navResults.position[1]);
					gps.setLongitude(navResults.position[0]);
					gps.setAltitude(((int) navResults.position[2]));
					gps.setAccuracy((int) navResults.confidence);
					gps.setSat(navResults.gps.prn.length);
					gps.setAge(0);
					combainRequest.setGps(gps);
				}

				CombainResponse combainResponse = combainService.locate(combainRequest);

				if (combainResponse.getLocation() != null && combainResponse.getError() == null) {
					log.info(combainResponse.toString());

					GnssPositionResults combainResults = new GnssPositionResults();
					CombainResponse.Location location = combainResponse.getLocation();
					combainResults.confidence = combainResponse.getAccuracy();
					combainResults.HeightAboveTerrain = 0;
					combainResults.position = new float[]{location.getLng(), location.getLat(), combainResponse.getAltitude() != null ? combainResponse.getAltitude() : 0};
					combainResults.technology = "";

					Object[] cellTowers = hybridNavigationParameters.hybrid.cellTowers;
					if (cellTowers != null && cellTowers.length != 0) {
						if (!combainResults.technology.isEmpty())
							combainResults.technology += "/";
						combainResults.technology += "Cellular";
					}
					Object[] wifiAccessPoints = hybridNavigationParameters.hybrid.wifiAccessPoints;
					if (wifiAccessPoints != null && wifiAccessPoints.length != 0) {
						if (!combainResults.technology.isEmpty())
							combainResults.technology += "/";
						combainResults.technology += "Wi-Fi";
					}
					Object[] bluetoothBeacons = hybridNavigationParameters.hybrid.bluetoothBeacons;
					if (bluetoothBeacons != null && bluetoothBeacons.length != 0) {
						if (!combainResults.technology.isEmpty())
							combainResults.technology += "/";
						combainResults.technology += "Bluetooth";
					}

					combainResults.utcTime = ZonedDateTime.now();
					combainResults.velocity = new float[3];

					if (navResults == null) {
						navResults = combainResults;

						log.info("Results = Combain: {}", navResults);

					} else if (navResults.confidence > combainResults.confidence) {
						navResults.confidence = combainResults.confidence;
						if (combainRequest.getCellTowers() != null)
							navResults.rssi = combainRequest.getCellTowers().get(0).getSignalStrength();
						navResults.position = combainResults.position;
						navResults.technology = combainResults.technology;
						if (navResults.utcTime == null)
							navResults.utcTime = combainResults.utcTime;

						log.info("Results = NextNav + Combain: {}", navResults);
					} else {
						log.info("Results = NextNav");
					}

				} else {
					log.error("Combain Error: {}", combainResponse);
				}
			} else {
				Object cell = hybridNavigationParameters.hybrid.cellTowers[0];

				log.info(cell.toString());
				Class<?> c = cell.getClass();

				try {
					Field field;

					field = c.getField("rsrp");
					field.setAccessible(true);
					navResults.rssi = (int) field.get(cell);

				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}

		// Results saving
		boolean validResults = navResults != null;
		if (!validResults) {
			navResults = new GnssPositionResults();
			navResults.confidence = 65535;
			navResults.gpsTime = 0;
			navResults.HeightAboveTerrain = 0;
			navResults.payload = new byte[] {};
			navResults.position = new float[3];
			navResults.technology = "None";
			navResults.utcTime = ZonedDateTime.now();
			navResults.velocity = new float[3];
		}

		for (ThinTrackPlatformStatusRecord thinTrackPlatformStatusRecord : thinTrackPlatformStatusRecords) {
			thinTrackPlatformStatusRecord.setKey(payload.deviceId, navResults.utcTime);
			log.info("ThinkTrack platform status: {}", thinTrackPlatformStatusRecord);
            navResults.thintrackPlatformStatus = thintrackPlatformStatusRepository.insertNewRecord(thinTrackPlatformStatusRecord);
        }
		response = savePosition(payload, navResults);
		if (response.status == OK && response.message != null) {
			response = new GnssServiceResponse(!validResults ? NOT_FOUND : OK, hybridNavPayload.addTechno(navResults.technology, response.message));
		}
		log.info("Final results: {}", navResults);
		return response;
	}

	private static boolean isNotPreciseEnough(HybridNavParameters hybrid, float confidence) {
		float confidenceToHave = confidence;

		Object[] cellTowers = hybrid.cellTowers;
		Object[] wifiAccessPoints = hybrid.wifiAccessPoints;
		Object[] bluetoothBeacons = hybrid.bluetoothBeacons;

		if (cellTowers != null && cellTowers.length != 0)
			confidenceToHave = 500;
		if (wifiAccessPoints != null && wifiAccessPoints.length != 0)
			confidenceToHave = 100;
		if (bluetoothBeacons != null && bluetoothBeacons.length != 0)
			confidenceToHave = 50;

		log.info("Confidence to have {} VS actual confidence {}", confidenceToHave, confidence);
        return confidence > confidenceToHave;
	}

	public GnssServiceResponse retrievePositionsFromDatabase(long deviceId)
	{
		String csv;

		log.debug("Query all positions for deviceId = {}", deviceId);
		csv = positionRepository.getAllPositionRecordsWithId(deviceId);
		return new GnssServiceResponse(HttpStatus.OK, csv.getBytes());
	}

	public GnssServiceResponse retrievePositionsAndPlatofrmStatusFromDatabase(long deviceId, String apiVer)
	{
		String csv;
		List<PositionRecord> positionRecords = positionRepository.findAllPositionRecordsById(deviceId);

		if(positionRecords.isEmpty()){
			return retrievePositionsFromDatabase(deviceId);
		}
		log.debug("Query all positions and status records for deviceId = {}", deviceId);
		csv = thintrackPlatformStatusRepository.getAllRecordsWithId(deviceId, positionRecords, apiVer);
		return new GnssServiceResponse(HttpStatus.OK, csv.getBytes());
	}

	public GnssServiceResponse savePosition(Payload payload, GnssPositionResults navResults){
		GnssServiceResponse response;
		int customerId = payload.customerId();
		long IMEI = payload.IMEI();
		long deviceId = payload.deviceId;
		log.info("CustomerId is: {}.\nIMEI is: {}.\n Full deviceId is: {}.", customerId, IMEI, deviceId);
		if(deviceId == 0){
			response = new GnssServiceResponse(OK, navResults.payload);
		}else{
			response = savePositionIntoDatabase(payload.deviceId, navResults);
			for(PartnerService service : partnerServices){
				GnssServiceResponse resp;
				try{
					resp = service.onGnssPosition(customerId, deviceId, IMEI, navResults);
					log.info("Partner's service {} returned status {} and content {}.", service.getClass().getName(), resp.status, new String(resp.message));
				}catch(RestClientException e){
					log.error("Unexpected partner server error:\n{}", e.getMessage());
				}
			}
		}
		return response;
	}

	public GnssServiceResponse savePositionIntoDatabase(long deviceId, GnssPositionResults navResults){
		PositionRecord positionRecord = new PositionRecord(deviceId, navResults.utcTime,
				navResults.confidence,
				navResults.position[0], navResults.position[1], navResults.position[2], navResults.HeightAboveTerrain,
				navResults.velocity[0], navResults.velocity[1], navResults.velocity[2]);

		positionRepository.insertNavigationRecord(positionRecord);
		log.info("New position inserted in positions database.");
		return new GnssServiceResponse(HttpStatus.OK, navResults.payload, navResults.gpsTime);
	}

	<T> byte[] serializeResponse(ResponseEntity<T> responseEntity){
		byte[] jsonResponse;
		T body = responseEntity.getBody();

		try{
			jsonResponse = objectMapper.writeValueAsBytes(body);
		}catch(JsonProcessingException e){
			log.error("Error when processing JSON: {}", e.getMessage());
			jsonResponse = null;
		}
		return jsonResponse;
	}
}

@Data
class NavigationParameters extends GnssServiceParameters{
	@NotNull
	@Schema(description = "GNSS raw measurements data as sent by the Iot device",
			example = "AAAAAA4AAAA=", required = true)
	public byte[] rawMeas;
	public String[] features;
	public NavigationParameters(Payload payload){
		super(payload);
		rawMeas = payload.content;
	}
}

@Data
class HybridNavigationParameters extends NavigationParameters{
	@Schema(description = "Hybrid navigation data as expected by the third party services")
	public HybridNavParameters hybrid;

	public HybridNavigationParameters(Payload payload, HybridNavPayload hybridNavPayload, ThinTrackPlatformStatusRecord[] thinTrackPlatformStatusRecord){
		super(payload);

		rawMeas = hybridNavPayload.rawMeas();
		hybrid = new HybridNavParameters(hybridNavPayload, thinTrackPlatformStatusRecord);
	}
}
