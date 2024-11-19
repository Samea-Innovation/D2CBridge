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
package com.traxmate.model;

import lombok.Data;

@Data
public class TraxmateSubmitPositionParameters{
	float latitude;
	float longitude;
	float altitude;
	float accuracy;
	Float heightAboveTerrain;
	String technology;
	Integer temperature;
	Integer batteryLevel;
	Integer rssi;
	Object nswInfo;

	public TraxmateSubmitPositionParameters(float[] position, float confidence, Float heightAboveTerrain, String technology, Integer temperature, Integer batLevel, Integer rssi, Object data){
		longitude = position[0];
		latitude = position[1];
		altitude = position[2];
		accuracy = confidence;
		this.heightAboveTerrain = heightAboveTerrain;
		this.technology = technology;
		this.temperature = temperature;
		batteryLevel = batLevel;
		this.rssi = rssi;
		nswInfo = data;
	}
}
