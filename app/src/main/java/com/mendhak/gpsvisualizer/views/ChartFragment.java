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


    List<PointValue> values;
    List<AxisValue> xAxisValues;
    String xAxisName;
    String yAxisName;

    float yAxisTop;
    float yAxisBottom;



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

        generateDataElevationOverDuration();
        applyToChart();

        // Disable viewpirt recalculations, see toggleCubic() method for more info.
        chart.setViewportCalculationEnabled(false);

        chart.setValueSelectionEnabled(true);


        resetViewport();
    }

    private void resetViewport() {
        final Viewport v = new Viewport(chart.getMaxViewport());

        v.top = yAxisTop;
        v.bottom = yAxisBottom;

        chart.setMaxViewport(v);
        chart.setCurrentViewport(v, false);
    }

    private void applyToChart(){
        Line line = new Line(values);
        line.setColor(Utils.COLORS[0]);
        line.setShape(ValueShape.CIRCLE);
        line.setCubic(true);
        line.setFilled(false);
        line.setHasLabels(false);
        line.setHasLabelsOnlyForSelected(true);
        line.setHasLines(true);
        line.setHasPoints(true);

        data = new LineChartData(Lists.newArrayList(line));

        Axis axisX = new Axis();
        axisX.setValues(xAxisValues);

        Axis axisY = new Axis().setHasLines(true);

        axisX.setName(xAxisName);
        axisY.setName(yAxisName);

        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);

        data.setBaseValue(Float.NEGATIVE_INFINITY);
        chart.setLineChartData(data);
    }

    private void generateDataElevationOverDuration() {
        track = ProcessedData.GetTrack();

        values = Lists.newLinkedList();
        xAxisValues = Lists.newLinkedList();

        for (int i = 0; i < track.getTrackPoints().size(); ++i) {
            values.add(new PointValue(i, track.getTrackPoints().get(i).getElevation()));

            SimpleDateFormat timeFormat = new SimpleDateFormat("H:mm");
            //String timeRepresentation =  timeFormat.format(track.getTrackPoints().get(i).getCalendar().getTime());
            long elapsedMillis = (track.getTrackPoints().get(i).getCalendar().getTimeInMillis() -
                    track.getTrackPoints().get(0).getCalendar().getTimeInMillis())/(1000*60);

            xAxisValues.add(new AxisValue(i, String.valueOf(elapsedMillis).toCharArray()));
        }



        xAxisName = "Duration (min)";
        yAxisName = "Elevation (m)";

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

        yAxisTop = elevationOrdering.max(track.getTrackPoints()).getElevation()+50;
        yAxisBottom = elevationOrdering.min(track.getTrackPoints()).getElevation()-50;

    }
}
