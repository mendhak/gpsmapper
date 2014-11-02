package com.mendhak.gpsvisualizer.common;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;
import com.etsy.android.grid.util.DynamicHeightTextView;
import com.mendhak.gpsvisualizer.R;

import java.util.ArrayList;
import java.util.Random;

public class StatsAdapter extends ArrayAdapter<String> {

    private static final String TAG = "StatsAdapter";

    static class ViewHolder {
        DynamicHeightTextView txtLineOne;
        Button btnGo;
    }

    private final LayoutInflater layoutInflater;
    private final Random random;
    private final ArrayList<Integer> backgroundColors;

    private static final SparseArray<Double> sPositionHeightRatios = new SparseArray<Double>();

    public StatsAdapter(final Context context, final int textViewResourceId) {
        super(context, textViewResourceId);
        layoutInflater = LayoutInflater.from(context);
        random = new Random();
        backgroundColors = new ArrayList<Integer>();
        backgroundColors.add(R.color.orange);
        backgroundColors.add(R.color.green);
        backgroundColors.add(R.color.blue);
        backgroundColors.add(R.color.yellow);
        backgroundColors.add(R.color.grey);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        ViewHolder vh;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.stats_list_item, parent, false);
            vh = new ViewHolder();
            vh.txtLineOne = (DynamicHeightTextView) convertView.findViewById(R.id.txt_line1);
            vh.btnGo = (Button) convertView.findViewById(R.id.btn_go);

            convertView.setTag(vh);
        }
        else {
            vh = (ViewHolder) convertView.getTag();
        }

        double positionHeight = getPositionRatio(position);
        int backgroundIndex = position >= backgroundColors.size() ?
                position % backgroundColors.size() : position;

        convertView.setBackgroundResource(backgroundColors.get(backgroundIndex));

        //Log.d(TAG, "getView position:" + position + " h:" + positionHeight);

        vh.txtLineOne.setHeightRatio(positionHeight);
        //vh.txtLineOne.setText(getItem(position) + position);
        vh.txtLineOne.setText( Html.fromHtml(getItem(position)) );

        vh.btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Toast.makeText(getContext(), "Button Clicked Position " +
                        position, Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }

    private double getPositionRatio(final int position) {
        double ratio = sPositionHeightRatios.get(position, 0.0);
        // if not yet done generate and stash the columns height
        // in our real world scenario this will be determined by
        // some match based on the known height and width of the image
        // and maybe a helpful way to get the column height!
        if (ratio == 0) {
            ratio = getRandomHeightRatio();
            sPositionHeightRatios.append(position, ratio);
//            Log.d(TAG, "getPositionRatio:" + position + " ratio:" + ratio);
        }
        return ratio;
    }

    private double getRandomHeightRatio() {
        return (random.nextDouble() / 2) + 1; // height will be 1.0 - 1.5 the width
    }
}