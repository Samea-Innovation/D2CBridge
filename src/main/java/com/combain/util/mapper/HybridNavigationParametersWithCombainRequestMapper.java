package com.combain.util.mapper;

import com.combain.model.CombainRequest;
import com.nestwave.device.model.HybridNavParameters;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

@Slf4j
public class HybridNavigationParametersWithCombainRequestMapper {
    public static CombainRequest toCombainRequest(HybridNavParameters hybridNavParameters) {
        CombainRequest request = new CombainRequest();

        Object[] cellTowers = hybridNavParameters.cellTowers;
        if (cellTowers != null) {
            log.info("{} cell towers", cellTowers.length);
            for (Object cell : cellTowers) {
                log.info(cell.toString());
                Class<?> c = cell.getClass();

                int cellId;
                int mcc;
                int mnc;
                int lac;
                int rsrp;

                try {
                    Field field;

                    field = c.getField("mcc");
                    field.setAccessible(true);
                    mcc = (int) field.get(cell);

                    field = c.getField("mnc");
                    field.setAccessible(true);
                    mnc = (int) field.get(cell);

                    field = c.getField("lac");
                    field.setAccessible(true);
                    lac = (int) field.get(cell);

                    field = c.getField("cellId");
                    field.setAccessible(true);
                    cellId = (int) field.get(cell);

                    field = c.getField("rsrp");
                    field.setAccessible(true);
                    rsrp = (int) field.get(cell);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    return request;
                }

                log.info("MCC: {}, MNC: {}, LAC: {}, CID: {}, RSSI: {}",
                        mcc, mnc, lac, cellId, rsrp);

                CombainRequest.CellTower cellTower = new CombainRequest.CellTower();
                cellTower.setRadioType("lte");
                cellTower.setMobileCountryCode(mcc);
                cellTower.setMobileNetworkCode(mnc);
                cellTower.setLocationAreaCode(lac);
                cellTower.setCellId(cellId);
                cellTower.setSignalStrength(rsrp);

                request.addCellTower(cellTower);
            }
        }

        Object[] wifiAccessPoints = hybridNavParameters.wifiAccessPoints;
        if (wifiAccessPoints != null) {
            log.info("{} Wi-Fi access points", wifiAccessPoints.length);
            for (Object singleWifiInfo : wifiAccessPoints) {
                log.info(singleWifiInfo.toString());
                Class<?> c = singleWifiInfo.getClass();

                String mac;
                int rssi;

                try {
                    Field field;

                    field = c.getField("mac");
                    field.setAccessible(true);
                    mac = (String) field.get(singleWifiInfo);

                    field = c.getField("rssi");
                    field.setAccessible(true);
                    rssi = (int) field.get(singleWifiInfo);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    return request;
                }

                log.info("MAC: {}, RSSI: {}",
                        mac, rssi);

                CombainRequest.WifiAccessPoint wifiAccessPoint = new CombainRequest.WifiAccessPoint();
                wifiAccessPoint.setMacAddress(mac);
                wifiAccessPoint.setSignalStrength(rssi);
                request.addWifiAccessPoint(wifiAccessPoint);
            }
        }

        Object[] bluetoothBeacons = hybridNavParameters.bluetoothBeacons;
        if (bluetoothBeacons != null) {
            log.info("{} bluetooth beacons", bluetoothBeacons.length);
            for (Object SingleBluetoothInfo : bluetoothBeacons) {
                log.info(SingleBluetoothInfo.toString());
                Class<?> c = SingleBluetoothInfo.getClass();

                String mac;
                int rssi;

                try {
                    Field field;

                    field = c.getField("mac");
                    field.setAccessible(true);
                    mac = (String) field.get(SingleBluetoothInfo);

                    field = c.getField("rssi");
                    field.setAccessible(true);
                    rssi = (int) field.get(SingleBluetoothInfo);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    return request;
                }

                log.info("MAC: {}, RSSI: {}",
                        mac, rssi);

                CombainRequest.BluetoothBeacon bluetoothBeacon = new CombainRequest.BluetoothBeacon();
                bluetoothBeacon.setMacAddress(mac);
                bluetoothBeacon.setSignalStrength(rssi);
                request.addBluetoothBeacon(bluetoothBeacon);
            }
        }

        return request;
    }
}
