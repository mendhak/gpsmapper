package com.mendhak.gpsvisualizer.common;

import android.net.Uri;
import com.google.android.gms.drive.DriveId;

public interface IFileSelectedListener{
    void OnFileSelected(Uri uri);
    void OnGoogleDriveFileSelected(DriveId driveId);
}