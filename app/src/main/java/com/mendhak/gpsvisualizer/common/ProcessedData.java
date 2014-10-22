package com.mendhak.gpsvisualizer.common;

/**
 * A singleton class that holds the currently processed track. Accessible from all activities and fragments.
 */
public final class ProcessedData {
    public static GpsTrack track;


    public static void SetTrack(GpsTrack processedTracks)
    {
        if (track == null)
        {
            track = new GpsTrack();
        }

        track.setTrackPoints(processedTracks.getTrackPoints());
        track.setWayPoints(processedTracks.getWayPoints());
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

}
