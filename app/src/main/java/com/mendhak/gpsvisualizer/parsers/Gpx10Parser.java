package com.mendhak.gpsvisualizer.parsers;


import android.util.Log;

import com.google.common.collect.Lists;
import com.mendhak.gpsvisualizer.common.GpsPoint;
import com.mendhak.gpsvisualizer.common.GpsTrack;

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

public class Gpx10Parser {

    List<GpsPoint> points = Lists.newArrayList();

    public GpsTrack GetParsedTrack(){
        GpsTrack track = new GpsTrack();
        track.setPoints(points);
        Log.d("GPSVisualizer", String.valueOf(points.size()));
        return track;
    }

    public void Parse(String filePath) {
        try {

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            DefaultHandler handler = new DefaultHandler() {

                float lat;
                float lon;

                public void startElement(String uri, String localName,String qName,
                                         Attributes attributes) throws SAXException {

                    if(attributes.getValue("lat") != null){
                       lat = Float.valueOf(attributes.getValue("lat"));
                    }

                    if(attributes.getValue("lon") != null){
                        lon = Float.valueOf(attributes.getValue("lon"));
                    }

                }

                public void endElement(String uri, String localName,
                                       String qName) throws SAXException {

                    if(qName.equalsIgnoreCase("trkpt")){
                        points.add(GpsPoint.from(lat, lon, null));
                    }

                }

                public void characters(char ch[], int start, int length) throws SAXException {

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


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
