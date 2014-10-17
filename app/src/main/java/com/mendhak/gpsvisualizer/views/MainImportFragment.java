package com.mendhak.gpsvisualizer.views;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.common.collect.Lists;
import com.mendhak.gpsvisualizer.R;
import com.mendhak.gpsvisualizer.common.GpsPoint;
import com.mendhak.gpsvisualizer.common.GpsTrack;

/**
 * A placeholder fragment containing a simple view.
 */
public  class MainImportFragment extends Fragment implements View.OnClickListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    GpsTrack flatTrack;
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MainImportFragment newInstance(int sectionNumber) {
        MainImportFragment fragment = new MainImportFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public MainImportFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        Button btnImport = (Button)rootView.findViewById(R.id.btnImportData);
        btnImport.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btnImportData:
                ProcessUserGpsFile();
                break;
        }
    }

    private void ProcessUserGpsFile() {
        flatTrack = new GpsTrack();

        flatTrack.addPoints(Lists.newArrayList(
                GpsPoint.from(45.4431641f, -121.7295456f, null),
                GpsPoint.from(45.4428615f, -121.7290800f, null),
                GpsPoint.from(45.4425697f, -121.7279085f, null),
                GpsPoint.from(45.4424274f, -121.7267360f, null),
                GpsPoint.from(45.4422017f, -121.7260429f, null),
                GpsPoint.from(45.4416576f, -121.7252347f, null),
                GpsPoint.from(45.4406144f, -121.7241181f, null),
                GpsPoint.from(45.4398193f, -121.7224890f, null),
                GpsPoint.from(45.4387649f, -121.7226112f, null),
                GpsPoint.from(45.4383933f, -121.7224328f, null),
                GpsPoint.from(45.4377850f, -121.7224159f, null),
                GpsPoint.from(45.4372204f, -121.7226603f, null),
                GpsPoint.from(45.4347837f, -121.7226007f, null),
                GpsPoint.from(45.4332000f, -121.7216480f, null),
                GpsPoint.from(45.4334576f, -121.7223143f, null),
                GpsPoint.from(45.4321730f, -121.7222102f, null),
                GpsPoint.from(45.4316609f, -121.7219974f, null),
                GpsPoint.from(45.4303068f, -121.7220616f, null),
                GpsPoint.from(45.4270753f, -121.7209685f, null),
                GpsPoint.from(45.4267610f, -121.7211872f, null),
                GpsPoint.from(45.4260133f, -121.7212623f, null),
                GpsPoint.from(45.4257683f, -121.7214738f, null),
                GpsPoint.from(45.4257400f, -121.7217762f, null),
                GpsPoint.from(45.4259485f, -121.7226009f, null),
                GpsPoint.from(45.4249972f, -121.7223672f, null),
                GpsPoint.from(45.4246035f, -121.7219816f, null),
                GpsPoint.from(45.4238682f, -121.7219830f, null),
                GpsPoint.from(45.4226721f, -121.7216494f, null),
                GpsPoint.from(45.4224120f, -121.7217998f, null),
                GpsPoint.from(45.4211497f, -121.7218767f, null),
                GpsPoint.from(45.4193319f, -121.7208650f, null),
                GpsPoint.from(45.4186435f, -121.7202956f, null),
                GpsPoint.from(45.4185934f, -121.7200745f, null),
                GpsPoint.from(45.4178963f, -121.7196035f, null),
                GpsPoint.from(45.4171101f, -121.7198115f, null),
                GpsPoint.from(45.4166827f, -121.7193250f, null),
                GpsPoint.from(45.4161855f, -121.7190778f, null),
                GpsPoint.from(45.4159291f, -121.7193146f, null),
                GpsPoint.from(45.4153644f, -121.7193939f, null),
                GpsPoint.from(45.4151268f, -121.7191578f, null),
                GpsPoint.from(45.4148071f, -121.7191043f, null),
                GpsPoint.from(45.4146310f, -121.7187962f, null),
                GpsPoint.from(45.4142524f, -121.7187236f, null),
                GpsPoint.from(45.4142844f, -121.7185595f, null),
                GpsPoint.from(45.4133520f, -121.7180429f, null),
                GpsPoint.from(45.4131406f, -121.7181383f, null),
                GpsPoint.from(45.4130356f, -121.7179036f, null),
                GpsPoint.from(45.4118436f, -121.7168789f, null),
                GpsPoint.from(45.4109205f, -121.7156569f, null),
                GpsPoint.from(45.4104523f, -121.7145250f, null),
                GpsPoint.from(45.4104930f, -121.7143814f, null),
                GpsPoint.from(45.4102075f, -121.7140608f, null),
                GpsPoint.from(45.4099806f, -121.7134527f, null)
        ));

    }
}
