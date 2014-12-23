package com.mendhak.gpsvisualizer.views;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.animation.AnimationUtils;
import android.widget.TextView;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mendhak.gpsvisualizer.R;
import com.mendhak.gpsvisualizer.common.GpsPoint;
import com.mendhak.gpsvisualizer.common.GpsTrack;
import com.mendhak.gpsvisualizer.common.ProcessedData;

import java.text.SimpleDateFormat;
import java.util.List;

import lecho.lib.hellocharts.model.*;
import lecho.lib.hellocharts.util.Utils;
import lecho.lib.hellocharts.view.LineChartView;


public class ChartFragment extends Fragment{

    View rootView;
    private LineChartView chart;
    private LineChartData data;
    private static boolean visibleToUser;
    private static ChartType chartType = ChartType.DISTANCE_OVER_TIME;
    private boolean accumulatedDistanceInKms=false;

    public static ChartFragment newInstance(int sectionNumber) {
        ChartFragment fragment = new ChartFragment();
        Bundle args = new Bundle();
        args.putInt("section_number", sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ChartFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_chart, container, false);

        setHasOptionsMenu(true);

        chart = (LineChartView)rootView.findViewById(R.id.chart);

        SetupChart();

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        visibleToUser = isVisibleToUser;

        if (visibleToUser) {
            Log.d("GPSVisualizer", "Chart Fragment is now visible");

            SetupChart();
        } else {
            Log.d("GPSVisualizer", "Chart Fragment is now invisible");
        }
    }

    private void SetupChart() {

        GpsTrack track = ProcessedData.GetTrack();

        if(track.getTrackPoints() != null && track.getTrackPoints().size() > 0){
            ChartParameters params;

            if(chartType == ChartType.ELEVATION_OVER_DURATION){
                params = generateDataElevationOverDuration(track);
                SetChartTitle("Elevation/Time");
            }
            else if (chartType == ChartType.ELEVATION_OVER_DISTANCE) {
                params = generateDataElevationOverDistance(track);
                SetChartTitle("Elevation/Distance");
            }
            else if (chartType == ChartType.SPEED_OVER_DISTANCE){
                params = generateDataSpeedOverDistance(track);
                SetChartTitle("Speed/Distance");
            }
            else if (chartType == ChartType.SPEED_OVER_DURATION){
                params = generateDataSpeedOverDuration(track);
                SetChartTitle("Speed/Time");
            }
            else if(chartType == ChartType.DISTANCE_OVER_TIME){
                params = generateDataDistanceOverTime(track);
                SetChartTitle("Distance/Time");
            }
            else {
                params = generateDataDistanceOverTime(track);
                SetChartTitle("Elevation/Time");
            }

            applyToLineChart(params);
        }

    }


    private void applyToLineChart(ChartParameters params){
        //Create the lines with attributes and data
        Line trackpointLine = new Line(params.TrackPointValues);
        trackpointLine.setColor(Utils.COLORS[0]);
        trackpointLine.setShape(ValueShape.CIRCLE);
        trackpointLine.setCubic(true);
        trackpointLine.setFilled(false);
        trackpointLine.setHasLabels(false);
        trackpointLine.setHasLabelsOnlyForSelected(true);
        trackpointLine.setHasLines(true);
        trackpointLine.setHasPoints(false);

        Line waypointLine = new Line(params.WayPointValues);
        waypointLine.setColor(Utils.COLOR_RED);
        waypointLine.setShape(ValueShape.SQUARE);
        waypointLine.setHasLabels(true);
        waypointLine.setHasLabelsOnlyForSelected(true);
        waypointLine.setHasLines(false);
        waypointLine.setHasPoints(true);

        //Pass to data (a set of lines)
        data = new LineChartData(Lists.newArrayList(trackpointLine, waypointLine));

        //XAxis
        Axis axisX = new Axis();
        if(!params.XAxisValues.isEmpty()){
            axisX.setValues(params.XAxisValues);
        }

        //YAxis
        Axis axisY = new Axis().setHasLines(true);
        if(!params.YAxisValues.isEmpty()){
            axisY.setValues(params.YAxisValues);
        }

        //Axis names
        axisX.setName(params.XAxisName);
        axisX.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        axisY.setName(params.YAxisName);
        axisY.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        //Set axes
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);

        //Pass data to chart
        data.setBaseValue(Float.NEGATIVE_INFINITY);
        chart.setLineChartData(data);

        // Disable viewport recalculations, see toggleCubic() method for more info.
        chart.setViewportCalculationEnabled(false);

        //Allow selecting points
        chart.setValueSelectionEnabled(true);


