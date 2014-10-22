package com.mendhak.gpsvisualizer.common;


import com.google.common.collect.Lists;

import java.util.List;

/**
 * Holds information about a track, such as trackpoints, waypoints...
 */
public class GpsTrack {
    private List<GpsPoint> trackPoints = Lists.newLinkedList();
    private List<GpsPoint> wayPoints = Lists.newLinkedList();


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



    public List<GpsPoint> getWayPoints() {
        return wayPoints;
    }

    public void setWayPoints(List<GpsPoint> wayPoints) {
        this.wayPoints = wayPoints;
    }


}
