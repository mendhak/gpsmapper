package com.mendhak.gpsvisualizer.common;

import android.net.Uri;
import com.google.android.gms.drive.DriveId;

import java.util.ArrayList;

public interface IFileSelectedListener{
    void OnFileSelected(ArrayList<Uri> uri);
    void OnGoogleDriveFileSelected(DriveId driveId);
}