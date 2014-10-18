package com.mendhak.gpsvisualizer.common;


public final  class ProcessedData {
    public static GpsTrack track;


    public static void SetTrack(GpsTrack processedTracks)
    {
        if (track == null)
        {
            track = new GpsTrack();
        }

        track.setPoints(processedTracks.getPoints());
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
