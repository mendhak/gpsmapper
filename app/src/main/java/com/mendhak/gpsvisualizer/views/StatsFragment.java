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
import java.util.ArrayList;
import java.util.List;


public class StatsFragment extends Fragment {

    private View rootView;
    private GpsTrack track;
    private boolean visibleToUser;
    private static int statType = StatType.ELEVATION;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_stats, container, false);

        setHasOptionsMenu(true);

        DisplayStats();
        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        visibleToUser = isVisibleToUser;

        if (visibleToUser) {
            Log.d("GPSVisualizer", "Chart Fragment is now visible");

            DisplayStats();
        } else {
            Log.d("GPSVisualizer", "Chart Fragment is now invisible");
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

            CharSequence statTypeNames[] = new CharSequence[] {"Elevation","Speed","Time","Distance"};


            AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
            builder.setTitle("Pick a stat type");
            builder.setItems(statTypeNames, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch(which){
                        case 0:
                            statType = StatType.ELEVATION;
                            break;
                        case 1:
                            statType = StatType.SPEED;
                            break;
                        case 2:
                            statType = StatType.TIME;
                            break;
                        case 3:
                            statType = StatType.DISTANCE;
                            break;
                        default:
                            statType = StatType.ELEVATION;
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

        for(GpsPoint p: trackPoints){


        }

        return statPoints;

    }

    public List<StatPoint> generateElevationData(GpsTrack track) {

        List<GpsPoint> trackPoints = track.getTrackPoints();
        List<StatPoint> statPoints = Lists.newLinkedList();

        //Elevation of 0m is a bad data point.  Remove these.
        trackPoints = Lists.newArrayList(Iterables.filter(trackPoints, new Predicate<GpsPoint>() {
            @Override
            public boolean apply(GpsPoint input) {
                return input.getElevation().isPresent();
            }
        }));

        if(trackPoints.size() == 0){ return statPoints; }


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

        }
        else if (statType == StatType.ELEVATION){
            layout.setBackgroundResource(R.drawable.wallpaper_elevation);
            statPoints = generateElevationData(track);
        }
        else if (statType == StatType.SPEED){
            layout.setBackgroundResource(R.drawable.wallpaper_speed);
            statPoints = generateSpeedData(track);
        }
        else {
            layout.setBackgroundResource(R.drawable.wallpaper_time);
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

    private static class StatType {
        public static final int ELEVATION = 0;
        public static final int SPEED = 1;
        public static final int TIME = 2;
        public static final int DISTANCE = 3;
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
