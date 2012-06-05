package com.gebogebo.android.distancecalcfree;

import static java.lang.String.format;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.millennialmedia.android.MMAdView;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.location.LocationProvider;
import android.os.Environment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class DistanceCalculatorUtilities {
    public static final String ADMOB_KEY = "a14d09d21af1d1f";
    private static final String MILLENIA_KEY = "61922";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MMM.dd_HH.mm.ss");
    private static final String SCREENSHOT_DIR = "distancecalc";
    private static final String SCREENSHOT_TEMP_DIR = "distancecalcTmp";
    private static Random random = new Random(System.currentTimeMillis());

    /**
     * converts given float distance and returns string representation which is directly displayable on activity
     * 
     * @param newDistanceInMeters float distance to be converted (in meters)
     * @param multiplier distance unit multiplier. 1.00 for km, 0.6xx for miles
     * @param distanceSuffix distance suffix to be used based on distance unit
     * @return
     */
    public static String getVisualDistance(float newDistanceInMeters, long totalTimeInSecs, float multiplier,
            String distanceSuffix, String hourStr) {
        // Log.i("util", "visual mult: " + multiplier + " dist: " + distanceSuffix);
        int viewingDistance = (int) (newDistanceInMeters * multiplier);
        // speed is always calculated per hour basis
        float speed = (float) (newDistanceInMeters * 3.6 * multiplier) / totalTimeInSecs; 
        // 3.6 = 3600 min / 1000 (for meters to km)
        return String.format("%.3f %s at %.2f %s/%s", (float) viewingDistance / 1000, distanceSuffix, speed,
                distanceSuffix, hourStr);
    }

    /**
     * obtains report specific string for distance
     * 
     * @param distanceInMeters distance in meters
     * @param multiplier multiplier to use to get distance in user selected unit
     * @param distanceSuffix suffix as per unit selected by user
     * @return formatted, displayable string to display distance in report
     */
    public static String getDistanceForReport(float distanceInMeters, float multiplier, String distanceSuffix) {
        // Log.i("util", "visual mult: " + multiplier + " dist: " + distanceSuffix);
        int viewingDistance = (int) (distanceInMeters * multiplier);
        return String.format("%.3f %s", (float) viewingDistance / 1000, distanceSuffix);
    }

    /**
     * returns visual representable elapsed time
     * 
     * @param timeElapsed number of seconds which are to be converted to formatted
     * @param timeElapsedFormattedString format string which holds format in which time is to be formatted
     * 
     * @return formatted string representation of passed number of seconds
     */
    public static String getVisualTime(long timeElapsed, String timeElapsedFormattedString) {
        if (timeElapsed < 0) {
            timeElapsed = 0;
        }
        return String.format(timeElapsedFormattedString, DateUtils.formatElapsedTime(timeElapsed));
    }

    /**
     * returns formatted, displayable elapsed time
     * 
     * @param timeElapsed time in secs which is to be formatted
     * @return formatted elapsed time
     */
    public static String getTimeForReport(long timeElapsed) {
        if (timeElapsed < 0) {
            timeElapsed = 0;
        }
        return DateUtils.formatElapsedTime(timeElapsed);
    }

    /**
     * obtains report specific formatted, displayable string for speed
     * 
     * @param speedInMetersPerSec speed in meters per second
     * @param multiplier multiplier to use based on distance unit selected by user
     * @param distanceSuffix suffix as per distance unit selected by user
     * @param hourStr hour string 
     * @return report specific formatted, displayable string for speed
     */
    public static String getSpeedForReport(float speedInMetersPerSec, float multiplier, String distanceSuffix, String hourStr) {
        float speed = (float) (speedInMetersPerSec * 3.6 * multiplier);
        return String.format("%.2f %s/%s", speed, distanceSuffix, hourStr);
    }

    /**
     * obtains report specific formatted, displayable string for average speed
     * 
     * @param distanceInMeters distance covered in meters
     * @param totalTimeInSecs total time taken to cover distance
     * @param multiplier multiplier to use based on user's selection of distance unit
     * @param distanceSuffix distance suffix based on user's selection of distance unit
     * @param hourStr hour string 
     * @return report specific formatted, displayable string for average speed
     */
    public static String getAverageSpeedForReport(float distanceInMeters, long totalTimeInSecs, float multiplier,
            String distanceSuffix, String hourStr) {
        float speed = 0f;
        if (totalTimeInSecs > 0) {
            speed = (float) (distanceInMeters * 3.6 * multiplier) / totalTimeInSecs;
        }
        return String.format("%.2f %s/%s", speed, distanceSuffix, hourStr);
    }

    /**
     * captures screenshot for given view object
     * 
     * @param v view whose screenshot is to be taken
     * @param c context from which view is selected
     * @param isTemporary indicates if parent view of given view is to be saved temporarily or not 
     */
    public static String saveParentView(View v, Context c, boolean isTemporary) {
        v.setDrawingCacheEnabled(true);
        Bitmap bitmap = v.getDrawingCache();
        try {
            if(!Environment.MEDIA_MOUNTED.equalsIgnoreCase(Environment.getExternalStorageState())) {
                Log.i("activity", "external storage not mounted");
                Toast.makeText(c, R.string.msg_no_storage, Toast.LENGTH_LONG).show();
                //inform user that external storage device is not mounted
                return null;
            }
            File dirPath = null;
            if(isTemporary) {
                dirPath = new File(getTempDirPath());
            } else {
                dirPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + SCREENSHOT_DIR
                        + File.separator);
            }
            if(dirPath != null && !dirPath.exists()) {
                if(!dirPath.mkdir()) {
                    //let user know about error
                    Log.i("activity", "unable to create directory");
                    return null;
                }
            }
            Date now = new Date();
            String filename = dirPath.getAbsolutePath() + File.separator + DATE_FORMAT.format(now) + ".png";
            File newFile = new File(filename);
            FileOutputStream outStream = new FileOutputStream(newFile);
            bitmap.compress(CompressFormat.PNG, 90, outStream);
            Log.i("util", "screenshot captured to file: " + filename);
            return filename;
        } catch(Exception e) {
            Log.e("util", "exception while capturing screenshot: " + e, e);
            Toast.makeText(c, R.string.msg_unable_to_save, Toast.LENGTH_LONG).show();
        }
        return null;
    }
    
    /**
     * deletes all temporary file from distance calculator temporary directory
     */
    public static void deleteTempFiles() {
        File dirPath = new File(getTempDirPath());
        try {
            for(File file : dirPath.listFiles()) {
                file.delete();
            }
            Log.i("util", "deleted all temp files successfully");
        } catch(Throwable t) {
            Log.i("util", "error while deleting temp file. continuing with others");
        }
    }
    
    /**
     * obtains string to get the temporary directory for distance calculator app
     * 
     * @return string representing temporary directory for distance calculator app
     */
    private static String getTempDirPath() {
        String tmpDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + SCREENSHOT_DIR
            + File.separator + SCREENSHOT_TEMP_DIR + File.separator;
        return tmpDirPath;
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
        float speed = (float) (speedInMetersPerSec * 3.6 * multiplier);
        return String.format(formattedString, speed, distanceSuffix, hourStr);
    }

    /**
     * returns corresponding error code id from string resources, which corresponds to passed errorCode
     * 
     * @param errorCode error code for which string resources id is to be returned
     * @return string resources id for given error code
     */
    public int getErrorTextId(int errorCode) {
        if (LocationProvider.OUT_OF_SERVICE == errorCode) {
            Log.d("util", "GPS service not available");
            return R.string.service_not_available;
        } else if (LocationProvider.TEMPORARILY_UNAVAILABLE == errorCode) {
            Log.d("util", "GPS service temporariliy not available");
            return R.string.service_temp_not_available;
        } else if (LocationProvider.AVAILABLE == errorCode) {
            Log.d("util", "Service is back and running");
            return R.string.empty;
        } else {
            Log.w("util", "Unknow errorCode sent to activity by distance service. Code:  " + errorCode);
            return -1;
        }
    }
    
    /**
     * adds millenia ad to given layout, with given adType and with given refresh time.
     * (refresh time isn't working as of now)
     * 
     * @param ll layout in which millenia ad is to be added
     */
    public static void addMilleniaAd(Activity activity, LinearLayout ll) {
        Log.i("activity", format("adding millenia %s type ad", MMAdView.BANNER_AD_BOTTOM));
        MMAdView mmAdView = new MMAdView(activity, MILLENIA_KEY, MMAdView.BANNER_AD_BOTTOM, 60);
        mmAdView.setId(random.nextInt(Integer.MAX_VALUE));
        ll.addView(mmAdView);
        mmAdView.fetch();
    }
    
    public static AdView addAdmobAd(Activity activity, LinearLayout ll) {
        // AdManager.setTestDevices( new String[] { AdManager.TEST_EMULATOR,
        // "CA101E12F9C3DF4E8301247EF68FB13C" } );
        AdView adView = new AdView(activity, AdSize.BANNER, DistanceCalculatorUtilities.ADMOB_KEY);
        adView.loadAd(new AdRequest());
        ll.addView(adView);
        return adView;
    }
    
    public static int getRandomInt(int upperLimit) {
        return random.nextInt(upperLimit);
    }
}
