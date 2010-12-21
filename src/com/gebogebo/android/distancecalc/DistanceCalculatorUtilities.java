package com.gebogebo.android.distancecalc;

import android.location.LocationProvider;
import android.util.Log;

public class DistanceCalculatorUtilities {
	/**
     * converts given float distance and returns string representation which is directly displayable on activity
     * 
     * @param newDistanceInMeters float distance to be converted (in meters)
     * @param multiplier distance unit multiplier. 1.00 for km, 0.6xx for miles
     * @param distanceSuffix distance suffix to be used based on distance unit 
     * @return
     */
    public String getVisualDistance(float newDistanceInMeters, long totalTimeInSecs, float multiplier, 
    		String distanceSuffix, String hourStr) {
//        Log.i("util", "visual mult: " + multiplier + " dist: " + distanceSuffix);
        int viewingDistance = (int)(newDistanceInMeters * multiplier);
        //speed is always calculated per hour basis
        float speed = (float)(newDistanceInMeters * 3.6 * multiplier) / totalTimeInSecs; //3.6 = 3600 min / 1000 (for meters to km)
        return String.format("%.3f %s at %.2f %s/%s", (float)viewingDistance/1000, distanceSuffix, speed, distanceSuffix, hourStr);
    }
    
    public String getVisualCurrentSpeed(float speedInMetersPerSec, float multiplier, String distanceSuffix, 
    		String hourStr, String formattedString) {
    	float speed = (float)(speedInMetersPerSec * 3.6 * multiplier);
    	return String.format(formattedString, speed, distanceSuffix, hourStr);
    }
    
    /**
     * returns corresponding error code id from string resources, which corresponds to passed errorCode
     * 
     * @param errorCode error code for which string resources id is to be returned
     * @return string resources id for given error code
     */
    public int getErrorTextId(int errorCode) {
        if(LocationProvider.OUT_OF_SERVICE == errorCode) {
            Log.d("locationService", "GPS service not available");
            return R.string.service_not_available;
        } else if (LocationProvider.TEMPORARILY_UNAVAILABLE == errorCode) {
            Log.d("locationService", "GPS service temporariliy not available");
            return R.string.service_temp_not_available;
        } else if (LocationProvider.AVAILABLE == errorCode) {
            Log.d("locationService", "Service is back and running");
            return R.string.empty;
        } else {
            Log.w("locationService", "Unknow errorCode sent to activity by distance service. Code:  " + errorCode);
            return -1;
        }
    }
}
