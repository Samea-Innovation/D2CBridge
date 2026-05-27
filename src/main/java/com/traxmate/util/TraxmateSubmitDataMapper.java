package com.traxmate.util;

import com.nestwave.device.repository.thintrack.ThinTrackPlatformBarometerStatusRecord;
import com.nestwave.model.GnssPositionResults;
import com.traxmate.model.Location;
import com.traxmate.model.TraxmateCapture;
import com.traxmate.model.TraxmateProperties;
import com.traxmate.model.TraxmateSignals;
import org.springframework.stereotype.Component;

@Component
public class TraxmateSubmitDataMapper {
    private final static int THINTRACK_TYPE_ID = 35976;

    private int toSafeAccuracy(float confidence) {
        if (!Float.isFinite(confidence)) {
            return 0;
        }
        if (confidence > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (confidence < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) confidence;
    }

    public TraxmateCapture mapData(long deviceId, long imei, int customerId, GnssPositionResults gnssPositionResults) {
        String name = "Thintrack - " + deviceId;

        TraxmateProperties properties = new TraxmateProperties();

        properties.setConnectivity("LTE");

        TraxmateSignals signals = new TraxmateSignals();

        if(gnssPositionResults.thintrackPlatformStatus != null) {
            signals.setTemperature(
                    (float) gnssPositionResults.thintrackPlatformStatus.getAmbientTemperature());

            signals.setBatteryLevel(
                    gnssPositionResults.thintrackPlatformStatus.getBatteryChargeLevel());
            signals.setBatteryTemperature(
                    (float) gnssPositionResults.thintrackPlatformStatus.getBatteryTemperature());

            signals.setShockCount(
                    gnssPositionResults.thintrackPlatformStatus.getShocksCount());

            if (gnssPositionResults.thintrackPlatformStatus.getClass() == ThinTrackPlatformBarometerStatusRecord.class) {
                ThinTrackPlatformBarometerStatusRecord barometerStatusRecord =
                        (ThinTrackPlatformBarometerStatusRecord) gnssPositionResults.thintrackPlatformStatus;

                signals.setBarometerMeasureCount(barometerStatusRecord.getBarometerMeasurementsCount());
                signals.setBarometerAverage(barometerStatusRecord.getBarometerMeasurementsAverage());
                signals.setBarometerVariance(barometerStatusRecord.getBarometerMeasurementsVariance());
                signals.setBarometerMin(barometerStatusRecord.getBarometerMeasurementsMin());
                signals.setBarometerMax(barometerStatusRecord.getBarometerMeasurementsMax());
                signals.setBarometerTemperature(barometerStatusRecord.getBarometerMeasurementsTemperature());
            }
        }

        signals.setPositionTime(gnssPositionResults.utcTime);

        signals.setPositioning(gnssPositionResults.technology);

        signals.setAccuracy(toSafeAccuracy(gnssPositionResults.confidence));
        signals.setAltitude(gnssPositionResults.position[2]);
        signals.setHat(gnssPositionResults.HeightAboveTerrain);
        signals.setLocation(new Location(
                gnssPositionResults.position[1],    // Latitude
                gnssPositionResults.position[0]     // Longitude
        ));

        signals.setConnectionSignalStrength(gnssPositionResults.rssi);

        return new TraxmateCapture(name, THINTRACK_TYPE_ID, properties, signals, gnssPositionResults);
    }
}
