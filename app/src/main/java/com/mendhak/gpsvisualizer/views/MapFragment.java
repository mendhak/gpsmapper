package com.mendhak.gpsvisualizer.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mendhak.gpsvisualizer.R;
import com.mendhak.gpsvisualizer.common.GpsPoint;
import com.mendhak.gpsvisualizer.common.GpsTrack;
import com.mendhak.gpsvisualizer.common.ProcessedData;

import java.util.List;

public  class MapFragment extends BaseFragment implements OnMapReadyCallback {

    MapView mapView;
    private GoogleMap googleMap;
    private View rootView;
    private GpsTrack track;
    boolean showCircles;
    private List<LatLng> gmapLatLongs;
    private static int mapType = GoogleMap.MAP_TYPE_NORMAL;

    public static MapFragment newInstance(int sectionNumber) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putInt("section_number", sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public MapFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // inflate and return the layout
        rootView = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = (MapView) rootView.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        //mapView.requestTransparentRegion(mapView);

        mapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SetupMap();
        return rootView;
    }

    @Override
    public void onResume() {

        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        showCircles = prefs.getBoolean("pref_map_showcircles", false);

        mapView.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mapView != null){
            mapView.onDestroy();
        }

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.maps, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.maptype_selection) {

            CharSequence mapTypeNames[] = new CharSequence[] {
                    getString(R.string.mapType_normal),
                    getString(R.string.mapType_satellite),
                    getString(R.string.mapType_hybrid),
                    getString(R.string.mapType_terrain)};


            AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
            builder.setTitle("Pick a map type");
            builder.setItems(mapTypeNames, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch(which){
                        case 0:
                            mapType = GoogleMap.MAP_TYPE_NORMAL;
                            break;
                        case 1:
                            mapType = GoogleMap.MAP_TYPE_SATELLITE;
                            break;
                        case 2:
                            mapType = GoogleMap.MAP_TYPE_HYBRID;
                            break;
                        case 3:
                            mapType = GoogleMap.MAP_TYPE_TERRAIN;
                            break;
                        default:
                            mapType = GoogleMap.MAP_TYPE_NORMAL;
                            break;
                    }
                    RenderMap();
                }
            });
            builder.show();


            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        boolean visibleToUser = isVisibleToUser;

        if (visibleToUser) {
            Log.d("GPSVisualizer", "Map Fragment is now visible");

            SetupMap();
        } else {
            Log.d("GPSVisualizer", "Map Fragment is now invisible");
        }
    }

    private void SetupMap(){
        mapView.getMapAsync(this);
    }

    private void RenderMap(){
        googleMap.clear();
        googleMap.setMapType(mapType);

        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                CameraPosition cameraPosition = googleMap.getCameraPosition();

                Log.d("GPSVisualizer", String.valueOf( cameraPosition.zoom));
                Log.d("GPSVisualizer", String.valueOf(showCircles));

                if(!showCircles){
                    return;
                }

                if(cameraPosition.zoom <= 13f){
                    return;
                }

                //Can't clear only circles, so we have to clear everything and redraw.
                googleMap.clear();
                DrawLineAndMarkers();

                for(GpsPoint trk : track.getTrackPoints()){

                    if(googleMap.getProjection().getVisibleRegion().latLngBounds
                            .contains(new LatLng(trk.getLatitude(), trk.getLongitude()))){

                        CircleOptions co = new CircleOptions()
                                .center(new LatLng(trk.getLatitude(), trk.getLongitude()))
                                .radius(30 - cameraPosition.zoom)
                                .strokeWidth(0)
                                .fillColor(Color.BLUE);

                        googleMap.addCircle(co);

                    }
                }
            }
        });

        track = ProcessedData.GetTrack();

        if(track.getTrackPoints().size() > 0){
            gmapLatLongs = Lists.transform(track.getTrackPoints(), new Function<GpsPoint, LatLng>() {
                @Override
                public LatLng apply(GpsPoint input) {
                    return new LatLng(input.getLatitude(), input.getLongitude());
                }
            });


            DrawLineAndMarkers();

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng ll : gmapLatLongs) {
                builder.include(ll);
            }
            LatLngBounds bounds = builder.build();

            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 35),2000, null);
            //Focus on a specific point using:
            //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Iterables.getFirst(gmapLatLongs, null), 8), 2000, null);
            //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SYDNEY, 15));
            //googleMap.animateCamera(CameraUpdateFactory.zoomIn());


        }
    }

    private void DrawLineAndMarkers() {

        googleMap.addPolyline(new PolylineOptions().geodesic(true).add(Iterables.toArray(gmapLatLongs, LatLng.class)));

        for(GpsPoint wpt : track.getWayPoints()){
            MarkerOptions marker = new MarkerOptions().position(
                    new LatLng(wpt.getLatitude(), wpt.getLongitude())).title(wpt.getDescription());
            // Changing marker icon
            marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
            // adding marker
            googleMap.addMarker(marker);
        }
    }

//    @Override
//    public void onMapLoaded() {
//        RenderMap();
//    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        RenderMap();
    }
}