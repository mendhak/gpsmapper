package com.mendhak.gpsvisualizer.views;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mendhak.gpsvisualizer.R;
import com.mendhak.gpsvisualizer.common.GpsPoint;
import com.mendhak.gpsvisualizer.common.GpsTrack;
import com.mendhak.gpsvisualizer.common.ProcessedData;

import java.util.List;
import java.util.Set;

public  class MapFragment extends BaseFragment {

    public static MapFragment newInstance(int sectionNumber) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putInt("section_number", sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public MapFragment() {
    }

    MapView mapView;
    private GoogleMap googleMap;
    private View rootView;
    private GpsTrack track;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // inflat and return the layout
        rootView = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = (MapView) rootView.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);


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
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private static boolean m_iAmVisible;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        m_iAmVisible = isVisibleToUser;

        if (m_iAmVisible) {
            Log.d("GPSVisualizer", "this fragment is now visible");

            RenderMap();
        } else {
            Log.d("GPSVisualizer", "this fragment is now invisible");
        }
    }

//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        // TODO Auto-generated method stub
//        if (googleMap != null)
//            RenderMap();
//
//        if (googleMap == null) {
//            SetupMap();
//        }
//    }

//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        if (googleMap != null) {
//            MainActivity.fragmentManager.beginTransaction()
//                    .remove(MainActivity.fragmentManager.findFragmentById(R.id.location_map)).commit();
//            mMap = null;
//        }
//    }

    private void SetupMap(){

        if(googleMap != null){
            return;
        }
        googleMap = mapView.getMap();
        //RenderMap();
    }

    private void RenderMap(){

        track = ProcessedData.GetTrack();


        if(track.getPoints().size() > 0){
            Log.i("GPSVisualizer", "Track points: " + String.valueOf(track.getPoints().size()));

            List<LatLng> gmapLatLongs = Lists.transform(track.getPoints(), new Function<GpsPoint, LatLng>() {
                @Override
                public LatLng apply(GpsPoint input) {
                    return new LatLng(input.getLatitude(), input.getLongitude());
                }
            });

            googleMap.addPolyline(new PolylineOptions().geodesic(true).add(Iterables.toArray(gmapLatLongs, LatLng.class)));


            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng ll : gmapLatLongs) {
                builder.include(ll);
            }
            LatLngBounds bounds = builder.build();

            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 35),2000, null);

            //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Iterables.getFirst(gmapLatLongs, null), 8), 2000, null);
        }



//            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                    new LatLng(-18.142, 178.431), 2),2000,null);
//
//            // Polylines are useful for marking paths and routes on the map.
//            googleMap.addPolyline(new PolylineOptions().geodesic(true)
//                    .add(new LatLng(-33.866, 151.195))  // Sydney
//                    .add(new LatLng(-18.142, 178.431))  // Fiji
//                    .add(new LatLng(21.291, -157.821))  // Hawaii
//                    .add(new LatLng(37.423, -122.091))  // Mountain View
//            );


//        final LatLng SYDNEY = new LatLng(-33.88,151.21);
//        final LatLng MOUNTAIN_VIEW = new LatLng(37.4, -122.1);
//
//        // Move the camera instantly to Sydney with a zoom of 15.
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SYDNEY, 15));
//
//// Zoom in, animating the camera.
//        googleMap.animateCamera(CameraUpdateFactory.zoomIn());
//
//// Zoom out to zoom level 10, animating with a duration of 2 seconds.
//        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
//
//// Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
//        CameraPosition cameraPosition = new CameraPosition.Builder()
//                .target(MOUNTAIN_VIEW)      // Sets the center of the map to Mountain View
//                .zoom(17)                   // Sets the zoom
//                .bearing(90)                // Sets the orientation of the camera to east
//                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
//                .build();                   // Creates a CameraPosition from the builder
//        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

//
//
//            // latitude and longitude
//            double latitude = 51;
//            double longitude = -2;
//
//            // create marker
//            MarkerOptions marker = new MarkerOptions().position(
//                    new LatLng(latitude, longitude)).title("Hello Maps");
//
//            // Changing marker icon
//            marker.icon(BitmapDescriptorFactory
//                    .defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
//
//            // adding marker
//            googleMap.addMarker(marker);
//            CameraPosition cameraPosition = new CameraPosition.Builder()
//                    .target(new LatLng(51, -2)).zoom(12).build();
//            googleMap.animateCamera(CameraUpdateFactory
//                    .newCameraPosition(cameraPosition));

        //googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        // Perform any camera updates here
    }
}