        //Set Y-axis top and bottom
        final Viewport v = new Viewport(chart.getMaxViewport());
        v.top = params.YAxisTop;
        v.bottom = params.YAxisBottom;
        v.left = params.XAxisLeft;
        v.right = params.XAxisRight;

        chart.setMaxViewport(v);
        chart.setCurrentViewport(v, true);

        chart.startDataAnimation();
        chart.setAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.fade));

    }


    private ChartParameters generateDataSpeedOverDistance(GpsTrack track) {


        ChartParameters params = new ChartParameters();

        params.TrackPointValues = Lists.newLinkedList();
        params.WayPointValues = Lists.newLinkedList();
        params.XAxisValues = Lists.newLinkedList();
        params.YAxisValues = Lists.newLinkedList();

        List<GpsPoint> trackPoints = copyOfTrackPoints(track);

        //Elevation of 0m is a bad data point.  Remove these.
        trackPoints = Lists.newArrayList(GpsTrack.SpeedFilter(trackPoints));

        if(trackPoints.isEmpty()) { return params; }

        trackPoints = adjustTrackPointUnits(trackPoints);

        for (int i = 0; i < trackPoints.size(); ++i) {

            params.TrackPointValues.add(new PointValue(
                    trackPoints.get(i).getAccumulatedDistance(),
                    trackPoints.get(i).getSpeed().get()));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm '('MMM dd yyyy')'");
        params.XAxisName = "Accumulated Distance (" + (accumulatedDistanceInKms ? "km":"m") + ")";
        params.YAxisName = "Speed (m/s)";



        params.YAxisTop = GpsTrack.SpeedOrdering.max(trackPoints).getSpeed().get()*1.05f;
        params.YAxisBottom = GpsTrack.SpeedOrdering.min(trackPoints).getSpeed().get();
        params.XAxisLeft = 0;
        params.XAxisRight = trackPoints.get(trackPoints.size() - 1).getAccumulatedDistance()*1.05f;

        return params;
    }

    private List<GpsPoint> copyOfTrackPoints(GpsTrack track) {
        List<GpsPoint> trackPoints = Lists.newLinkedList();
        for(int i = 0; i < track.getTrackPoints().size(); i++){
            trackPoints.add(new GpsPoint(track.getTrackPoints().get(i)));
        }

        return trackPoints;
    }

    private List<GpsPoint> adjustTrackPointUnits(List<GpsPoint> trackPoints) {
        float maxDistance = Iterables.getLast(trackPoints).getAccumulatedDistance();
        if(maxDistance > 5000){

            accumulatedDistanceInKms=true;
            for(int i = 0; i < trackPoints.size(); ++i){
                trackPoints.get(i).setAccumulatedDistance(trackPoints.get(i).getAccumulatedDistance()/1000);
            }
        }

        return trackPoints;
    }

    private ChartParameters generateDataElevationOverDistance(final GpsTrack track) {

        ChartParameters params = new ChartParameters();

        params.TrackPointValues = Lists.newLinkedList();
        params.WayPointValues = Lists.newLinkedList();
        params.XAxisValues = Lists.newLinkedList();
        params.YAxisValues = Lists.newLinkedList();

        List<GpsPoint> trackPoints = copyOfTrackPoints(track);

        //Elevation of 0m is a bad data point.  Remove these.
        trackPoints = Lists.newArrayList(GpsTrack.ElevationFilter(trackPoints));

        if(trackPoints.isEmpty()){ return params; }

        adjustTrackPointUnits(trackPoints);

        for (int i = 0; i < trackPoints.size(); ++i) {

            params.TrackPointValues.add(new PointValue(
                    trackPoints.get(i).getAccumulatedDistance(),
                    trackPoints.get(i).getElevation().get()));
        }


        for(int i = 0; i< track.getWayPoints().size(); ++i){
            final int index = i;

            try{
                GpsPoint correspondingTrackPoint = Iterables.find(trackPoints, new Predicate<GpsPoint>() {
                    @Override
                    public boolean apply(GpsPoint input) {
                        return input.getCalendar().getTimeInMillis() == track.getWayPoints().get(index).getCalendar().getTimeInMillis();
                    }
                });

                params.WayPointValues.add(new PointValue(correspondingTrackPoint.getAccumulatedDistance(), correspondingTrackPoint.getElevation().get())
                        .setLabel(track.getWayPoints().get(i).getDescription().toCharArray()));
            }
            catch (Exception ex){

            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm '('MMM dd yyyy')'");
        params.XAxisName = "Accumulated Distance (" + (accumulatedDistanceInKms ? "km":"m") + ")";
        params.YAxisName = "Elevation (m)";

        params.YAxisTop = GpsTrack.ElevationOrdering.max(trackPoints).getElevation().get()*1.05f;
        params.YAxisBottom = GpsTrack.ElevationOrdering.min(trackPoints).getElevation().get()*0.95f;
        params.XAxisLeft = 0;
        params.XAxisRight = trackPoints.get(trackPoints.size() - 1).getAccumulatedDistance()*1.05f;

        return params;
    }

    private ChartParameters generateDataDistanceOverTime(final GpsTrack track){


        ChartParameters params = new ChartParameters();

        params.TrackPointValues = Lists.newLinkedList();
        params.WayPointValues = Lists.newLinkedList();
        params.XAxisValues = Lists.newLinkedList();
        params.YAxisValues = Lists.newLinkedList();

        List<GpsPoint> trackPoints = copyOfTrackPoints(track);

        if(trackPoints.isEmpty()) { return params; }

        trackPoints = adjustTrackPointUnits(trackPoints);


        for (int i = 0; i < trackPoints.size(); ++i) {
            long elapsedMinutes = (trackPoints.get(i).getCalendar().getTimeInMillis() -
                    trackPoints.get(0).getCalendar().getTimeInMillis())/(1000*60);

            params.TrackPointValues.add(new PointValue(elapsedMinutes, trackPoints.get(i).getAccumulatedDistance()));
        }

        for(int i = 0; i< track.getWayPoints().size(); ++i){

            try{
                final int index = i;
                GpsPoint correspondingTrackPoint = Iterables.find(trackPoints, new Predicate<GpsPoint>() {
                    @Override
                    public boolean apply(GpsPoint input) {
                        return input.getCalendar().getTimeInMillis() == track.getWayPoints().get(index).getCalendar().getTimeInMillis();
                    }
                });

                long elapsedMinutes = (correspondingTrackPoint.getCalendar().getTimeInMillis()-trackPoints.get(0).getCalendar().getTimeInMillis())/(1000*60);

                params.WayPointValues.add(new PointValue(
                        elapsedMinutes,
                        correspondingTrackPoint.getAccumulatedDistance()
                ).setLabel(track.getWayPoints().get(i).getDescription().toCharArray()));
            }
            catch(Exception e){


            }

        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm '('MMM dd yyyy')'");
        params.XAxisName = "Minutes since " + sdf.format(trackPoints.get(0).getCalendar().getTime());
        params.YAxisName = "Distance (" + (accumulatedDistanceInKms ? "km":"m") + ")";

        params.YAxisTop = Iterables.getLast(trackPoints).getAccumulatedDistance()*1.1f;
        params.YAxisBottom = Iterables.getFirst(trackPoints,null).getAccumulatedDistance();
        params.XAxisLeft = 0;
        params.XAxisRight = ((trackPoints.get(trackPoints.size() - 1).getCalendar().getTimeInMillis() -
                trackPoints.get(0).getCalendar().getTimeInMillis())/(1000*60));

        return params;

    }


    private ChartParameters generateDataSpeedOverDuration(GpsTrack track) {
        ChartParameters params = new ChartParameters();

        params.TrackPointValues = Lists.newLinkedList();
        params.WayPointValues = Lists.newLinkedList();
        params.XAxisValues = Lists.newLinkedList();
        params.YAxisValues = Lists.newLinkedList();

        List<GpsPoint> trackPoints = copyOfTrackPoints(track);

        //No speed is a bad data point.  Remove these.
        trackPoints = Lists.newArrayList(GpsTrack.SpeedFilter(trackPoints));

        if(trackPoints.isEmpty()) { return params; }

        for (int i = 0; i < trackPoints.size(); ++i) {
            long elapsedMinutes = (trackPoints.get(i).getCalendar().getTimeInMillis() -
                    trackPoints.get(0).getCalendar().getTimeInMillis())/(1000*60);

            params.TrackPointValues.add(new PointValue(elapsedMinutes, trackPoints.get(i).getSpeed().get()));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm '('MMM dd yyyy')'");
        params.XAxisName = "Minutes since " + sdf.format(trackPoints.get(0).getCalendar().getTime());
        params.YAxisName = "Speed (m/s)";

        params.YAxisTop = GpsTrack.SpeedOrdering.max(trackPoints).getSpeed().get()*1.05f;
        params.YAxisBottom = GpsTrack.SpeedOrdering.min(trackPoints).getSpeed().get();
        params.XAxisLeft = 0;
        params.XAxisRight = ((trackPoints.get(trackPoints.size()-1).getCalendar().getTimeInMillis() -
                trackPoints.get(0).getCalendar().getTimeInMillis())/(1000*60));

        return params;
    }

    private ChartParameters generateDataElevationOverDuration(final GpsTrack track) {
        ChartParameters params = new ChartParameters();

        params.TrackPointValues = Lists.newLinkedList();
        params.WayPointValues = Lists.newLinkedList();
        params.XAxisValues = Lists.newLinkedList();
        params.YAxisValues = Lists.newLinkedList();

        List<GpsPoint> trackPoints = copyOfTrackPoints(track);

        //Elevation of 0m is a bad data point.  Remove these.
        trackPoints = Lists.newArrayList(GpsTrack.ElevationFilter(trackPoints));

        if(trackPoints.isEmpty()) { return params; }

        for (int i = 0; i < trackPoints.size(); ++i) {
            long elapsedMinutes = (trackPoints.get(i).getCalendar().getTimeInMillis() -
                    trackPoints.get(0).getCalendar().getTimeInMillis())/(1000*60);

            params.TrackPointValues.add(new PointValue(elapsedMinutes, trackPoints.get(i).getElevation().get()));
        }

        for(int i = 0; i< track.getWayPoints().size(); ++i){

            final int index = i;
            try{
                GpsPoint correspondingTrackPoint = Iterables.find(trackPoints, new Predicate<GpsPoint>() {
                    @Override
                    public boolean apply(GpsPoint input) {
                        return input.getCalendar().getTimeInMillis() == track.getWayPoints().get(index).getCalendar().getTimeInMillis();
                    }
                });

                long elapsedMinutes = (correspondingTrackPoint.getCalendar().getTimeInMillis()-trackPoints.get(0).getCalendar().getTimeInMillis())/(1000*60);

                params.WayPointValues.add(new PointValue(
                        elapsedMinutes,
                        correspondingTrackPoint.getElevation().get()
                ).setLabel(track.getWayPoints().get(i).getDescription().toCharArray()));
            }
            catch(Exception ex){

            }


        }


        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm '('MMM dd yyyy')'");
        params.XAxisName = "Minutes since " + sdf.format(trackPoints.get(0).getCalendar().getTime());
        params.YAxisName = "Elevation (m)";

        params.YAxisTop = GpsTrack.ElevationOrdering.max(trackPoints).getElevation().get()*1.05f;
        params.YAxisBottom = GpsTrack.ElevationOrdering.min(trackPoints).getElevation().get()*0.95f;
        params.XAxisLeft = 0;
        params.XAxisRight = ((trackPoints.get(trackPoints.size() - 1).getCalendar().getTimeInMillis() -
                trackPoints.get(0).getCalendar().getTimeInMillis())/(1000*60));

        return params;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.charts, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.charttype_selection) {

            CharSequence chartTypeSelection[] = new CharSequence[] {"Distance over time", "Elevation over time",
                    "Elevation over distance", "Speed over distance", "Speed over time" };


            AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
            builder.setTitle("Pick a chart type");
            builder.setItems(chartTypeSelection, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch(which){
                        case 0:
                            chartType = ChartType.DISTANCE_OVER_TIME;
                            break;
                        case 1:
                            chartType = ChartType.ELEVATION_OVER_DURATION;
                            break;
                        case 2:
                            chartType = ChartType.ELEVATION_OVER_DISTANCE;
                            break;
                        case 3:
                            chartType = ChartType.SPEED_OVER_DISTANCE;
                            break;
                        case 4:
                            chartType = ChartType.SPEED_OVER_DURATION;
                            break;
                        default:
                            chartType = ChartType.DISTANCE_OVER_TIME;
                            break;
                    }
                    SetupChart();
                }
            });
            builder.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void SetChartTitle(String title){
        TextView tvTitle = (TextView) rootView.findViewById(R.id.txtChartTitle);
        tvTitle.setText(title);

    }

    private enum ChartType {
       ELEVATION_OVER_DURATION,
       ELEVATION_OVER_DISTANCE,
       SPEED_OVER_DISTANCE,
       SPEED_OVER_DURATION,
       DISTANCE_OVER_TIME
    }

    private class ChartParameters {
        public String XAxisName;
        public String YAxisName;
        public float YAxisTop;
        public float YAxisBottom;
        public float XAxisLeft;
        public float XAxisRight;
        public List<AxisValue> XAxisValues;
        public List<AxisValue> YAxisValues;
        public List<PointValue> TrackPointValues;
        public List<PointValue> WayPointValues;
    }
}
