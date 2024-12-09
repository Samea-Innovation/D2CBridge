package com.basic_partner.model;

import com.nestwave.model.GnssPositionResults;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BasicPartnerPostData {
    private long deviceId;
    private long imei;
    private int customerId;
    private GnssPositionResults gnssPositionResults;

    public BasicPartnerPostData() {
    }
}
