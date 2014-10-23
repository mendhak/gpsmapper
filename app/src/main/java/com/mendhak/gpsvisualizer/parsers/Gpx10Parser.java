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
                        trackPoints.add(GpsPoint.from(lat, lon, elevation, calendar));
                    }

                    if(qName.equalsIgnoreCase("name")){
                        wayPoints.add(GpsPoint.wayPoint(lat, lon, sb.toString(), elevation, calendar));
                    }

                    if(qName.equalsIgnoreCase("time")){

                        try {
                            Log.d("GPSVisualizer","Parsing time: " + sb.toString());
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
}
