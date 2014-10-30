package com.mendhak.gpsvisualizer.views;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.etsy.android.grid.StaggeredGridView;
import com.google.android.gms.maps.GoogleMap;
import com.mendhak.gpsvisualizer.R;

import com.mendhak.gpsvisualizer.common.GpsTrack;
import com.mendhak.gpsvisualizer.common.ProcessedData;
import com.mendhak.gpsvisualizer.common.StatsAdapter;

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

    public ArrayList<String> generateSampleData() {
        final ArrayList<String> data = new ArrayList<String>(44);

        data.add("<h1><big>Minimum elevation</big></h1><br /> <h2>4m</h2>");
        data.add("<h2>Maximum elevation</h2><br /> <h3>2817m</h3>");
        data.add("<h2>Average elevation</h2><br /> <h3>1850.9m</h3>");
        data.add("<h2>Total climbing</h2><br /> <h3>28900m</h3>");
        data.add("<h2>Total descent</h2><br /> <h3>28820m</h3>");
        data.add("<h2>Start elevation</h2><br /> <h3>1782.2m</h3>");
        data.add("<h2>End elevation</h2><br /> <h3>1862m</h3>");

        return data;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void DisplayStats() {


        track = ProcessedData.GetTrack();

//        ImageView background = (ImageView)rootView.findViewById(R.id.stats_background);
        LinearLayout layout = (LinearLayout)rootView.findViewById(R.id.linear_layout_stats_fragment);
        if(statType == StatType.DISTANCE){
            //background.setImageResource(R.drawable.wallpaper_distance);
            layout.setBackgroundResource(R.drawable.wallpaper_distance);

        }
        else if (statType == StatType.ELEVATION){
            //background.setImageResource(R.drawable.wallpaper_elevation);
            layout.setBackgroundResource(R.drawable.wallpaper_elevation);
        }
        else if (statType == StatType.SPEED){
            //background.setImageResource(R.drawable.wallpaper_speed);
            layout.setBackgroundResource(R.drawable.wallpaper_speed);
        }
        else {
            //background.setImageResource(R.drawable.wallpaper_time);
            layout.setBackgroundResource(R.drawable.wallpaper_time);
        }


        StaggeredGridView staggeredGridView = (StaggeredGridView) rootView.findViewById(R.id.grid_view);
        StatsAdapter statsAdapter = new StatsAdapter(rootView.getContext(), R.id.txt_line1);

        LayoutInflater inflater = (LayoutInflater) rootView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

//        View header = inflater.inflate(R.layout.list_item_header_footer, null);
//        View footer = inflater.inflate(R.layout.list_item_header_footer, null);
//        TextView txtHeaderTitle = (TextView) header.findViewById(R.id.txt_title);
//        TextView txtFooterTitle =  (TextView) footer.findViewById(R.id.txt_title);
//        txtHeaderTitle.setText("THE HEADER!");
//        txtFooterTitle.setText("THE FOOTER!");
//        mGridView.addHeaderView(header);
//        mGridView.addFooterView(footer);

        List<String> dataSamples = generateSampleData();

        for(String data: dataSamples){
            //mAdapter.add(data);
            statsAdapter.add(data);
        }



        staggeredGridView.setAdapter(statsAdapter);

        //if(statType!=StatType.ELEVATION){ mAdapter.clear(); mGridView.clearAnimation(); mGridView.setAdapter(mAdapter); }


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

    private static class StatType {
        public static final int ELEVATION = 0;
        public static final int SPEED = 1;
        public static final int TIME = 2;
        public static final int DISTANCE = 3;
    }
}
