package com.mendhak.gpsvisualizer.views;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.mendhak.gpsvisualizer.R;
import com.mendhak.gpsvisualizer.common.GpsPoint;
import com.mendhak.gpsvisualizer.common.GpsTrack;
import com.mendhak.gpsvisualizer.common.ProcessedData;

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

        chart = (LineChartView)rootView.findViewById(R.id.chart);

        SetupChart();

        return rootView;
    }

    private void SetupChart() {
        track = ProcessedData.GetTrack();
        ChartParameters params = generateDataElevationOverDuration();
        applyToLineChart(params);
    }


    private void applyToLineChart(ChartParameters params){
        //Create the line with attributes and data
        Line line = new Line(params.PointValues);
        line.setColor(Utils.COLORS[0]);
        line.setShape(ValueShape.CIRCLE);
        line.setCubic(true);
        line.setFilled(false);
        line.setHasLabels(false);
        line.setHasLabelsOnlyForSelected(true);
        line.setHasLines(true);
        line.setHasPoints(false);

        //Pass to data (a set of lines)
        data = new LineChartData(Lists.newArrayList(line));

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

        params.PointValues = Lists.newLinkedList();
        params.XAxisValues = Lists.newLinkedList();
        params.YAxisValues = Lists.newLinkedList();

        for (int i = 0; i < track.getTrackPoints().size(); ++i) {
            long elapsedMillis = (track.getTrackPoints().get(i).getCalendar().getTimeInMillis() -
                    track.getTrackPoints().get(0).getCalendar().getTimeInMillis())/(1000*60);

            params.PointValues.add(new PointValue(elapsedMillis, track.getTrackPoints().get(i).getElevation()));
            //params.XAxisValues.add(new AxisValue(i, String.valueOf(elapsedMillis).toCharArray()));
        }


        params.XAxisName = "Elapsed time (minutes)";
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

    private class ChartParameters {
        public String XAxisName;
        public String YAxisName;
        public float YAxisTop;
        public float YAxisBottom;
        public List<AxisValue> XAxisValues;
        public List<AxisValue> YAxisValues;
        public List<PointValue> PointValues;
    }
}
