package com.mendhak.gpsvisualizer.parsers;

import com.mendhak.gpsvisualizer.common.GpsTrack;
import com.mendhak.gpsvisualizer.common.IDataImportListener;

import java.io.InputStream;


public abstract class BaseParser {

    public abstract GpsTrack GetTrack(InputStream stream);


    public void Parse(InputStream stream, IDataImportListener callback){
        GpsTrack processedTrack = GetTrack(stream);
        callback.OnDataImported(processedTrack);
    }



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
