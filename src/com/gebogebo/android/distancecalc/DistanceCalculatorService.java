package com.gebogebo.android.distancecalc;

import java.util.HashSet;
import java.util.Set;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class DistanceCalculatorService extends Service implements LocationListener {
    private static final int MIN_DISTANCE = 20;
    private final Set<DistanceCalculatorServiceListener> LISTENERS = 
        new HashSet<DistanceCalculatorServiceListener>();
    
    private Location prevLocation = null;
    private float actualDistanceCovered = -1.0F;
    private final DistanceServiceBinder binder = new DistanceServiceBinder();
    
    @Override
    public void onLocationChanged(Location newLocation) {
        Log.i("locList", "handling location change event: " + newLocation.getAccuracy());
        if(prevLocation == null) {
            if(newLocation.getAccuracy() < 200.0) {
                //only if accuracy is better than 200m, care about new location during startup
                Log.i("locList", "previous location wasn't set. setting location");
                prevLocation = newLocation;
                actualDistanceCovered = 0.0F;
                updateListenersWithDistance();
            }
        } else {
            float distance = newLocation.distanceTo(prevLocation);
            Log.i("locList", "previous location was set. updating distance " + distance);
            prevLocation = newLocation;
            actualDistanceCovered += distance;
            Log.i("locList", "distance between previous and new location is " + distance);
            updateListenersWithDistance();
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
            Log.i("distanceCalcService", "GPS service status change");
            updateListenersWithServiceAvailability(status);
        }
    }
    
    public void addListener(DistanceCalculatorServiceListener listener) {
        LISTENERS.add(listener);
        if(actualDistanceCovered > 0.0) {
            updateListenersWithDistance();
        }
    }
    
    public void removeListener(DistanceCalculatorServiceListener listener) {
        LISTENERS.remove(listener);
    }
    
    public void start() {
        goForeground();
        LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, MIN_DISTANCE, this);
        Log.i("distCal", "Started listening to location changes");
    }
    
    public void stop() {
        LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        locationManager.removeUpdates(this);
        stopForeground(true);
        prevLocation = null;
        actualDistanceCovered = 0.0F;
        Log.i("distCal", "Stopped listening to location changes");
    }
    
    public float getCurrentDistance() {
        return actualDistanceCovered;
    }
    
    private void updateListenersWithDistance() {
        for(DistanceCalculatorServiceListener listener : LISTENERS) {
            listener.onDistanceChange(actualDistanceCovered);
        }
    }
    
    private void updateListenersWithServiceAvailability(int errorCode) {
        for(DistanceCalculatorServiceListener listener : LISTENERS) {
            listener.onServiceStatusChange(errorCode);
        }
    }
    
    private void goForeground() {
        Notification notification = new Notification(R.drawable.ruler_status, getString(R.string.notificationTitle), System.currentTimeMillis());
        Context context = getApplicationContext();
        CharSequence contentTitle = getString(R.string.notificationTitle);
        CharSequence contentText = getString(R.string.notificationMsg);
        Intent notificationIntent = new Intent(this, DistanceCalculatorActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
        startForeground(1, notification);
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
