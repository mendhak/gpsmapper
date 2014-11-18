package com.mendhak.gpsvisualizer.views;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.etsy.android.grid.StaggeredGridView;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mendhak.gpsvisualizer.R;

import com.mendhak.gpsvisualizer.common.*;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class StatsFragment extends Fragment {

    private View rootView;
    private GpsTrack track;
    private boolean visibleToUser;
    private static StatType statType = StatType.DISTANCE;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_stats, container, false);

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        visibleToUser = isVisibleToUser;

        if (visibleToUser) {
            Log.d("GPSVisualizer", "Stats Fragment is now visible");

            DisplayStats();
        } else {
            Log.d("GPSVisualizer", "Stats Fragment is now invisible");
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.stats, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.stattype_selection) {

            CharSequence statTypeNames[] = new CharSequence[] {"Distance","Elevation","Speed","Time"};


            AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
            builder.setTitle("Pick a stat type");
            builder.setItems(statTypeNames, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch(which){
                        case 0:
                            statType = StatType.DISTANCE;
                            break;
                        case 1:
                            statType = StatType.ELEVATION;
                            break;
                        case 2:
                            statType = StatType.SPEED;
                            break;
                        case 3:
                            statType = StatType.TIME;
                            break;
                        default:
                            statType = StatType.DISTANCE;
                            break;
                    }
                    DisplayStats();
                }
            });
            builder.show();


            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public List<StatPoint> generateSpeedData(GpsTrack track){
        List<GpsPoint> trackPoints = track.getTrackPoints();
        List<StatPoint> statPoints = Lists.newLinkedList();

        trackPoints = Lists.newArrayList(GpsTrack.SpeedFilter(trackPoints));

        if(trackPoints.isEmpty()) { return statPoints; }

        DecimalFormat df = new DecimalFormat("#.###");

        double minimumSpeed = GpsTrack.SpeedOrdering.min(trackPoints).getSpeed().get();
        double maximumSpeed = GpsTrack.SpeedOrdering.max(trackPoints).getSpeed().get();
        statPoints.add(new StatPoint("Minimum speed", df.format(minimumSpeed) + "m/s"));
        statPoints.add(new StatPoint("Maximum speed", df.format(maximumSpeed) + "m/s"));

        double averageSpeed = 0;
        for(GpsPoint p : trackPoints){
            averageSpeed += p.getSpeed().get();
        }
        averageSpeed = averageSpeed/trackPoints.size();
        statPoints.add(new StatPoint("Average speed", df.format(averageSpeed) + "m/s"));

        //Now also remove points without elevation
        trackPoints = Lists.newArrayList(GpsTrack.ElevationFilter(trackPoints));
        double averageClimbingSpeed = 0;
        int averageClimbingCount = 0;
        double averageDescendingSpeed = 0;
        int averageDescendingCount = 0;

        for(int i = 0; i < trackPoints.size(); i++){
            if(i == 0) { continue; }

            if(trackPoints.get(i).getElevation().get() < trackPoints.get(i-1).getElevation().get()){
                averageDescendingCount++;
                averageDescendingSpeed += trackPoints.get(i-1).getSpeed().get();
            }

            if(trackPoints.get(i).getElevation().get() > trackPoints.get(i-1).getElevation().get()){
                averageClimbingCount++;
                averageClimbingSpeed += trackPoints.get(i).getSpeed().get();
            }
        }

        averageDescendingSpeed = averageDescendingSpeed/averageDescendingCount;
        averageClimbingSpeed = averageClimbingSpeed/averageClimbingCount;

        statPoints.add(new StatPoint("Climbing speed", df.format(averageClimbingSpeed) +"m/s"));
        statPoints.add(new StatPoint("Descent speed", df.format(averageDescendingSpeed) +"m/s"));


        return statPoints;

    }

    public List<StatPoint> generateDistanceData(GpsTrack track){
        List<GpsPoint> trackPoints = track.getTrackPoints();
        List<StatPoint> statPoints = Lists.newLinkedList();

        if(trackPoints.isEmpty()){ return statPoints; }

        DecimalFormat df = new DecimalFormat("#.###");

        double pointToPointDistance = Iterables.getLast(trackPoints).getAccumulatedDistance();
        statPoints.add(new StatPoint("Point to point distance", df.format(pointToPointDistance) + " m"));

        double beelineDistance = Utils.CalculateDistance(
                Iterables.getLast(trackPoints).getLatitude(),
                Iterables.getLast(trackPoints).getLongitude(),
                Iterables.getFirst(trackPoints, null).getLatitude(),
                Iterables.getFirst(trackPoints, null).getLongitude());

        statPoints.add(new StatPoint("Beeline distance", df.format(beelineDistance) + " m"));

        //Remove unelevated points for elevation based distance calculations
        trackPoints = Lists.newArrayList(GpsTrack.ElevationFilter(trackPoints));

        if(!trackPoints.isEmpty()){
            double traversedDistance = 0;
            double climbedDistance = 0;
            double descentDistance = 0;
            double flatgroundDistance = 0;
            for(int i = 0; i < trackPoints.size(); i++) {
                if (i == 0) {
                    continue;
                }

                double distanceDifference = trackPoints.get(i).getAccumulatedDistance() - trackPoints.get(i-1).getAccumulatedDistance();
                double elevationDifference = trackPoints.get(i).getElevation().get() - trackPoints.get(i-1).getElevation().get();
                double hypotenuse = Math.sqrt(Math.pow(distanceDifference,2) + Math.pow(Math.abs(elevationDifference),2));
                traversedDistance += hypotenuse;

                if(elevationDifference < 0) { descentDistance += hypotenuse; }
                if (elevationDifference > 0) { climbedDistance += hypotenuse;}
                if (elevationDifference == 0) { flatgroundDistance += hypotenuse; }


            }

            if(climbedDistance > 0){
                statPoints.add(new StatPoint("Climbed Distance", df.format(climbedDistance) + " m"));
            }

            if(descentDistance > 0){
                statPoints.add(new StatPoint("Descent Distance", df.format(descentDistance) + " m"));
            }

            if(flatgroundDistance > 0){
                statPoints.add(new StatPoint("Distance on flat ground", df.format(flatgroundDistance) + " m"));
            }

            statPoints.add(new StatPoint("Distance with elevation", df.format(traversedDistance) + " m"));
        }


        return statPoints;

    }

    public List<StatPoint> generateTimeData(GpsTrack track){
        List<GpsPoint> trackPoints = track.getTrackPoints();
        List<StatPoint> statPoints = Lists.newLinkedList();

        SimpleDateFormat sdfDate = new SimpleDateFormat("MMM dd yyyy");
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
        Date startDate = Iterables.getFirst(trackPoints,null).getCalendar().getTime();
        Date endDate = Iterables.getLast(trackPoints).getCalendar().getTime();

        statPoints.add(new StatPoint("Date", sdfDate.format(startDate)));
        statPoints.add(new StatPoint("Start Time", sdfTime.format(startDate)));
        statPoints.add(new StatPoint("End Time", sdfTime.format(endDate)));

        long timeDifferenceMillis = Iterables.getLast(trackPoints).getCalendar().getTimeInMillis() - Iterables.getFirst(trackPoints,null).getCalendar().getTimeInMillis();

        statPoints.add(new StatPoint("Total time", String.valueOf(timeDifferenceMillis/60000) + " min"));

        //Remove unelevated points for elevation based time calculations
        trackPoints = Lists.newArrayList(GpsTrack.ElevationFilter(trackPoints));
        if(!trackPoints.isEmpty()) {
            long climbingTime=0, descendingTime=0, flatGroundTime=0;

            for(int i = 0; i < trackPoints.size(); i++) {
                if (i == 0) {
                    continue;
                }

                double elevationDifference = trackPoints.get(i).getElevation().get() - trackPoints.get(i-1).getElevation().get();
                long timeDifference = trackPoints.get(i).getCalendar().getTimeInMillis() - trackPoints.get(i-1).getCalendar().getTimeInMillis();

                if(elevationDifference > 0) { climbingTime += timeDifference; }
                if(elevationDifference < 0) { descendingTime += timeDifference; }
                if(elevationDifference ==0) { flatGroundTime += timeDifference; }
            }

            if(climbingTime>0){
                statPoints.add(new StatPoint("Climbing Time",  String.valueOf(climbingTime/60000) + " min"));
            }

            if(descendingTime>0){
                statPoints.add(new StatPoint("Descent Time",  String.valueOf(descendingTime/60000) + " min"));
            }

            if(flatGroundTime>0){
                statPoints.add(new StatPoint("Flat Ground Time",  String.valueOf(flatGroundTime/60000) + " min"));
            }

            statPoints.add(new StatPoint("Time with elevation",  String.valueOf((climbingTime + descendingTime + flatGroundTime)/60000) + " min"));
        }


        return statPoints;


    }

    public List<StatPoint> generateElevationData(GpsTrack track) {

        List<GpsPoint> trackPoints = track.getTrackPoints();
        List<StatPoint> statPoints = Lists.newLinkedList();

        //Elevation of 0m is a bad data point.  Remove these.
        trackPoints = Lists.newArrayList(GpsTrack.ElevationFilter(trackPoints));

        if(trackPoints.isEmpty()){ return statPoints; }


        DecimalFormat df = new DecimalFormat("#.###");

        double startElevation = Iterables.getFirst(trackPoints,null).getElevation().get();
        double endElevation = Iterables.getLast(trackPoints, null).getElevation().get();
        statPoints.add(new StatPoint("Start Elevation", df.format(startElevation) + "m" ));
        statPoints.add(new StatPoint("End Elevation", df.format(endElevation) + "m" ));

        double minimumElevation = GpsTrack.ElevationOrdering.min(trackPoints).getElevation().get();
        double maximumElevation = GpsTrack.ElevationOrdering.max(trackPoints).getElevation().get();

        statPoints.add(new StatPoint("Minimum Elevation", df.format(minimumElevation) + "m" ));
        statPoints.add(new StatPoint("Maximum Elevation", df.format(maximumElevation) + "m" ));


        double avgElevation = 0;
        for(GpsPoint p : trackPoints){
            avgElevation += p.getElevation().get();
        }

        avgElevation = avgElevation/trackPoints.size();
        statPoints.add(new StatPoint("Average Elevation",df.format(avgElevation) + "m" ));

        double climbing = 0;
        double descending = 0;

        for(int i = 0; i < trackPoints.size(); i++){
            if(i == 0) { continue; }

            if(trackPoints.get(i).getElevation().get() < trackPoints.get(i-1).getElevation().get()){
                descending += trackPoints.get(i-1).getElevation().get() - trackPoints.get(i).getElevation().get();
            }

            if(trackPoints.get(i).getElevation().get() > trackPoints.get(i-1).getElevation().get()){
                climbing += trackPoints.get(i).getElevation().get() - trackPoints.get(i-1).getElevation().get();
            }
        }

        statPoints.add(new StatPoint("Total Climbing", df.format(climbing) + "m"));
        statPoints.add(new StatPoint("Total Descending", df.format(descending) + "m"));
        statPoints.add(new StatPoint("Net Ascent", df.format(climbing - descending) + "m"));

        return statPoints;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void DisplayStats() {

        track = ProcessedData.GetTrack();
        if(Iterables.isEmpty(track.getTrackPoints())){
            return;
        }

        List<StatPoint> statPoints = Lists.newLinkedList();

        LinearLayout layout = (LinearLayout)rootView.findViewById(R.id.linear_layout_stats_fragment);
        if(statType == StatType.DISTANCE){
            layout.setBackgroundResource(R.drawable.wallpaper_distance);
            statPoints = generateDistanceData(track);

        }
        else if (statType == StatType.ELEVATION){
            layout.setBackgroundResource(R.drawable.wallpaper_elevation);
            statPoints = generateElevationData(track);
        }
        else if (statType == StatType.SPEED){
            layout.setBackgroundResource(R.drawable.wallpaper_speed);
            statPoints = generateSpeedData(track);
        }
        else if (statType == StatType.TIME){
            layout.setBackgroundResource(R.drawable.wallpaper_time);
            statPoints = generateTimeData(track);
        }
        else {
            layout.setBackgroundResource(R.drawable.wallpaper_distance);
            statPoints = generateDistanceData(track);
        }


        StaggeredGridView staggeredGridView = (StaggeredGridView) rootView.findViewById(R.id.grid_view);
        StatsAdapter statsAdapter = new StatsAdapter(rootView.getContext(), R.id.txt_line1);

        LayoutInflater inflater = (LayoutInflater) rootView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        statsAdapter.clear();

        for(StatPoint stat: statPoints){
            statsAdapter.add("<h1><big>" + stat.Title + "</big></h1><br /> <h2>" + stat.Value + "</h2>");
        }

        //staggeredGridView.animate().
        layout.clearAnimation();

        staggeredGridView.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.fade));
        staggeredGridView.setAdapter(statsAdapter);
    }

    public static StatsFragment newInstance(int sectionNumber) {
        StatsFragment fragment = new StatsFragment();
        Bundle args = new Bundle();
        args.putInt("section_number", sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public StatsFragment() {

    }

    private enum StatType {
         DISTANCE,
         ELEVATION ,
         SPEED ,
         TIME
    }

    private class StatPoint {
        public StatPoint(String title, String value){
            Title = title;
            Value = value;
        }
        public String Title = "";
        public String Value = "";
    }
}
