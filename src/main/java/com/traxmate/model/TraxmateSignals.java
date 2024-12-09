package com.traxmate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public class TraxmateSignals {
    @JsonProperty("Status")
    private String status;

    @JsonProperty("Temperature")
    private Float temperature;              // unit: °C

    @JsonProperty("Battery Level")
    private Integer batteryLevel;           // unit: %
    @JsonProperty("Battery Temperature")
    private Float batteryTemperature;       // unit: °C

    @JsonProperty("Barometer Measure Count")
    private Integer barometerMeasureCount;
    @JsonProperty("Barometer Average")
    private Float barometerAverage;         // unit: Pa
    @JsonProperty("Barometer Variance")
    private Float barometerVariance;        // unit: Pa
    @JsonProperty("Barometer Minimum")
    private Float barometerMin;             // unit: Pa
    @JsonProperty("Barometer Maximum")
    private Float barometerMax;             // unit: Pa
    @JsonProperty("Barometer Temperature")
    private Float barometerTemperature;     // unit: °C

    @JsonProperty("Shock Count")
    private Integer shockCount;

    @JsonProperty("Position Time")
    private ZonedDateTime positionTime;

    @JsonProperty("Positioning")
    private String positioning;

    @JsonProperty("Accuracy")
    private Integer accuracy;
    @JsonProperty("Altitude")
    private Float altitude;
    @JsonProperty("Height Above Terrain")
    private Float hat;
    @JsonProperty("Location")
    private Location location;
    @JsonProperty("Source")
    private String source;                  // NextNav / Combain (cps) / Google / Here

    @JsonProperty("Debugging")
    private String debugging;

    @JsonProperty("Connection Signal Strength")
    private Integer connectionSignalStrength;   // unit: dBm

    @JsonProperty("radioData")
    private RadioData radioData;
}
