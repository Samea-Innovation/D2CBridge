package com.traxmate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

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
    private String positionTime;

    @JsonProperty("Positioning")
    private String positioning;

    @JsonProperty("Accuracy")
    private int accuracy;
    @JsonProperty("Altitude")
    private float altitude;
    @JsonProperty("Height Above Terrain")
    private float hat;
    @JsonProperty("Location")
    private Location location;
    @JsonProperty("Source")
    private String source;                  // NextNav / Combain (cps) / Google / Here

    @JsonProperty("Debugging")
    private String debugging;

    @JsonProperty("Connection Signal Strength")
    private int connectionSignalStrength;   // unit: dBm

    @JsonProperty("radioData")
    private RadioData radioData;
}
