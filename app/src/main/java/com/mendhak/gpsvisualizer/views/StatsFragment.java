package com.mendhak.gpsvisualizer.views;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.mendhak.gpsvisualizer.R;

import lecho.lib.hellocharts.view.LineChartView;


public class StatsFragment extends Fragment {

    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_stats, container, false);

        DisplayStats();
        return rootView;
    }

    private void DisplayStats() {
        TableLayout table = (TableLayout)rootView.findViewById(R.id.table_stats);


        TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

        TableLayout tableLayout = new TableLayout(rootView.getContext());
        tableLayout.setLayoutParams(tableParams);

        TableRow tableRow = new TableRow(rootView.getContext());
        tableRow.setLayoutParams(tableParams);

        TextView textView = new TextView(rootView.getContext());
        textView.setText("TEST");
        textView.setLayoutParams(rowParams);

        tableRow.addView(textView);

        table.addView(tableRow);
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
