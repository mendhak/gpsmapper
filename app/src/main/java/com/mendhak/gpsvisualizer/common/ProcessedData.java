package com.mendhak.gpsvisualizer.common;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * A singleton class that holds the currently processed track. Accessible from all activities and fragments.
 */
public final class ProcessedData {
    public static GpsTrack track;

    private static boolean qualityTrack;

    public static void SetTrack(GpsTrack processedTracks)
    {
        if (track == null)
        {
            track = new GpsTrack();
        }

        track.setTrackPoints(processedTracks.getTrackPoints());
        track.setWayPoints(processedTracks.getWayPoints());

        setQualityTrack(IsTrackOfGoodQuality(track));
    }



    public static GpsTrack GetTrack()
    {
        if (track == null)
        {
            track = new GpsTrack();
        }

        // Return the instance
        return track;
    }

    public static boolean isQualityTrack() {
        return qualityTrack;
    }

    public static void setQualityTrack(boolean qualityTrack) {
        ProcessedData.qualityTrack = qualityTrack;
    }

    private static boolean IsTrackOfGoodQuality(GpsTrack track) {
        List<GpsPoint> pointsWithElevations = Lists.newArrayList(Iterables.filter(track.getTrackPoints(), new Predicate<GpsPoint>() {
            @Override
            public boolean apply(GpsPoint input) {
                return input.getElevation().isPresent();
            }
        }));

        if(!pointsWithElevations.isEmpty() && pointsWithElevations.size() > (track.getTrackPoints().size()/2) ) { return true; }
        return false;
    }
}
