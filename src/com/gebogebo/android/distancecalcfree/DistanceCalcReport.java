package com.gebogebo.android.distancecalcfree;

import java.io.Serializable;

/**
 * class holds values specific to report for distance calculator session
 * 
 * @author viraj
 */
public class DistanceCalcReport implements Serializable {
    private static final long serialVersionUID = -8117205561752959021L;
    
    private float totalDistance;
    private String totalDistanceString;
    private long totalTime;
    private String totalTimeString;
    private float minSpeed;
    private float maxSpeed;
    private String minSpeedString;
    private String maxSpeedString;
    private long totalTimePaused;
    private String totalTimePausedString;
    private String avgSpeed;
    private double currSpeed;
    private String currentTime;
    
    public float getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(float actualDistanceCovered) {
        this.totalDistance = actualDistanceCovered;
    }

    public String getAvgSpeed() {
        return avgSpeed;
    }

    public void setAvgSpeed(String avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public void setCurrSpeed(double currSpeed) {
        this.currSpeed = currSpeed;
    }

    public double getCurrSpeed() {
        return currSpeed;
    }

    public void setTotalDistanceString(String totalDistanceString) {
        this.totalDistanceString = totalDistanceString;
    }

    public String getTotalDistanceString() {
        return totalDistanceString;
    }

    public void setTotalTimeString(String totalTimeString) {
        this.totalTimeString = totalTimeString;
    }

    public String getTotalTimeString() {
        return totalTimeString;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public float getMinSpeed() {
        return minSpeed;
    }

    public void setMinSpeed(float minSpeed) {
        this.minSpeed = minSpeed;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public String getMinSpeedString() {
        return minSpeedString;
    }

    public void setMinSpeedString(String minSpeedString) {
        this.minSpeedString = minSpeedString;
    }

    public String getMaxSpeedString() {
        return maxSpeedString;
    }

    public void setMaxSpeedString(String maxSpeedString) {
        this.maxSpeedString = maxSpeedString;
    }

    public void setTotalTimePaused(long totalTimePaused) {
        this.totalTimePaused = totalTimePaused;
    }

    public long getTotalTimePaused() {
        return totalTimePaused;
    }

    public void setTotalTimePausedString(String totalTimePausedString) {
        this.totalTimePausedString = totalTimePausedString;
    }

    public String getTotalTimePausedString() {
        return totalTimePausedString;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public String getCurrentTime() {
        return currentTime;
    }
}