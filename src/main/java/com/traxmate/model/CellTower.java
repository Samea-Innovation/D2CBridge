package com.traxmate.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CellTower {
    /**Radio type: GSM/2G/3G/LTE/NB-IoT
     */
    private String radioType;

    @JsonProperty("mobileCountryCode")
    private int mcc;
    @JsonProperty("mobileNetworkCode")
    private int mnc;
    @JsonProperty("locationAreaCode")
    private int lac;
    @JsonProperty("cellId")
    private int cid;
    @JsonProperty("signalStrength")
    private int rsrp;
}
