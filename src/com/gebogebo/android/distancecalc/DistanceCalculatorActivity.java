package com.gebogebo.android.distancecalc;

import java.util.Random;

//import com.admob.android.ads.AdManager;
import com.admob.android.ads.AdView;
import com.gebogebo.android.distancecalc.DistanceCalculatorService.DistanceServiceBinder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author viraj
 *
 */
public class DistanceCalculatorActivity extends Activity implements OnClickListener, DistanceCalculatorServiceListener {
	private static String currentSpeedFormat = null;
    private boolean isCalculatingDistance = false; //indicates state of app (calculating distance or not)
    private DistanceCalculatorService locationService = null;
    private DistanceCalculatorUtilities util;
    private String hoursStr = null;
    
    private TextView distanceText = null;
    private TextView speedText = null;
    private TextView errorText = null;
    
    private float multiplier = 1.0F;
    private String distanceSuffix;
    private Random random = new Random(System.currentTimeMillis());
    
    /**
     * set activity variable as per its current operation
     */
    private void setActivityState() {
        Button button = (Button)findViewById(R.drawable.button);
        if(isCalculatingDistance) {
            //if app is in distance calculating mode
            button.setText(R.string.stop);
            speedText.setText(R.string.dots);
            float actualDistance = locationService.getCurrentDistance();
            if(actualDistance >= 0.0) {
                long timeElapsed = locationService.getTimeElapsed();
                String visualDistance = util.getVisualDistance(actualDistance, timeElapsed, multiplier, distanceSuffix, hoursStr);
                distanceText.setText(visualDistance);
                errorText.setText(R.string.empty);
            } else {
                errorText.setText(R.string.service_temp_not_available);
            }
            util = new DistanceCalculatorUtilities();
            Log.i("activity", "handled on click event for button. calculating distance now.");
        } else {
            //if app is not in distance calculating mode
            button.setText(R.string.start);
            Log.i("activity", "handled on click event for button. not calculating distance now.");
        }
    }
    
    /**
     * sets distance calculating parameters as per chosen settings
     */
    private void setDistanceParameters() {
        SharedPreferences defPref = PreferenceManager.getDefaultSharedPreferences(this);
        //default selection is miles
        String selectedMetricId = defPref.getString(DistanceCalculatorPrefActivity.PREF_KEY_DISTANCE_UNIT, 
        		DistanceCalculatorPrefActivity.PREF_MILES);
        Log.i("activity", "got selected metric: " + selectedMetricId);
        multiplier = DistanceCalculatorPrefActivity.getDistanceMultiplierForDistanceUnit(selectedMetricId);
        if (DistanceCalculatorPrefActivity.PREF_KM.equals(selectedMetricId)) {
            distanceSuffix = getString(R.string.km);
            Log.i("menu", "KMs selected: " + distanceSuffix);
        } else if (DistanceCalculatorPrefActivity.PREF_MILES.equals(selectedMetricId)) {
            distanceSuffix = getString(R.string.miles);
            Log.i("menu", "Miles selected: " + distanceSuffix);
        }
    }
    
    private ServiceConnection serviceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("serviceConn", "Service disconnected to activity");
        }
        
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("serviceConn", "Service connected to activity");
            locationService = ((DistanceServiceBinder)service).getService();
            float currDistance = locationService.getCurrentDistance();
            if(currDistance < 0.0) {
                isCalculatingDistance = false;
            } else {
                isCalculatingDistance = true;
            }
            setActivityState();
        }
    };
    
    //DistanceCalculatorServiceListener methods
    @Override
    public void onDistanceChange(float newDistanceInMeters, long totalTimeInSecs, Location newLocation) {
        if(newDistanceInMeters >= 0.0) {
            errorText.setText(R.string.empty);
        }
        Log.i("serviceListener", "distance changed");
        String visualDistance = util.getVisualDistance(newDistanceInMeters, totalTimeInSecs, multiplier, distanceSuffix, hoursStr);
        distanceText.setText(visualDistance);
        
        if(newLocation.hasSpeed()) {
        	String speed = util.getVisualCurrentSpeed(newLocation.getSpeed(), multiplier, distanceSuffix, hoursStr, currentSpeedFormat);
        	speedText.setText(speed);
        }
        
        if(random.nextInt(10) < 1) {
        	//with a probability of 10%
            AdView adView = (AdView)findViewById(R.id.ad);
            adView.requestFreshAd();
        }
    }
    
    @Override
    public void onServiceStatusChange(int errorCode) {
        int textId = util.getErrorTextId(errorCode);
        if(textId > 0) {
            errorText.setText(textId);
        }
    }
    
    //activity overrides
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.i("activity", "set activity as content view: " + this);
        
        util = new DistanceCalculatorUtilities();
        distanceText = (TextView) findViewById(R.drawable.distance);
        speedText = (TextView) findViewById(R.drawable.currentSpeed);
        errorText = (TextView) findViewById(R.drawable.errors);
        hoursStr = getString(R.string.Hr);
        currentSpeedFormat = getString(R.string.currSpeedFormat);
        Button button = (Button)findViewById(R.drawable.button);
        button.setOnClickListener(this);
        Log.i("activity", "added button click listener");
        
        setDistanceParameters();

        Intent serviceStart = new Intent(Intent.ACTION_MAIN);
        serviceStart.setClassName("com.gebogebo.android.distancecalc", "com.gebogebo.android.distancecalc.DistanceCalculatorService");
        startService(serviceStart);
        bindService(serviceStart, serviceConn, BIND_AUTO_CREATE);
        
//    	AdManager.setTestDevices( new String[] { AdManager.TEST_EMULATOR, "CA101E12F9C3DF4E8301247EF68FB13C" } );
        AdView adView = (AdView)findViewById(R.id.ad);
        adView.requestFreshAd();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        Log.i("menu", "Options menu created");
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_exit) {
            Log.i("menu", "Exit selected");
            this.finish();
        } else if(item.getItemId() == R.id.settings) {
            Log.i("menu", "Settings selected");
            Intent prefIntent = new Intent(Intent.ACTION_EDIT);
            prefIntent.addCategory(Intent.CATEGORY_PREFERENCE);
            startActivityForResult(prefIntent, 1);
        }
        return true;
    }
    
    @Override
    public void onDestroy() {
        Log.i("lifecycle", "in destroy");
        locationService.removeListener(this);
        
        if(isFinishing()) {
            Log.i("lifecycle", "activity is destory-finishing");
            //if acticity was 
            Intent serviceStop = new Intent(Intent.ACTION_MAIN);
            serviceStop.setClassName("com.gebogebo.android.distancecalc", "com.gebogebo.android.distancecalc.DistanceCalculatorService");
            unbindService(serviceConn);
            stopService(serviceStop);
        }
        super.onDestroy();
    }
    
    @Override
    public void onBackPressed() {
        Log.i("activity", "BACK button pressed");
        moveTaskToBack(false);
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//there is only one activity we call.. so no checks are added here
		setDistanceParameters();
	}

    //onClickListener overrides
    @Override
    public void onClick(View v) {
        isCalculatingDistance = !isCalculatingDistance;
        if(isCalculatingDistance) {
            locationService.start();
            locationService.addListener(this);
        } else {
            locationService.removeListener(this);
            locationService.stop();
        }
        setActivityState();
    }
}