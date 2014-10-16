package com.mendhak.gpsvisualizer.common;


import com.google.common.collect.Lists;

import java.util.List;

public class GpsTrack {
    private List<GpsPoint> points = Lists.newLinkedList();

    public List<GpsPoint> getPoints() {
        return points;
    }

    public void setPoints(List<GpsPoint> points) {
        this.points = points;
    }

    public void addPoints(List<GpsPoint> points){
        this.points.addAll(points);
    }
}
