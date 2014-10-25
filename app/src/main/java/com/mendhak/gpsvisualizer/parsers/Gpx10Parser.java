package com.mendhak.gpsvisualizer.parsers;


import android.util.Log;

import com.google.common.collect.Lists;
import com.mendhak.gpsvisualizer.common.GpsPoint;
import com.mendhak.gpsvisualizer.common.GpsTrack;
import com.mendhak.gpsvisualizer.common.IDataImportListener;
import com.mendhak.gpsvisualizer.common.ISO8601;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Given a GPX file, parses the tracks and holds it. When GetParsedTrack is called, returns a GpsTrack.
 */
public class Gpx10Parser {

    List<GpsPoint> trackPoints = Lists.newArrayList();
    List<GpsPoint> wayPoints = Lists.newArrayList();

    public void Parse(String filePath, IDataImportListener callback) {
        try {

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            DefaultHandler handler = new DefaultHandler() {

                float lat;
                float lon;
                float elevation;
                Calendar calendar;
                double accumulatedDistance = 0;


                StringBuilder sb;
                boolean parseIt = false;

                public void startElement(String uri, String localName,String qName,
                                         Attributes attributes) throws SAXException {

                    sb = new StringBuilder();
                    parseIt=false;

                    if(attributes.getValue("lat") != null){
                       lat = Float.valueOf(attributes.getValue("lat"));
                    }

                    if(attributes.getValue("lon") != null){
                        lon = Float.valueOf(attributes.getValue("lon"));
                    }

                    if(qName.equalsIgnoreCase("ele")
                            || qName.equalsIgnoreCase("time")
                            || qName.equalsIgnoreCase("name") ){
                        parseIt=true;
                    }

                }

                public void endElement(String uri, String localName,
                                       String qName) throws SAXException {

                    if(qName.equalsIgnoreCase("trkpt")){
                        if(trackPoints.size()>0){
                            accumulatedDistance += CalculateDistance(
                                    lat,
                                    lon,
                                    trackPoints.get(trackPoints.size()-1).getLatitude(),
                                    trackPoints.get(trackPoints.size()-1).getLongitude()
                            );
                        }
                        trackPoints.add(GpsPoint.from(lat, lon, elevation, calendar, new Float(accumulatedDistance)));
                    }

                    if(qName.equalsIgnoreCase("name")){
                        wayPoints.add(GpsPoint.wayPoint(lat, lon, sb.toString(), elevation, calendar));
                    }

                    if(qName.equalsIgnoreCase("time")){

                        try {
                            calendar = ISO8601.toCalendar(sb.toString());
                        } catch (ParseException e) {
                            Log.e("GPSVisualizer", "Could not parse " + sb.toString(), e);
                            calendar = null;
                        }
                    }

                    if(qName.equalsIgnoreCase("ele")){
                        elevation = Float.valueOf(sb.toString());
                    }
                }

                public void characters(char ch[], int start, int length) throws SAXException {

                    if(parseIt){
                        sb.append(new String(ch, start, length));
                    }
                }
            };

            File file = new File(filePath);
            InputStream inputStream= new FileInputStream(file);
            Reader reader = new InputStreamReader(inputStream,"UTF-8");

            InputSource is = new InputSource(reader);
            is.setEncoding("UTF-8");

            saxParser.parse(is, handler);
            GpsTrack track = new GpsTrack();
            track.setTrackPoints(trackPoints);
            track.setWayPoints(wayPoints);

            callback.OnDataImported(track);


        } catch (Exception e) {
            Log.e("GPSVisualizer", "Could not parse GPX file", e);
            e.printStackTrace();
        }
    }

    /**
     * Uses the Haversine formula to calculate the distnace between to lat-long coordinates
     *
     * @param latitude1  The first point's latitude
     * @param longitude1 The first point's longitude
     * @param latitude2  The second point's latitude
     * @param longitude2 The second point's longitude
     * @return The distance between the two points in meters
     */
    private double CalculateDistance(double latitude1, double longitude1, double latitude2, double longitude2) {
        /*
            Haversine formula:
            A = sin²(Δlat/2) + cos(lat1).cos(lat2).sin²(Δlong/2)
            C = 2.atan2(√a, √(1−a))
            D = R.c
            R = radius of earth, 6371 km.
            All angles are in radians
            */

        double deltaLatitude = Math.toRadians(Math.abs(latitude1 - latitude2));
        double deltaLongitude = Math.toRadians(Math.abs(longitude1 - longitude2));
        double latitude1Rad = Math.toRadians(latitude1);
        double latitude2Rad = Math.toRadians(latitude2);

        double a = Math.pow(Math.sin(deltaLatitude / 2), 2) +
                (Math.cos(latitude1Rad) * Math.cos(latitude2Rad) * Math.pow(Math.sin(deltaLongitude / 2), 2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return 6371 * c * 1000; //Distance in meters

    }
}
