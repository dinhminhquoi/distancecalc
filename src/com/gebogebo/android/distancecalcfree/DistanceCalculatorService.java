package com.gebogebo.android.distancecalcfree;

import java.util.HashSet;
import java.util.Set;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class DistanceCalculatorService extends Service implements LocationListener {
    private final Set<DistanceCalculatorServiceListener> LISTENERS = 
        new HashSet<DistanceCalculatorServiceListener>();
    
    private static final int MIN_ACCURACY = 50; //minimum accuracy in meters
    private Location prevLocation = null;
    private float actualDistanceCovered = -1.0F;
    private long startTimeInSecs = -1L;
    private final DistanceServiceBinder binder = new DistanceServiceBinder();
    
    /**
     * adds listener to the service
     *  
     * @param listener listener to be added
     */
    public void addListener(DistanceCalculatorServiceListener listener) {
        LISTENERS.add(listener);
        if(actualDistanceCovered > 0.0) {
            updateListenersWithDistance(null);
        }
    }
    
    /**
     * removes listener from the list of interested parties
     * 
     * @param listener listener to be removed
     */
    public void removeListener(DistanceCalculatorServiceListener listener) {
        LISTENERS.remove(listener);
    }
    
    /**
     * start this service
     */
    public void start() {
        goForeground();
        startTimeInSecs = System.currentTimeMillis() / 1000;
        startTimeInSecs--; //decrement time so that 0 is never returned as time elapsed
        SharedPreferences defPref = PreferenceManager.getDefaultSharedPreferences(this);
        //default selection is miles
        String action = defPref.getString(DistanceCalculatorPrefActivity.PREF_KEY_ACTION, 
        		DistanceCalculatorPrefActivity.PREF_BIKING);
        Integer gpsFreqInMillis = DistanceCalculatorPrefActivity.getFrequencyInMillisForAction(action);
        Integer gpsFreqInDistance = DistanceCalculatorPrefActivity.getFrequencyInMetersForAction(action);
        Log.i("service", "action " + action + ": distance=" + gpsFreqInDistance + " ,freq=" + gpsFreqInMillis);
        LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, gpsFreqInMillis, gpsFreqInDistance, this);
        Log.i("service", "Started listening to location changes");
    }
    
    /**
     * stop this service
     */
    public void stop() {
        LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        locationManager.removeUpdates(this);
        stopForeground(true);
        prevLocation = null;
        actualDistanceCovered = -1.0F;
        startTimeInSecs = -1L;
        Log.i("service", "Stopped listening to location changes");
    }
    
    /**
     * returns distance covered so far (since service was start using {@link #start()})
     * @return
     */
    public float getCurrentDistance() {
        return actualDistanceCovered;
    }
    
    /**
     * returns number of seconds elapsed since service was started
     * @return
     */
    public long getTimeElapsed() {
    	return System.currentTimeMillis()/1000 - startTimeInSecs;
    }
    
    /**
     * updates all interested parties with change in distance
     * 
     * @param latestLocation latest location recorded
     */
    private void updateListenersWithDistance(Location latestLocation) {
        for(DistanceCalculatorServiceListener listener : LISTENERS) {
            listener.onDistanceChange(actualDistanceCovered, getTimeElapsed(), latestLocation);
        }
    }
    
    /**
     * updates all interested parties if there was any change in service's availability
     *  
     * @param errorCode error code, if any, resulting from change in status
     */
    private void updateListenersWithServiceAvailability(int errorCode) {
        for(DistanceCalculatorServiceListener listener : LISTENERS) {
            listener.onServiceStatusChange(errorCode);
        }
    }
    
    /**
     * bring service to foreground. this prevent service/app from being killed by android's task manager
     */
    private void goForeground() {
        Notification notification = new Notification(R.drawable.ruler_status, getString(R.string.notificationTitle), System.currentTimeMillis());
        Context context = getApplicationContext();
        CharSequence contentTitle = getString(R.string.notificationTitle);
        CharSequence contentText = getString(R.string.notificationMsg);
        Intent notificationIntent = new Intent(this, DistanceCalculatorActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
        startForeground(1, notification);
        Log.i("service", "service brought to foreground");
    }
    
    //location listener overrides
    @Override
    public void onLocationChanged(Location newLocation) {
        if(newLocation.getAccuracy() > MIN_ACCURACY) {
        	Log.i("service", "accuracy too low: " + newLocation.getAccuracy());
        	return;
        }
        if(prevLocation != null) {
            float distance = newLocation.distanceTo(prevLocation);
            prevLocation = newLocation;
            actualDistanceCovered += distance;
            updateListenersWithDistance(newLocation);
        } else {
            prevLocation = newLocation;
            actualDistanceCovered = 0.0F;
            updateListenersWithDistance(newLocation);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        updateListenersWithServiceAvailability(LocationProvider.TEMPORARILY_UNAVAILABLE);
    }

    @Override
    public void onProviderEnabled(String arg0) {
        updateListenersWithServiceAvailability(LocationProvider.AVAILABLE);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if(provider.equals(LocationManager.GPS_PROVIDER)) {
            Log.d("service", "GPS service status change");
            updateListenersWithServiceAvailability(status);
        }
    }
    
    //Service lifecycle methods
    @Override
    public void onCreate() {
        Log.i("service", "Service created");
        super.onCreate();
    }
    
    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        Log.i("service", "On start command called");
        super.onStartCommand(i, flags, startId);
        return Service.START_NOT_STICKY;
    }
    
    @Override
    public void onDestroy() {
        Log.i("service", "Service is being destroyed");
        stop();
        super.onDestroy();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    class DistanceServiceBinder extends Binder {
        DistanceCalculatorService getService() {
            return DistanceCalculatorService.this;
        }
    }
}
