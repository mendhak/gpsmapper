package com.mendhak.gpsvisualizer.common;


import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

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


    public static Ordering<GpsPoint> ElevationOrdering = new Ordering<GpsPoint>() {
        @Override
        public int compare(GpsPoint left, GpsPoint right) {

            if(left.getElevation().get() > right.getElevation().get()){
                return 1;
            }
            if(left.getElevation().get() < right.getElevation().get()){
                return -1;
            }
            return 0;
        }
    };

    public static Ordering<GpsPoint> SpeedOrdering = new Ordering<GpsPoint>() {
        @Override
        public int compare(GpsPoint left, GpsPoint right) {

            if(left.getSpeed().get() > right.getSpeed().get()){
                return 1;
            }
            if(left.getSpeed().get() < right.getSpeed().get()){
                return -1;
            }
            return 0;
        }
    };

    public static Iterable<GpsPoint> ElevationFilter(List<GpsPoint> trackPoints){
        return Iterables.filter(trackPoints, new Predicate<GpsPoint>() {
            @Override
            public boolean apply(GpsPoint input) {
                return input.getSpeed().isPresent();
            }
        });
    }

    public static Iterable<GpsPoint> SpeedFilter(List<GpsPoint> trackPoints){
        return Iterables.filter(trackPoints, new Predicate<GpsPoint>() {
            @Override
            public boolean apply(GpsPoint input) {
                return input.getSpeed().isPresent();
            }
        });
    }

}
