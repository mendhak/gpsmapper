package com.mendhak.gpsvisualizer.views;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.mendhak.gpsvisualizer.R;
import com.mendhak.gpsvisualizer.common.GpsPoint;
import com.mendhak.gpsvisualizer.common.GpsTrack;
import com.mendhak.gpsvisualizer.common.ProcessedData;

import java.text.SimpleDateFormat;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.Utils;
import lecho.lib.hellocharts.view.LineChartView;


public class ChartFragment extends Fragment{

    View rootView;
    private GpsTrack track;
    private LineChartView chart;
    private LineChartData data;
    private static int chartType;
    private static int ELEVATION_OVER_DURATION = 0;
    private static int ELEVATION_OVER_DISTANCE = 1;



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

    private void SetupChart() {
        track = ProcessedData.GetTrack();
        if(track.getTrackPoints() != null && track.getTrackPoints().size() > 0){
            ChartParameters params;

            if(chartType == ELEVATION_OVER_DURATION){

                params = generateDataElevationOverDuration();
            }
            else {
                params = generateDataElevationOverDistance();
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
        if(params.XAxisValues != null && params.XAxisValues.size() > 0){
            axisX.setValues(params.XAxisValues);
        }

        //YAxis
        Axis axisY = new Axis().setHasLines(true);
        if(params.YAxisValues != null && params.YAxisValues.size() > 0){
            axisY.setValues(params.YAxisValues);
        }

        //Axis names
        axisX.setName(params.XAxisName);
        axisY.setName(params.YAxisName);

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


    }


    private ChartParameters generateDataElevationOverDistance() {
        ChartParameters params = new ChartParameters();

        params.TrackPointValues = Lists.newLinkedList();
        params.WayPointValues = Lists.newLinkedList();
        params.XAxisValues = Lists.newLinkedList();
        params.YAxisValues = Lists.newLinkedList();



        for (int i = 0; i < track.getTrackPoints().size(); ++i) {

            params.TrackPointValues.add(new PointValue(
                    track.getTrackPoints().get(i).getAccummulatedDistance(),
                    track.getTrackPoints().get(i).getElevation()));
            //params.XAxisValues.add(new AxisValue(i, String.valueOf(elapsedMillis).toCharArray()));
        }


        for(int i = 0; i< track.getWayPoints().size(); ++i){
            final int index = i;
            GpsPoint correspondingTrackPoint = Iterables.find(track.getTrackPoints(), new Predicate<GpsPoint>() {
                @Override
                public boolean apply(GpsPoint input) {
                    return input.getCalendar().getTimeInMillis() == track.getWayPoints().get(index).getCalendar().getTimeInMillis();
                }
            });

            params.WayPointValues.add(new PointValue(correspondingTrackPoint.getAccummulatedDistance(), correspondingTrackPoint.getElevation())
                    .setLabel(track.getWayPoints().get(i).getDescription().toCharArray()));
        }


        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm '('MMM dd yyyy')'");
        params.XAxisName = "Accummulated Distance (m)";
        params.YAxisName = "Elevation (m)";

        Ordering<GpsPoint> elevationOrdering = new Ordering<GpsPoint>() {
            @Override
            public int compare(GpsPoint left, GpsPoint right) {

                if(left.getElevation() > right.getElevation()){
                    return 1;
                }
                if(left.getElevation() < right.getElevation()){
                    return -1;
                }
                return 0;
            }
        };

        params.YAxisTop = elevationOrdering.max(track.getTrackPoints()).getElevation()+50;
        params.YAxisBottom = elevationOrdering.min(track.getTrackPoints()).getElevation()-50;
        params.XAxisLeft = 0;
        params.XAxisRight = track.getTrackPoints().get(track.getTrackPoints().size()-1).getAccummulatedDistance()+50;

        return params;
    }

    private ChartParameters generateDataElevationOverDuration() {
        ChartParameters params = new ChartParameters();

        params.TrackPointValues = Lists.newLinkedList();
        params.WayPointValues = Lists.newLinkedList();
        params.XAxisValues = Lists.newLinkedList();
        params.YAxisValues = Lists.newLinkedList();

        for (int i = 0; i < track.getTrackPoints().size(); ++i) {
            long elapsedMillis = (track.getTrackPoints().get(i).getCalendar().getTimeInMillis() -
                    track.getTrackPoints().get(0).getCalendar().getTimeInMillis())/(1000*60);

            params.TrackPointValues.add(new PointValue(elapsedMillis, track.getTrackPoints().get(i).getElevation()));
            //params.XAxisValues.add(new AxisValue(i, String.valueOf(elapsedMillis).toCharArray()));
        }

        for(int i = 0; i< track.getWayPoints().size(); ++i){
            params.WayPointValues.add(new PointValue(
                    (track.getWayPoints().get(i).getCalendar().getTimeInMillis() -
                    track.getTrackPoints().get(0).getCalendar().getTimeInMillis())/(1000*60),
                    track.getWayPoints().get(i).getElevation()
            ).setLabel(track.getWayPoints().get(i).getDescription().toCharArray()));
        }


        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm '('MMM dd yyyy')'");
        params.XAxisName = "Minutes since " + sdf.format(track.getTrackPoints().get(0).getCalendar().getTime());
        params.YAxisName = "Elevation (m)";

        Ordering<GpsPoint> elevationOrdering = new Ordering<GpsPoint>() {
            @Override
            public int compare(GpsPoint left, GpsPoint right) {

                if(left.getElevation() > right.getElevation()){
                    return 1;
                }
                if(left.getElevation() < right.getElevation()){
                    return -1;
                }
                return 0;
            }
        };

        params.YAxisTop = elevationOrdering.max(track.getTrackPoints()).getElevation()+50;
        params.YAxisBottom = elevationOrdering.min(track.getTrackPoints()).getElevation()-50;
        params.XAxisLeft = 0;
        params.XAxisRight = ((track.getTrackPoints().get(track.getTrackPoints().size()-1).getCalendar().getTimeInMillis() -
                track.getTrackPoints().get(0).getCalendar().getTimeInMillis())/(1000*60));

        return params;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.line_chart, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.charttype_selection) {

            CharSequence colors[] = new CharSequence[] {"Elevation over time",
                    "Elevation over distance"};


            AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
            builder.setTitle("Pick a map type");
            builder.setItems(colors, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch(which){
                        case 0:
                            chartType = ELEVATION_OVER_DURATION;

                            break;
                        case 1:
                            chartType = ELEVATION_OVER_DISTANCE;
                            break;

                        default:
                            chartType = ELEVATION_OVER_DURATION;
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
