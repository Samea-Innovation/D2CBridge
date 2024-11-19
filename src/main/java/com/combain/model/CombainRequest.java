package com.combain.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CombainRequest {
    String radioType;
    List<CellTower> cellTowers;
    List<WifiAccessPoint> wifiAccessPoints;
    List<BluetoothBeacon> bluetoothBeacons;
    Gps gps;
    Fallback fallbacks;

    Sensor sensors;

//    boolean geoname;
//    boolean address;
//    boolean indoor;

    LastLocation lastLocation;
//    boolean credits;
//    String state;
//    float speedLimit;

    public void addCellTower(CellTower cellTower) {
        if (this.cellTowers == null)
            this.cellTowers = new ArrayList<>();

        this.cellTowers.add(cellTower);
    }

    public void addWifiAccessPoint(WifiAccessPoint wifiAccessPoint) {
        if (this.wifiAccessPoints == null)
            this.wifiAccessPoints = new ArrayList<>();

        this.wifiAccessPoints.add(wifiAccessPoint);
    }

    public void addBluetoothBeacon(BluetoothBeacon bluetoothBeacon) {
        if (this.bluetoothBeacons == null)
            this.bluetoothBeacons = new ArrayList<>();

        this.bluetoothBeacons.add(bluetoothBeacon);
    }

    @Data
    public static class CellTower {
        //      Parameter                   Required    Values          Description
        String radioType;               //  recommended gsm             The radio type of this cell tower. Can also be put directly in root JSON element if all cellTowers have same radioType
        Integer mobileCountryCode;      //  true        0..999          The Mobile Country Code (MCC)
        Integer mobileNetworkCode;      //  true        0..999          The Mobile Network Code (MNC)
        Integer locationAreaCode;       //  true        0..65535        The Location Area Code (LAC)
        Integer cellId;                 //  true        0..65535        The Cell ID (CID)
        Integer signalStrength;         //  recommended -120--25        The measured signal strength for this cell tower in dBm
        Integer timingAdvance;          //  false       0..63           The timing advance value for this cell tower when available
        Integer primaryScramblingCode;  //  recommended 0..511          The primary scrambling code for WCDMA and physical CellId for LTE
        Boolean serving;                //  false       0..1            Specify with 0/1 if the cell is serving or not. If not specified, the first cell in the array is assumed to be serving
    }

    @Data
    public static class WifiAccessPoint {
        //      Parameter                   Required    Values          Description
        String macAddress;              //  true        ""              The mac address also called BSSID of the Wi-Fi router
        String ssid;                    //  recommended ""              The SSID of the Wi-Fi router
        String ssidHex;                 //  recommended ""              The SSID of the Wi-Fi network in a hex string format. Each byte of the SSID is represented by two HEX characters
        Integer signalStrength;         //  recommended -100--20        The measured signal strength for the access point in dBm
        Integer frequency;              //  false       ?               The frequency of the Wi-Fi access point in MHz. Improves indoor location accuracy
        Integer channel;                //  false       ?               The channel measured of the Wi-Fi access point
        String age;                     //  false       ""              Time ago the measurement was made in ms
    }

    @Data
    public static class BluetoothBeacon {
        //      Parameter                   Required    Values          Description
        String macAddress;              //  true        ""              The mac address of the Bluetooth beacon
        String name;                    //  recommended ""              The name of the Bluetooth beacon
        Integer signalStrength;             //  recommended -100--20        The measured signal strength for the bluetooth beacon in dBm
        String age;                     //  false       ""              Time ago the measurement was made in ms
        String uuid;                    //  false       ""              The uuid identifier of the beacon if iBeacon
        Integer major;                  //  false       0..65535        The major identifier of the beacon if iBeacon
        Integer minor;                  //  false       0..65535        The minor identifier of the beacon if iBeacon
    }

    @Data
    public static class Gps {
        //      Parameter                   Required    Values          Description
        Float latitude;                 //  true        -90..90         The GPS latitude
        Float longitude;                //  true        -180..180       The GPS longitude
        Integer accuracy;               //  true        0..255          The GPS accuracy (horizontal, CEP = 50%) in meters
        Integer sat;                    //  recommended 0..20           The number of satellites seen in the measurement
        Integer age;                    //  recommended 0..600          The age of the GPS location (in seconds)
        Integer altitude;               //  recommended -1000..10000    The altitude of the GPS location
        Integer altitudeAccuracy;       //  false       0..100          The accuracy of the altitude in meters for the GPS location
        Integer heading;                //  recommended 0..360          The direction (bearing) of movement for the GPS location in degrees
        Integer speed;                  //  recommended 0..100          The speed of the GPS location in m/s
    }

    // These parameters should be used with care since it could potentially return wrong coordinates. Associative array with values 1 for on and 0 for off. Parameters for turning individual fallbacks on or off
    @Data
    public static class Fallback {
        //      Parameter                   Required    Values          Description
        Boolean all;                    //  false       0..1            Enable/disable all fallbacks for this request. (This does not include cidxf which has to be activated manually)
        Boolean w4f;                    //  false       0..1            Used to support mobile units only able to read a 4 CID Hex digits of a WCDMA CID (normally 8 CID Hex digits). Search the CID but just compare the 4 less significant hex digits
        Boolean lacf;                   //  false       0..1            If the CID is not found, return the center and accuracy for the LAC
        Boolean nbcidf;                 //  false       0..1            Neighbouring CID. If the cells is not found, it will search for a slightly similar CID
        Boolean rncidf;                 //  false       0..1            If the cell is not found, search with MCC, MNC, RNCID (first part of CellId)
        Boolean mncf;                   //  false       0..1            If the MNC is not found, search for just MCC, LAC and CID
        Boolean ratf;                   //  false       0..1            If the cell is not found search different radio types
        Boolean cidf;                   //  false       0..1            If the cell is not found, search with just MCC, MNC, CID
    }

    @Data
    public static class LastLocation {
        //      Parameter                   Required    Values          Description
        Float lat;                      //  true        -90..90         The last known latitude
        Float lng;                      //  true        -180..180       The last known longitude
        Integer accuracy;               //  true        0..255          The accuracy of last known location in meters
        Integer timestamp;              //  true        timestamp       Unix timestamp
    }

    @Data
    public static class Sensor {
        //      Parameter                   Required    Values          Description
        Integer pressure;               //  false       ?               The ambient air pressure in hPa or mbar. Normally 1013 at sea level
        Integer steps;                  //  false       ?               The number of steps since last request. For best performance use effective number of steps (=distance). Thus, if going 4 steps straight and then 3 steps to the right, the effective number of steps are 5
        Integer heading;                //  false       0..359          The heading of the movement in degrees
        String activity;                //  false       ""              The detected activity in an Android device. Valid values are IN_VEHICLE, ON_BICYCLE, ON_FOOT, RUNNING, STILL, TILTING, UNKNOWN and WALKING
    }
}
