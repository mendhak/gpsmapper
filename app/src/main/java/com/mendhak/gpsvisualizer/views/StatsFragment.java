package com.mendhak.gpsvisualizer.views;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.etsy.android.grid.StaggeredGridView;
import com.mendhak.gpsvisualizer.R;

import com.mendhak.gpsvisualizer.common.GpsTrack;
import com.mendhak.gpsvisualizer.common.ProcessedData;
import com.mendhak.gpsvisualizer.common.StatsAdapter;

import java.util.ArrayList;
import java.util.List;


public class StatsFragment extends Fragment {

    private View rootView;
    private GpsTrack track;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_stats, container, false);

        DisplayStats();
        return rootView;
    }

    public ArrayList<String> generateSampleData() {
        final ArrayList<String> data = new ArrayList<String>(44);

//        for (int i = 0; i < 44; i++) {
//            data.add("Altitude of something");
//        }

        data.add("<h2>Minimum elevation</h2><br /> <h3>4</h3>");
        data.add("<h2>Maximum elevation</h2><br /> <h3>2817m</h3>");
        data.add("<h2>Average elevation</h2><br /> <h3>1850.9m</h3>");
        data.add("<h2>Total climbing</h2><br /> <h3>28900m</h3>");
        data.add("<h2>Total descent</h2><br /> <h3>28820m</h3>");
        data.add("<h2>End elevation</h2><br /> <h3>1862m</h3>");

        return data;
    }

    private void DisplayStats() {

        track = ProcessedData.GetTrack();

        StaggeredGridView mGridView = (StaggeredGridView) rootView.findViewById(R.id.grid_view);
        StatsAdapter mAdapter = new StatsAdapter(rootView.getContext(), R.id.txt_line1);

        LayoutInflater inflater = (LayoutInflater) rootView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View header = inflater.inflate(R.layout.list_item_header_footer, null);
        View footer = inflater.inflate(R.layout.list_item_header_footer, null);
        TextView txtHeaderTitle = (TextView) header.findViewById(R.id.txt_title);
        TextView txtFooterTitle =  (TextView) footer.findViewById(R.id.txt_title);
        txtHeaderTitle.setText("THE HEADER!");
        txtFooterTitle.setText("THE FOOTER!");

//        mGridView.addHeaderView(header);
//        mGridView.addFooterView(footer);

        List<String> dataSamples = generateSampleData();

        for(String data: dataSamples){
            //mAdapter.add(data);
            mAdapter.add(data);
        }



        mGridView.setAdapter(mAdapter);


//        TableLayout table = (TableLayout)rootView.findViewById(R.id.table_stats);
//
//
//        TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
//        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
//
//        TableLayout tableLayout = new TableLayout(rootView.getContext());
//        tableLayout.setLayoutParams(tableParams);
//
//        TableRow tableRow = new TableRow(rootView.getContext());
//        tableRow.setLayoutParams(tableParams);
//
//
//
//        TextView textView = new TextView(rootView.getContext());
//        textView.setText("Minimum elevation:");
//        textView.setLayoutParams(rowParams);
//
//        TextView textView2 = new TextView(rootView.getContext());
//        textView2.setText("234");
//        textView2.setLayoutParams(rowParams);
//
//        tableRow.addView(textView);
//        tableRow.addView(textView2);
//
//        table.addView(tableRow);
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
}
