package com.mendhak.gpsvisualizer.common;


import android.net.Uri;

public interface IDataImportListener{
    /**
     * Callback from MainImportFragment. After a file is parsed, this should replace the currently held track.
     * @param track
     */
    public void OnDataImported(GpsTrack track);
    public Uri GetPendingExternalFile();
}
