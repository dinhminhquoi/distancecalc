package com.gebogebo.android.distancecalc;

public interface DistanceCalculatorServiceListener {
    void onDistanceChange(float newDistanceInMeters);
    void onServiceStatusChange(int errorCode); 
}
