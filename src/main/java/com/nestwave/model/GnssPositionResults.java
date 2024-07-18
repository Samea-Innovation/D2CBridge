/******************************************************************************
 * Copyright 2022 - NESTWAVE SAS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 *****************************************************************************/
package com.nestwave.model;

import com.nestwave.device.repository.thintrack.ThinTrackPlatformStatusRecord;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class GnssPositionResults{
	public ZonedDateTime utcTime;
	public int gpsTime;
	public float confidence;
	public float[] position;
	public float HeightAboveTerrain;
	public float[] velocity;
	public GpsMeasurements gps;
	public byte[] payload;
	public String technology;
	public ThinTrackPlatformStatusRecord thintrackPlatformStatus;
}

@Data
class GpsMeasurements{
	public float[] prn;
	public float[] cn0;
}
