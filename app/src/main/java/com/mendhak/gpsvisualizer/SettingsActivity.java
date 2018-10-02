package com.mendhak.gpsvisualizer;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import com.mendhak.gpsvisualizer.common.Utils;


import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {



    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_general);
        setupSimplePreferencesScreen();
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {

        Preference pref_version = (Preference)findPreference("pref_version");
        try {
            pref_version.setTitle("GPS Mapper - Version " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) { }


        Preference pref_gpslogger = (Preference)findPreference("pref_gpslogger");
        pref_gpslogger.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                if(Utils.IsPackageInstalled("com.mendhak.gpslogger",getApplicationContext())){
                    Intent gpsLoggerIntent = getPackageManager().getLaunchIntentForPackage("com.mendhak.gpslogger");
                    startActivity(gpsLoggerIntent);
                }
                else {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=com.mendhak.gpslogger"));
                    startActivity(intent);
                }
                return false;
            }
        });

        Preference pref_issue = (Preference)findPreference("pref_issue");
        pref_issue.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String url = "https://github.com/mendhak/gpsvisualizer/issues";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                return false;
            }
        });



        return;


    }



}
