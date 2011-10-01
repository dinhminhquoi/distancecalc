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
    private enum SERVICE_MODE {
        starting, started, stopped, paused, resuming, resumed
    };

    private final Set<DistanceCalculatorServiceListener> LISTENERS = new HashSet<DistanceCalculatorServiceListener>();

    private static final int MIN_ACCURACY = 50; // minimum accuracy in meters
    private Location prevLocation = null;
    private float actualDistanceCovered = -1.0F;
    private long startTimeInSecs = -1L;
    private SERVICE_MODE currentServiceMode = SERVICE_MODE.stopped;
    private String action = null;
    private long totalPausedTimeInMillis = 0l;
    private long pausedStartTimeInMillis = 0l;
    private final DistanceServiceBinder binder = new DistanceServiceBinder();
    private float minSpeed = Float.MAX_VALUE;
    private float maxSpeed = 0f;

    /**
     * adds listener to the service
     * 
     * @param listener listener to be added
     */
    public void addListener(DistanceCalculatorServiceListener listener) {
        LISTENERS.add(listener);
        if (actualDistanceCovered > 0.0) {
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
        setServiceParameters(SERVICE_MODE.starting);
    }

    /**
     * stop this service
     */
    public void stop() {
        setServiceParameters(SERVICE_MODE.stopped);
    }

    /**
     * called when service is to be paused. service stops listening to location events and also exclude this time from
     * total time spent by app
     */
    public void pause() {

        setServiceParameters(SERVICE_MODE.paused);
    }

    /**
     * resumes service's functionality from paused mode. if service is not paused when this is called, no action is
     * taken
     */
    public void resume() {
        setServiceParameters(SERVICE_MODE.resuming);
    }

    /**
     * returns distance covered so far (since service was start using {@link #start()})
     * 
     * @return
     */
    public float getCurrentDistance() {
        return actualDistanceCovered;
    }

    /**
     * returns number of seconds elapsed since service was started
     * 
     * @return
     */
    public long getTimeElapsed() {
        if(startTimeInSecs <= 0) {
            return 0l;
        }
        return (System.currentTimeMillis() - totalPausedTimeInMillis) / 1000 - startTimeInSecs;
    }

    /**
     * sets service parameters as per current mode in which service is running
     * usually takes care of modes which are changed based on user action
     * 
     * @param mode service mode in which service is currently running in
     */
    private void setServiceParameters(SERVICE_MODE mode) {
        switch (mode) {
            case starting:
                prevLocation = null;
                actualDistanceCovered = -1.0F;
                totalPausedTimeInMillis = 0l;
                pausedStartTimeInMillis = 0l;
                minSpeed = Float.MAX_VALUE;
                maxSpeed = 0f;
                SharedPreferences defPref = PreferenceManager.getDefaultSharedPreferences(this);
                action = defPref.getString(DistanceCalculatorPrefActivity.PREF_KEY_ACTION,
                        DistanceCalculatorPrefActivity.PREF_BIKING);
                startListeningToLocationEvents();
                goForeground();
                break;
            case paused:
                if (SERVICE_MODE.paused.equals(currentServiceMode)) {
                    return; // already paused. nothing to do
                }
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                locationManager.removeUpdates(this);
                if(SERVICE_MODE.started.equals(currentServiceMode) || 
                        SERVICE_MODE.resumed.equals(currentServiceMode)) {
                    //only if he have started time, we care about pausedStartTime
                    pausedStartTimeInMillis = System.currentTimeMillis();
                }
                prevLocation = null;
                break;
            case stopped:
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                locationManager.removeUpdates(this);
                stopForeground(true);
                prevLocation = null;
                actualDistanceCovered = -1.0F;
                startTimeInSecs = -1L;
                totalPausedTimeInMillis = 0l;
                action = null;
                break;
            case resuming:
                if (!SERVICE_MODE.paused.equals(currentServiceMode)) {
                    return; // nothing to do here as service is not in paused mode
                }
                startListeningToLocationEvents();
                break;
            default:
                break;
        }
        Log.i("service", "mode changed to " + mode);
        currentServiceMode = mode;
    }

    /**
     * starts listening to location events from locationmanager. location events parameters are obtained from the
     * parameters which are set by user
     */
    private void startListeningToLocationEvents() throws IllegalStateException {
        Integer gpsFreqInMillis = DistanceCalculatorPrefActivity.getFrequencyInMillisForAction(action);
        Integer gpsFreqInDistance = DistanceCalculatorPrefActivity.getFrequencyInMetersForAction(action);
        Log.i("service", "action " + action + ": distance=" + gpsFreqInDistance + " ,freq=" + gpsFreqInMillis);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        validateGpsEnabled();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, gpsFreqInMillis, gpsFreqInDistance, this);
        Log.i("service", "Started listening to location changes");
    }
    
    /**
     * validates if GPS is enabled or not. if it is not enabled, {@link IllegalStateException} is thrown
     */
    private void validateGpsEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.w("service", "gps is not activated. throwing exception");
            throw new IllegalStateException("GPS service is not enabled");
        }
    }
    
    /**
     * updates all interested parties with change in distance
     * 
     * @param latestLocation latest location recorded
     */
    private void updateListenersWithDistance(Location latestLocation) {
        for (DistanceCalculatorServiceListener listener : LISTENERS) {
            listener.onDistanceChange(actualDistanceCovered, getTimeElapsed(), latestLocation);
        }
    }

    /**
     * updates all interested parties if there was any change in service's availability
     * 
     * @param errorCode error code, if any, resulting from change in status
     */
    private void updateListenersWithServiceAvailability(int errorCode) {
        for (DistanceCalculatorServiceListener listener : LISTENERS) {
            listener.onServiceStatusChange(errorCode);
        }
    }

    /**
     * bring service to foreground. this prevent service/app from being killed by android's task manager
     */
    private void goForeground() {
        Notification notification = new Notification(R.drawable.ruler_status, getString(R.string.notificationTitle),
                System.currentTimeMillis());
        Context context = getApplicationContext();
        CharSequence contentTitle = getString(R.string.notificationTitle);
        CharSequence contentText = getString(R.string.notificationMsg);
        Intent notificationIntent = new Intent(this, DistanceCalculatorActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
        startForeground(1, notification);
        Log.i("service", "service brought to foreground");
    }

    /**
     * this method listen to location changes and also changes service modes. modes which are dependent on location change
     * are usually taken care of by this method
     * 
     * @param newLocation new location which is returned by GPS
     */
    @Override
    public void onLocationChanged(Location newLocation) {
        if (newLocation.getAccuracy() > MIN_ACCURACY) {
            Log.i("service", "accuracy too low: " + newLocation.getAccuracy());
            return;
        }
        switch(currentServiceMode) {
            case started:
            case resumed:
                // for both.. started and resumed
                float distance = newLocation.distanceTo(prevLocation);
                actualDistanceCovered += distance;
                break;
            case starting:
                //if previous location is null and if mode is started, only then we will reset actualDistanceCovered
                //if it was resumed, we need not set actualDistanceCovered
//                Log.i("service", "prev location is null and service is just started");
                startTimeInSecs = System.currentTimeMillis() / 1000;
                startTimeInSecs--; // decrement time so that 0 is never returned as time elapsed
                actualDistanceCovered = 0.0F;
                setServiceParameters(SERVICE_MODE.started);
                break;
            case resuming:
                if(pausedStartTimeInMillis > 0l) {
                    //only if we have had noted down pausedStartTime, it makes sense to subtrat it from currMillis
                    totalPausedTimeInMillis += System.currentTimeMillis() - pausedStartTimeInMillis;
                }
                pausedStartTimeInMillis = 0l;
                setServiceParameters(SERVICE_MODE.resumed);
                break;
            case paused:
            case stopped:
                // if service is in paused state, ignore these msgs
                Log.i("service", "paused or stopped mode.. nothing to update");
                return;
            default:
                return;
        }
        prevLocation = newLocation;
        updateListenersWithDistance(newLocation);
        updateReportParameters();
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
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            Log.d("service", "GPS service status change");
            updateListenersWithServiceAvailability(status);
        }
    }

    // Service lifecycle methods
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
    
    /**
     * updates parameters which are used to generate report for the session. called after latest obtained location is set to
     * previous location
     */
    private void updateReportParameters() {
        // updates min and max speed only if previous location has speed
        if (prevLocation != null && prevLocation.hasSpeed()) {
            float currentSpeed = prevLocation.getSpeed();
            if(minSpeed > currentSpeed && currentSpeed > 0f) {
                minSpeed = currentSpeed; 
            }
            if(maxSpeed < currentSpeed) {
                maxSpeed = currentSpeed; 
            }
        }
    }
    
    /**
     * obtains report for this session. if service is not started yet, null is returned.
     * 
     * @return
     */
    public DistanceCalcReport getSummaryReport() {
        //method is not called as frequently. not very critical to return as fast as we can
        if(currentServiceMode.equals(SERVICE_MODE.stopped)) {
            return null;
        }
        DistanceCalcReport report = new DistanceCalcReport();
        report.setTotalDistance(actualDistanceCovered);
        report.setTotalTime(getTimeElapsed());
        report.setTotalTimePaused(totalPausedTimeInMillis > 0 ? totalPausedTimeInMillis/1000 : totalPausedTimeInMillis);
        report.setMinSpeed(minSpeed == Float.MAX_VALUE ? 0f : minSpeed);
        report.setMaxSpeed(maxSpeed);
        return report;
    }
}
