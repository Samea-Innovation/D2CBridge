package com.traxmate.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TraxmateCapture {
    private String name;
    private int typeId;

    TraxmateProperties properties;
    TraxmateSignals signals;

    Object original;

    public TraxmateCapture(String name, int typeId, TraxmateProperties properties, TraxmateSignals signals, Object original) {
        this.name = name;
        this.typeId = typeId;
        this.properties = properties;
        this.signals = signals;
        this.original = original;
    }
}
