package com.mendhak.gpsvisualizer.common;


import android.support.annotation.Nullable;

import com.google.common.base.Optional;

/**
 * Represents a single GPS point with information such as lat, long, elevation, description, speed, bearing...
 */
public class GpsPoint {
    private float latitude;
    private float longitude;
    private float elevation;
    private String description;


    public static GpsPoint from(float latitude, float longitude, @Nullable Float elevation){
        GpsPoint p = new GpsPoint();
        p.setLatitude(latitude);
        p.setLongitude(longitude);
        p.setElevation(Optional.fromNullable(elevation).or(0f));
        return p;
    }

    public static GpsPoint wayPoint(float latitude, float longitude, String description){
        GpsPoint p = new GpsPoint();
        p.setLatitude(latitude);
        p.setLongitude(longitude);
        p.setDescription(description);
        return p;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getElevation() {
        return elevation;
    }

    public void setElevation(float elevation) {
        this.elevation = elevation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
