package com.gebogebo.android.distancecalcfree;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

/**
 * Preference acitivity which controls preferences for distance calculator. also contains constants for
 * settings of distance calculator
 * 
 * @author viraj
 */
public class DistanceCalculatorPrefActivity extends PreferenceActivity {
	//preference constants for this app
    public static final String PREF_KEY_DISTANCE_UNIT = "distanceUnit";
    public static final String PREF_KEY_ACTION = "action";
    public static final String PREF_KEY_AUTO_REPORT = "autoReport";
    public static final String PREF_KEY_AUTO_SAVE = "autoSave";
    //distance unit constants
    public static final String PREF_MILES = "miles";
    public static final String PREF_KM = "km";
    //action constants
    public static final String PREF_WALKING = "walking";
    public static final String PREF_BIKING = "biking";
    public static final String PREF_DRIVING = "driving";
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i("prefAct", "in pref activity");
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
	}

	@Override
	public void onBackPressed() {
        Log.i("activity", "BACK button pressed");
        this.finish();
	}
	
	/**
	 * returns frequency at which GPS location should be listened to based on selected action.
	 * if-else blocks are used to save CPU cycles. 
	 * 
	 * @param action selected action
	 * @return frequency at which GPS location should be listened to
	 */
	public static final Integer getFrequencyInMillisForAction(String action) {
		//contant 500 is returned. control frequency by distance (see below)
		return 500;
	}
	
	/**
	 * returns minimum distance at which GPS notifications are to be listened to
	 * 
	 * @param action selection action
	 * @return distance frequency at which GPS location should be listened to
	 */
	public static final Integer getFrequencyInMetersForAction(String action) {
		if(PREF_BIKING.equals(action)) {
			return 30;
		} else if(PREF_DRIVING.equals(action)) {
			return 70;
		} else {
			//walking or default
			return 10;
		}
	}
	
	public static final Integer getMinZoomLevelForAction(String action) {
		if(PREF_DRIVING.equals(action)) {
			return 15;
		} else {
			//walking or default
			return 17;
		}
	}
	
	/**
	 * returns distance multiplier based on unit sent as parameter
	 * 
	 * @param unit unit for which distance multiplier is required
	 * @return distance multiplier to be applied to GPS location distance
	 */
	public static final Float getDistanceMultiplierForDistanceUnit(String unit) {
		if(PREF_MILES.equals(unit)) {
			return 0.621371192F;
		} else {
			//km or default
			return 1.0F;
		}
	}
}
