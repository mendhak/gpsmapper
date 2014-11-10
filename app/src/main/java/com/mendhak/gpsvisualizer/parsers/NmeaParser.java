package com.mendhak.gpsvisualizer.parsers;

import android.util.Log;
import com.google.common.collect.Lists;
import com.mendhak.gpsvisualizer.common.GpsPoint;
import com.mendhak.gpsvisualizer.common.GpsTrack;
import com.mendhak.gpsvisualizer.common.IDataImportListener;
import com.mendhak.gpsvisualizer.common.Utils;
import oceanebelle.parser.engine.ParserHandler;
import oceanebelle.parser.engine.nmea.NmeaEvent;
import oceanebelle.parser.engine.nmea.NmeaParserEngineBuilder;
import oceanebelle.parser.engine.nmea.NmeaParserEngineFactory;
import oceanebelle.parser.engine.nmea.helper.NmeaHandlers;
import oceanebelle.parser.engine.nmea.model.NmeaDataAdapter;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;


public class NmeaParser extends BaseParser {

    List<GpsPoint> trackPoints = Lists.newArrayList();
    double accumulatedDistance = 0;

    @Override
    public GpsTrack GetTrack(InputStream stream) {
        GpsTrack track = new GpsTrack();

        NmeaParserEngineBuilder builder = NmeaParserEngineFactory.newBuilder();
        builder.addEventHandler(NmeaHandlers.forGGA(new NmeaHandlers.HandlerAdapter() {
            @Override
            public void handle(NmeaDataAdapter nmeaDataAdapter) {

                if(!trackPoints.isEmpty()) {
                    accumulatedDistance += Utils.CalculateDistance(
                            nmeaDataAdapter.getCoordinates().getLatitude(),
                            nmeaDataAdapter.getCoordinates().getLongitude(),
                            trackPoints.get(trackPoints.size() - 1).getLatitude(),
                            trackPoints.get(trackPoints.size() - 1).getLongitude()
                    );
                }

                trackPoints.add(GpsPoint.from(
                        nmeaDataAdapter.getCoordinates().getLatitude(),
                        nmeaDataAdapter.getCoordinates().getLongitude(),
                        nmeaDataAdapter.getAltitude(),
                        nmeaDataAdapter.getDateTimeData().getCalendar(),
                        new Float(accumulatedDistance),
                        nmeaDataAdapter.getSpeed()));
//                Log.d("GPSVisualizer", "GGA" +  nmeaDataAdapter.getDateTimeData().getCalendar().getTime() + "," + nmeaDataAdapter.getCoordinates().getLatitude() + "," + nmeaDataAdapter.getCoordinates().getLongitude());
            }
        }));

//        builder.addEventHandler(NmeaHandlers.forRMC(new NmeaHandlers.HandlerAdapter() {
//            @Override
//            public void handle(NmeaDataAdapter nmeaDataAdapter) {
//                Log.d("GPSVisualizer", "RMC:" + nmeaDataAdapter.getDateTimeData().getCalendar().getTime() + "," + nmeaDataAdapter.getCoordinates().getLatitude() + "," + nmeaDataAdapter.getCoordinates().getLongitude());
//            }
//        }));

        builder.build().parse(stream);
        track.setTrackPoints(trackPoints);
        return track;
    }
}
