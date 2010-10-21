package com.gebogebo.android.distancecalc;

import android.location.LocationProvider;
import android.util.Log;

public class DistanceCalculatorUtilities {
    public static String getVisualDistance(float newDistanceInMeters, float multiplier, String distanceSuffix) {
        Log.i("util", "visual mult: " + multiplier + " dist: " + distanceSuffix);
        int viewingDistance = (int)(newDistanceInMeters * multiplier);
        return Float.toString((float)viewingDistance/1000) + " " + distanceSuffix;
    }
    
    public static int getErrorTextId(int errorCode) {
        if(LocationProvider.OUT_OF_SERVICE == errorCode) {
            Log.i("locationService", "GPS service not available");
            return R.string.service_not_available;
        } else if (LocationProvider.TEMPORARILY_UNAVAILABLE == errorCode) {
            Log.i("locationService", "GPS service temporariliy not available");
            return R.string.service_temp_not_available;
        } else if (LocationProvider.AVAILABLE == errorCode) {
            Log.i("locationService", "Service is back and running");
            return R.string.empty;
        } else {
            Log.w("locationService", "Unknow errorCode sent to activity by distance service. Code:  " + errorCode);
            return -1;
        }
    }
}
