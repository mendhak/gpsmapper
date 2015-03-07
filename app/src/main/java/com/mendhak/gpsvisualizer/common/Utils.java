package com.mendhak.gpsvisualizer.common;


import android.content.Context;
import android.content.pm.PackageManager;

import java.text.DecimalFormat;

public class Utils {
    /**
     * Uses the Haversine formula to calculate the distnace between to lat-long coordinates
     *
     * @param latitude1  The first point's latitude
     * @param longitude1 The first point's longitude
     * @param latitude2  The second point's latitude
     * @param longitude2 The second point's longitude
     * @return The distance between the two points in meters
     */
    public static double CalculateDistance(double latitude1, double longitude1, double latitude2, double longitude2) {
        /*
            Haversine formula:
            A = sin²(Δlat/2) + cos(lat1).cos(lat2).sin²(Δlong/2)
            C = 2.atan2(√a, √(1−a))
            D = R.c
            R = radius of earth, 6371 km.
            All angles are in radians
            */

        double deltaLatitude = Math.toRadians(Math.abs(latitude1 - latitude2));
        double deltaLongitude = Math.toRadians(Math.abs(longitude1 - longitude2));
        double latitude1Rad = Math.toRadians(latitude1);
        double latitude2Rad = Math.toRadians(latitude2);

        double a = Math.pow(Math.sin(deltaLatitude / 2), 2) +
                (Math.cos(latitude1Rad) * Math.cos(latitude2Rad) * Math.pow(Math.sin(deltaLongitude / 2), 2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return 6371 * c * 1000; //Distance in meters

    }

    public static boolean IsPackageInstalled(String packagename, Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static String GetSpeedDisplay(double metersPerSecond, boolean imperial){

        DecimalFormat df = new DecimalFormat("#.###");
        String result = df.format(metersPerSecond) + " m/s";

        if(imperial){
            result = df.format(metersPerSecond * 2.23693629) + " mi/h";
        }
        else if(metersPerSecond >= 0.28){
            result = df.format(metersPerSecond * 3.6) + " km/h";
        }

        return result;

    }

    public static String GetDistanceDisplay(double meters, boolean imperial) {
        DecimalFormat df = new DecimalFormat("#.###");
        String result = df.format(meters) + " m";

        if(imperial){
            if (meters <= 804){
                result = df.format(meters * 3.2808399) + " ft";
            }
            else {
                result = df.format(meters/1609.344) + " mi";
            }

        }
        else if(meters >= 1000){
            result = df.format(meters/1000) + " km";
        }

        return result;
    }

    public static String GetTimeDisplay(long milliseconds) {

        double ms = (double)milliseconds;
        DecimalFormat df = new DecimalFormat("#.##");

        String result = df.format(ms/1000) + " s";

        if(ms > 3600000){
            result = df.format(ms/3600000) + " hrs";
        }
        else if(ms > 60000){
            result = df.format(ms/60000) + " min";
        }

        return result;
    }
}
