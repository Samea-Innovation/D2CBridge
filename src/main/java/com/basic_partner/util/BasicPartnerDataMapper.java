package com.basic_partner.util;

import com.basic_partner.model.BasicPartnerPostData;
import com.nestwave.model.GnssPositionResults;
import org.springframework.stereotype.Component;

@Component
public class BasicPartnerDataMapper {
    public BasicPartnerPostData mapPostData(long deviceId, long imei, int customerId, GnssPositionResults gnssPositionResults) {
        BasicPartnerPostData data = new BasicPartnerPostData();
        data.setDeviceId(deviceId);
        data.setCustomerId(customerId);
        data.setImei(imei);
        data.setGnssPositionResults(gnssPositionResults);
        return data;
    }
}
