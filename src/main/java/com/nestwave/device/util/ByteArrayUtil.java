package com.nestwave.device.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteArrayUtil {

    public static byte[] byteToBytes(byte value) {
        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES);
        buffer.put(value);
        return buffer.array();
    }

    public static byte[] shortToBytes(short value, ByteOrder order) {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES).order(order);
        buffer.putShort(value);
        return buffer.array();
    }

    public static byte[] intToBytes(int value, ByteOrder order) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES).order(order);
        buffer.putInt(value);
        return buffer.array();
    }

    public static byte[] longToBytes(long value, ByteOrder order) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES).order(order);
        buffer.putLong(value);
        return buffer.array();
    }

    public static byte[] floatToBytes(float value, ByteOrder order) {
        ByteBuffer buffer = ByteBuffer.allocate(Float.BYTES).order(order);
        buffer.putFloat(value);
        return buffer.array();
    }

    public static byte[] doubleToBytes(double value, ByteOrder order) {
        ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES).order(order);
        buffer.putDouble(value);
        return buffer.array();
    }
}
