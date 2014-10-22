package com.mendhak.gpsvisualizer.parsers;


import android.util.Log;

import com.google.common.collect.Lists;
import com.mendhak.gpsvisualizer.common.GpsPoint;
import com.mendhak.gpsvisualizer.common.GpsTrack;
import com.mendhak.gpsvisualizer.common.IDataImportListener;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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
                boolean wpt;
                boolean wptName;
                boolean eleTag;
                float elevation;
                String wayPointName;

                public void startElement(String uri, String localName,String qName,
                                         Attributes attributes) throws SAXException {

                    if(attributes.getValue("lat") != null){
                       lat = Float.valueOf(attributes.getValue("lat"));
                    }

                    if(attributes.getValue("lon") != null){
                        lon = Float.valueOf(attributes.getValue("lon"));
                    }

                    if(qName.equalsIgnoreCase("ele")){
                        eleTag = true;
                    }

                    if(qName.equalsIgnoreCase("wpt")){
                        wpt = true;
                    }

                    if(qName.equalsIgnoreCase("name") && wpt){
                        wptName = true;
                    }

                }

                public void endElement(String uri, String localName,
                                       String qName) throws SAXException {

                    if(qName.equalsIgnoreCase("trkpt")){
                        trackPoints.add(GpsPoint.from(lat, lon, elevation));
                    }

                    if(qName.equalsIgnoreCase("wpt")){
                        wayPoints.add(GpsPoint.wayPoint(lat, lon, wayPointName));
                        wpt = false;
                        wptName = false;
                    }

                }

                public void characters(char ch[], int start, int length) throws SAXException {

                    if(wptName){
                        Log.i("GPSVisualizer", "Waypoint: " + new String(ch, start, length));
                        wayPointName = new String(ch, start, length);
                        wptName = false;
                    }

                    if(eleTag){
                        elevation = Float.valueOf(new String(ch, start, length));
                        eleTag = false;
                    }
//                    if (bfname) {
//                        System.out.println("First Name : " + new String(ch, start, length));
//                        bfname = false;
//                    }
//
//                    if (blname) {
//                        System.out.println("Last Name : " + new String(ch, start, length));
//                        blname = false;
//                    }
//
//                    if (bnname) {
//                        System.out.println("Nick Name : " + new String(ch, start, length));
//                        bnname = false;
//                    }
//
//                    if (bsalary) {
//                        System.out.println("Salary : " + new String(ch, start, length));
//                        bsalary = false;
//                    }
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
            e.printStackTrace();
        }
    }
}
