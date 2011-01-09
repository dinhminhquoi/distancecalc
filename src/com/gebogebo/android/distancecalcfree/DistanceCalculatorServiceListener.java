package com.gebogebo.android.distancecalcfree;

import android.location.Location;

public interface DistanceCalculatorServiceListener {
	void onDistanceChange(float newDistanceInMeters, long totalTimeInSecs, Location location);
    void onServiceStatusChange(int errorCode); 
}
