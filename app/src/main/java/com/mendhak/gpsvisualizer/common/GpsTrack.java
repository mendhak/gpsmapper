package com.mendhak.gpsvisualizer.common;


import com.google.common.collect.Lists;

import java.util.List;

/**
 * Holds information about a track, such as trackpoints, waypoints...
 */
public class GpsTrack {
    private List<GpsPoint> trackPoints = Lists.newLinkedList();


    /**
     * Returns trackpoints for the held track
     * @return
     */
    public List<GpsPoint> getTrackPoints() {
        return trackPoints;
    }

    /**
     * Sets trackpoints for the held track
     * @param trackPoints
     */
    public void setTrackPoints(List<GpsPoint> trackPoints) {
        this.trackPoints = trackPoints;
    }

    /**
     * Adds to the existing trackpoints
     * @param trackPoints
     */
    public void addTrackpoints(List<GpsPoint> trackPoints){
        this.trackPoints.addAll(trackPoints);
    }
}
