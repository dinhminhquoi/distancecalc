package com.gebogebo.android.distancecalc;

import com.gebogebo.android.distancecalc.DistanceCalculatorService.DistanceServiceBinder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.IBinder;
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
    private static final String PREF_METRIC = "metric";
    private static final String PREF_FILE_NAME = "settings.pref";
    
    private boolean isCalculatingDistance = false;
    private DistanceCalculatorService locationService = null;
    
    private TextView distanceText = null;
    private TextView errorText = null;
    
    private float multiplier = 1.0F;
    private String distanceSuffix;
    
    @Override
    public void onNewIntent(Intent i) {
        Log.i("enter", "In onNewIntent: " + this);
        super.onNewIntent(i);
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.i("activity", "set activity as content view: " + this);

        distanceText = (TextView) findViewById(R.drawable.distance);
        errorText = (TextView) findViewById(R.drawable.errors);
        Button button = (Button)findViewById(R.drawable.button);
        button.setOnClickListener(this);
        Log.i("activity", "added button click listener");
        
        SharedPreferences prefs = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        //default selection is miles
        int selectedMetricId = prefs.getInt(PREF_METRIC, R.id.miles);
        Log.i("activity", "got selected metric: " + selectedMetricId);
        setDistanceParameters(selectedMetricId);
        Log.i("debug", "create mult: " + multiplier + " dist: " + distanceSuffix);

        Intent serviceStart = new Intent(Intent.ACTION_MAIN);
        serviceStart.setClassName("com.gebogebo.android.distancecalc", "com.gebogebo.android.distancecalc.DistanceCalculatorService");
        startService(serviceStart);
        bindService(serviceStart, serviceConn, BIND_AUTO_CREATE);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        Log.i("menu", "Options menu created");
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.i("menu", "options menu being prepared");
        //not caching prefs as it could grow really big in memory which could slow down the app
        SharedPreferences prefs = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        //default selection is miles
        int selectedUnit = prefs.getInt(PREF_METRIC, R.id.miles);
        Log.i("", "unit selected: " + selectedUnit);
        MenuItem menuItem = menu.findItem(selectedUnit);
        if(menuItem != null) {
            menuItem.setChecked(true);
        }
        return true;
    }  
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_exit) {
            Log.i("menu", "Exit selected");
            this.finish();
        } else if(item.getItemId() == R.id.menu_settings) {
            Log.i("menu", "Settings selected");
        } else {
            Log.i("menu", "Unit selected ");
            setDistanceParameters(item.getItemId());
        }
        return true;
    }
    
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
    
    private void setActivityState() {
        Button button = (Button)findViewById(R.drawable.button);
        if(isCalculatingDistance) {
            //if app is in distance calculating mode
            button.setText(R.string.stop);
            float actualDistance = locationService.getCurrentDistance();
            if(actualDistance >= 0.0) {
                String visualDistance = DistanceCalculatorUtilities.getVisualDistance(actualDistance, multiplier, distanceSuffix);
                distanceText.setText(visualDistance);
                errorText.setText(R.string.empty);
            } else {
                errorText.setText(R.string.service_temp_not_available);
            }
            Log.i("activity", "handled on click event for button. calculating distance now.");
        } else {
            //if app is not in distance calculating mode
            button.setText(R.string.start);
            Log.i("activity", "handled on click event for button. not calculating distance now.");
        }
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
        Log.i("lifecycle", "BACK button pressed");
        moveTaskToBack(false);
    }
    
    private void setDistanceParameters(int selectedMetricId) {
        Editor prefEditor = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE).edit();
        prefEditor.putInt(PREF_METRIC, selectedMetricId);
        prefEditor.commit();
        if (selectedMetricId == R.id.km) {
            distanceSuffix = getString(R.string.km);
            multiplier = 1.0F;
            Log.i("menu", "KMs selected: " + distanceSuffix);
        } else if (selectedMetricId == R.id.miles) {
            distanceSuffix = getString(R.string.miles);
            //1 km = 0.621371192 miles
            multiplier = 0.621371192F;
            Log.i("menu", "Miles selected: " + distanceSuffix);
        }
    }
    
    //DistanceCalculatorServiceListener methods
    @Override
    public void onDistanceChange(float newDistanceInMeters) {
        if(newDistanceInMeters >= 0.0) {
            errorText.setText(R.string.empty);
        }
        Log.i("serviceListener", "distance changed");
        String visualDistance = DistanceCalculatorUtilities.getVisualDistance(newDistanceInMeters, multiplier, distanceSuffix);
        distanceText.setText(visualDistance);
    }
    
    @Override
    public void onServiceStatusChange(int errorCode) {
        int textId = DistanceCalculatorUtilities.getErrorTextId(errorCode);
        if(textId > 0) {
            errorText.setText(textId);
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
}