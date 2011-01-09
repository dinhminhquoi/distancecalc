package com.gebogebo.android.distancecalcfree;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.location.LocationProvider;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class DistanceCalculatorUtilities {
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MMM.DD_HH.mm.ss");
	private static final String SCREENSHOT_DIR = "distancecalc";
	
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
    
    /**
     * captures screenshot for given view object
     * 
     * @param v view whose screenshot is to be taken
     * @param c context from which view is selected
     */
    public void saveParentView(View v, Context c) {
    	v.setDrawingCacheEnabled(true);
    	Bitmap bitmap = v.getDrawingCache();
    	try {
    		if(!Environment.MEDIA_MOUNTED.equalsIgnoreCase(Environment.getExternalStorageState())) {
    			Log.i("activity", "external storage not mounted");
    			Toast.makeText(c, R.string.msg_no_storage, Toast.LENGTH_LONG).show();
    			//inform user that external storage device is not mounted
    			return;
    		}
    		File dirPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + SCREENSHOT_DIR
    				+ File.separator);
    		if(! dirPath.exists()) {
    			if(!dirPath.mkdir()) {
    				//let user know about error
    				Log.i("activity", "unable to create directory");
    				return;
    			}
    		}
    		Date now = new Date();
    		String filename = dirPath + File.separator + DATE_FORMAT.format(now) + ".png";
    		//File dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    		Log.i("activity", "external storage dir: " + filename);
    		File newFile = new File(filename);
    		FileOutputStream outStream = new FileOutputStream(newFile);
    		bitmap.compress(CompressFormat.PNG, 90, outStream);
    		Log.i("activity", "screenshot captured");
    		Toast.makeText(c, String.format(c.getString(R.string.msg_file_saved), filename), Toast.LENGTH_LONG).show();
    	} catch(Exception e) {
    		Log.e("activity", "unable to capture screenshot: " + e);
    		Toast.makeText(c, R.string.msg_unable_to_save, Toast.LENGTH_LONG).show();
    	}
    }
    
    /**
     * obtains formatted string which can be displayed directly to show current speed
     * 
     * @param speedInMetersPerSec current speed in m/s
     * @param multiplier multiplier based on distance unit selected by user
     * @param distanceSuffix suffix to use based on distance unit selected by user
     * @param hourStr string to be used to display hours string 
     * @param formattedString format string to be used for to display current speed 
     * @return formatted string with substitutions which can be displayed directly to show current speed
     */
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
