package com.combain.model;

import lombok.Data;

@Data
public class CombainResponse {
    Location location;          //  Array of the estimated location. See below for details.
    Integer accuracy;               //  The accuracy is the estimated median error in meters, i.e. the radius in a circle with 50% confidence level.

    Float heading;              //  Heading for GPS location if available.
    Float altitude;             //  Altitude for GPS location if available.
    Float speed;                //  Speed for GPS location if available (m/s).
    Integer calculatedSpeed;        //  Calculated speed between this and previous location (m/s).

    Geoname geoname;            //  If geoname is set in the request the geoname object will be returned if available. See below for details.
    Indoor indoor;              //  If indoor is set in the request the indoor object will be returned if available. See below for details.
    Address address;            //  If address is set in the request the address object will be returned if available. See the Reverse Geocoding for example of possible address fields.

    String fallback;            //  If this field is included no direct match was found. It shows which fallback was used to generate the response.
    String state;               //  If this field is included, it should preferably be used in next request for the specific device. The parameter includes positioning history that enhances indoor positioning performance and enables speed filter.

    Error error;                //  In the case of an error, a standard format error response body will be returned and the HTTP status code will be set to an error status.

    Long logId;                 //  A logId of the request that could be used for debugging purposes. Requests are stored for maximum 7 days currently and can be downloaded in portal.

    @Data
    public static class Location {
        Float lat;              //  The latitude of the estimated location.
        Float lng;              //  The longitude of the estimated location.
    }

    @Data
    public static class Geoname {
        String town;            //  Town of the location.
        String country;         //  Country of the location.
        String country_code;    //  Country code of the location according to ISO 3166.
    }

    @Data
    public static class Indoor {
        Integer buildingModelId;    //  The ID of the building model used to calculate the location.
        Integer buildingId;         //  The ID of the building that the used building model belongs to.
        String building;        //  The name of the building that the used building model belongs to.
        Integer floorIndex;         //  The index of the calculated floor if available. 0 means ground level.
        String floorLabel;      //  The name of the calculated floor if available.
        String room;            //  The name of the calculated room if available.
    }

    @Data
    public static class Address {

    }
}
