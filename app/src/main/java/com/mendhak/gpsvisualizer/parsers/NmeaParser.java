package com.mendhak.gpsvisualizer.parsers;

import android.util.Log;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mendhak.gpsvisualizer.common.GpsPoint;
import com.mendhak.gpsvisualizer.common.GpsTrack;
import com.mendhak.gpsvisualizer.common.IDataImportListener;
import com.mendhak.gpsvisualizer.common.Utils;
import oceanebelle.parser.engine.ErrorHandler;
import oceanebelle.parser.engine.ParseException;
import oceanebelle.parser.engine.ParserHandler;
import oceanebelle.parser.engine.nmea.NmeaEvent;
import oceanebelle.parser.engine.nmea.NmeaParserEngineBuilder;
import oceanebelle.parser.engine.nmea.NmeaParserEngineFactory;
import oceanebelle.parser.engine.nmea.helper.NmeaHandlers;
import oceanebelle.parser.engine.nmea.model.Coordinates;
import oceanebelle.parser.engine.nmea.model.DateTimeData;
import oceanebelle.parser.engine.nmea.model.NmeaDataAdapter;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;


public class NmeaParser extends BaseParser {


    double accumulatedDistance = 0;
    GpsTrack track = new GpsTrack();
    List<GpsPoint> trackPoints = Lists.newArrayList();
    List<GpsPoint> ggaPoints = Lists.newArrayList();
    List<GpsPoint> rmcPoints = Lists.newArrayList();

    @Override
    public GpsTrack GetTrack(InputStream stream) {

        NmeaParserEngineBuilder builder = NmeaParserEngineFactory.newBuilder();
        builder.addEventHandler(NmeaHandlers.forGGA(new NmeaHandlers.HandlerAdapter() {
            @Override
            public void handle(NmeaDataAdapter nmeaDataAdapter) {


                if(nmeaDataAdapter.isChecksumValid()){

                    if(!ggaPoints.isEmpty()){
                        accumulatedDistance += Utils.CalculateDistance(nmeaDataAdapter.getCoordinates().getLatitude(),nmeaDataAdapter.getCoordinates().getLongitude(), ggaPoints.get(ggaPoints.size()-1).getLatitude(), ggaPoints.get(ggaPoints.size()-1).getLongitude());
                    }

                    ggaPoints.add(GpsPoint.from(
                            nmeaDataAdapter.getCoordinates().getLatitude(),
                            nmeaDataAdapter.getCoordinates().getLongitude(),
                            nmeaDataAdapter.getAltitude(),
                            nmeaDataAdapter.getDateTimeData().getCalendar(),
                            new Float(accumulatedDistance),
                            nmeaDataAdapter.getSpeed()));
                }

            }
        }));

        builder.addEventHandler(NmeaHandlers.forRMC(new NmeaHandlers.HandlerAdapter() {
            @Override
            public void handle(NmeaDataAdapter nmeaDataAdapter) {

                if(nmeaDataAdapter.isChecksumValid()){
                    rmcPoints.add(GpsPoint.from(
                            nmeaDataAdapter.getCoordinates().getLatitude(),
                            nmeaDataAdapter.getCoordinates().getLongitude(),
                            nmeaDataAdapter.getAltitude(),
                            nmeaDataAdapter.getDateTimeData().getCalendar(),
                            new Float(accumulatedDistance),
                            nmeaDataAdapter.getSpeed()));
                }
            }
        }));

        builder.build().parse(stream);


        for(final GpsPoint point : ggaPoints){

            try {
                GpsPoint rmcPoint = Iterables.find(rmcPoints, new Predicate<GpsPoint>() {
                    @Override
                    public boolean apply(GpsPoint input) {

                        if(input.getLatitude() == point.getLatitude() &&
                                input.getLongitude() == point.getLongitude() &&
                                input.getCalendar().get(Calendar.HOUR) == point.getCalendar().get(Calendar.HOUR) &&
                                input.getCalendar().get(Calendar.MINUTE) == point.getCalendar().get(Calendar.MINUTE) &&
                                input.getCalendar().get(Calendar.SECOND) == point.getCalendar().get(Calendar.SECOND)) {
                            return true;
                        }

                        return false;
                    }
                });

                //RMC gives date, speed while GGA only has time
                point.setSpeed(rmcPoint.getSpeed());
                point.setCalendar(rmcPoint.getCalendar());
                trackPoints.add(point);
            }
            catch (NoSuchElementException nsee){

            }

        }

        track.setTrackPoints(trackPoints);
        return track;
    }


}
