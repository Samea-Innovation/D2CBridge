package com.nestwave.device.util;

import com.nestwave.model.Payload;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.elements.util.Bytes;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

import static com.nestwave.device.util.ByteArrayUtil.*;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

@Slf4j
@Component
public class ThintrackDownLinkEncoder {

    public static byte[] encode(ZonedDateTime utc, int accuracy, float[] position) {
        byte[] payload = { };

        // 1 - UTC Time 32-bit Epoch (u32 | 4 bytes)
        byte[] bUTC = encodeUTC(utc);
        payload = Bytes.concatenate(payload, bUTC);
        log.debug("DownLink | UTC Time: {} -> {}", utc, bUTC);

        // 2 - UTC Time nano seconds (u32 | 4 bytes)
        byte[] bUTCNano = encodeUTCNano(utc);
        payload = Bytes.concatenate(payload, bUTCNano);
        log.debug("DownLink | UTC Time nano: {} -> {}", utc, bUTCNano);

        // 3 - ? (4 bytes)
        payload = Bytes.concatenate(payload, new byte[4]);

        // 4 - Accuracy (u32 | 4 bytes)
        byte[] bAccuracy = intToBytes(accuracy, LITTLE_ENDIAN);
        payload = Bytes.concatenate(payload, bAccuracy);
        log.debug("DownLink | Accuracy: {} -> {}", accuracy, bAccuracy);

        // 5 - Position (lat, lng, alt)
        byte[] bPosition = encodePosition(position[1], position[0], position[2]);
        payload = Bytes.concatenate(payload, bPosition);
        log.debug("DownLink | Position: (lat: {}, lng: {}, alt: {}) -> {}", position[1], position[0], position[2], bPosition);

        // 6 - ? (20 bytes)
        payload = Bytes.concatenate(payload, new byte[20]);

        // 7 - Check Word
        int checkWord = Payload.fletcher32(payload, payload.length);
        payload = Payload.appendFletcher32(payload);
//        payload = Bytes.concatenate(payload, intToBytes(checkWord, LITTLE_ENDIAN));
        log.debug("DownLink | Check Word: {}", checkWord);

        return payload;
    }

    public static byte[] encodeUTC(ZonedDateTime utc) {
        long utcEpoch = utc.toEpochSecond();
        return intToBytes((int) utcEpoch, LITTLE_ENDIAN);
    }

    public static byte[] encodeUTCNano(ZonedDateTime utc) {
        return intToBytes(utc.getNano(), LITTLE_ENDIAN);
    }

    public static byte[] encodePosition(double latitude, double longitude, float altitude) {
        byte[] latB = doubleToBytes(latitude,  LITTLE_ENDIAN);
        byte[] lngB = doubleToBytes(longitude, LITTLE_ENDIAN);
        byte[] altB = floatToBytes(altitude,   LITTLE_ENDIAN);

        byte[] b = Bytes.concatenate(latB, lngB);
        return Bytes.concatenate(b, altB);
    }
}
