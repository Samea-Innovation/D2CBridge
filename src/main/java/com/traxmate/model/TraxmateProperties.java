package com.traxmate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TraxmateProperties {
    /**
     * 2G/3G/4G/LTE/LoRa/NB-IoT/BLE
     */
    @JsonProperty("Connectivity")
    private String connectivity;

    @JsonProperty("Technology")
    private String technology;
}
