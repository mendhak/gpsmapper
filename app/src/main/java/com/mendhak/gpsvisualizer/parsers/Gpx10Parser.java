package com.mendhak.gpsvisualizer.parsers;


import android.util.Log;

import com.google.common.collect.Lists;
import com.mendhak.gpsvisualizer.common.*;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Given a GPX file, parses the tracks and holds it. When GetParsedTrack is called, returns a GpsTrack.
 */
public class Gpx10Parser extends BaseParser {

    List<GpsPoint> trackPoints = Lists.newArrayList();
    List<GpsPoint> wayPoints = Lists.newArrayList();

    public GpsTrack GetTrack(InputStream stream){

        GpsTrack track = new GpsTrack();

        try {

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            Reader reader = new InputStreamReader(stream,"UTF-8");

            InputSource is = new InputSource(reader);
            is.setEncoding("UTF-8");

            saxParser.parse(is, handler);

            track.setTrackPoints(trackPoints);
            track.setWayPoints(wayPoints);

        } catch (Exception e) {
            Log.e("GPSMapper", "Could not parse GPX file", e);
            e.printStackTrace();
        }

        return track;
    }




    DefaultHandler handler = new DefaultHandler() {

        float lat;
        float lon;
        Float elevation = null;
        Float speed = null;
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
                    || qName.equalsIgnoreCase("speed")
                    || qName.equalsIgnoreCase("time")
                    || qName.equalsIgnoreCase("name") ){
                parseIt=true;
            }

        }

        public void endElement(String uri, String localName,
                               String qName) throws SAXException {

            if(qName.equalsIgnoreCase("trkpt") ||qName.equalsIgnoreCase("wpt")){
                if(!trackPoints.isEmpty()){
                    accumulatedDistance += Utils.CalculateDistance(
                            lat,
                            lon,
                            trackPoints.get(trackPoints.size() - 1).getLatitude(),
                            trackPoints.get(trackPoints.size() - 1).getLongitude()
                    );

                    speed = Utils.CalculateSpeed(
                            lat,
                            lon,
                            calendar.getTimeInMillis(),
                            trackPoints.get(trackPoints.size() - 1).getLatitude(),
                            trackPoints.get(trackPoints.size() - 1).getLongitude(),
                            trackPoints.get(trackPoints.size() -1).getCalendar().getTimeInMillis()
                    );
                }

                if(qName.equalsIgnoreCase("trkpt")) {
                    trackPoints.add(GpsPoint.from(lat, lon, elevation, calendar, new Float(accumulatedDistance), speed));
                }
                if(qName.equalsIgnoreCase("wpt")) {
                    wayPoints.add(GpsPoint.from(lat, lon, elevation, calendar, new Float(accumulatedDistance), speed));
                }

            }

            if(qName.equalsIgnoreCase("name")){
                if(lat != 0f && lon != 0f){
                    wayPoints.add(GpsPoint.wayPoint(lat, lon, sb.toString(), elevation, calendar));
                }

            }

            if(qName.equalsIgnoreCase("time")){

                try {
                    calendar = GregorianCalendar.getInstance();
                    calendar.setTime(DateUtils.parseIso8601DateTime(sb.toString()));
                } catch (Exception e) {
                    Log.e("GPSMapper", "Could not parse " + sb.toString(), e);
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
}
