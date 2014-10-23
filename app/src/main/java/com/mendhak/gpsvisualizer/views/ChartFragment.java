package com.mendhak.gpsvisualizer.views;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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

    private GpsTrack track;
    private LineChartView chart;
    private LineChartData data;






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
        View rootView = inflater.inflate(R.layout.fragment_chart, container, false);

        setHasOptionsMenu(true);

        chart = (LineChartView)rootView.findViewById(R.id.chart);

        SetupChart();

        return rootView;
    }

    private void SetupChart() {
        track = ProcessedData.GetTrack();
        if(track.getTrackPoints() != null && track.getTrackPoints().size() > 0){
            ChartParameters params = generateDataElevationOverDuration();
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

        chart.setMaxViewport(v);
        chart.setCurrentViewport(v, true);


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

        return params;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.line_chart, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.charttype_elevationovertime) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ChartParameters {
        public String XAxisName;
        public String YAxisName;
        public float YAxisTop;
        public float YAxisBottom;
        public List<AxisValue> XAxisValues;
        public List<AxisValue> YAxisValues;
        public List<PointValue> TrackPointValues;
        public List<PointValue> WayPointValues;
    }
}
