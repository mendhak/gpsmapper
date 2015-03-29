package com.mendhak.gpsvisualizer.common;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

public class Prefs {

    public static boolean ShouldAlwaysOpenGPSLoggerFolder(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_always_open_gpslogger_folder", true);
    }

    public static String GetLastOpenedFile(Context context){
        Log.d("GPSVisualizer",PreferenceManager.getDefaultSharedPreferences(context).getString("last_opened_folder",""));
        return PreferenceManager.getDefaultSharedPreferences(context).getString("last_opened_file","");
    }

    public static void SetLastOpenedFile(Uri uri, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("last_opened_file", uri.toString());
        editor.apply();
    }
}
