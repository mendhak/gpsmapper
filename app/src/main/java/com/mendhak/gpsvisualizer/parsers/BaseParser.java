package com.mendhak.gpsvisualizer.parsers;

import com.mendhak.gpsvisualizer.common.GpsTrack;

import java.io.InputStream;


public abstract class BaseParser {

    public abstract GpsTrack GetTrack(InputStream stream);


    public static BaseParser GetParserFromFilename(String fileName){
        if(fileName.endsWith(".gpx")){
            return new Gpx10Parser();
        }

        if(fileName.endsWith(".nmea")){
            return new NmeaParser();
        }

        return new Gpx10Parser();

    }

}
