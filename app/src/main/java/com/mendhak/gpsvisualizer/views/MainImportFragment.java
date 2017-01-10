package com.mendhak.gpsvisualizer.views;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.*;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.SearchableField;
import com.google.common.base.Charsets;
import com.mendhak.gpsvisualizer.MainActivity;
import com.mendhak.gpsvisualizer.R;
import com.mendhak.gpsvisualizer.common.*;
import com.mendhak.gpsvisualizer.parsers.BaseParser;
import com.mendhak.gpsvisualizer.parsers.Gpx10Parser;
import com.nononsenseapps.filepicker.FilePickerActivity;


import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;


public class MainImportFragment extends Fragment implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, IFileSelectedListener {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final Integer ACTION_FILE_PICKER = 41792;
    int GDRIVE_RESOLVE_CONNECTION_REQUEST_CODE = 99;
    public static int GDRIVE_REQUEST_CODE_OPENER = 39;
    private View rootView;
    private IDataImportListener dataImportListener;
    private ArrayList<Uri> selectedLocalFiles;
    private DriveId selectedGoogleDriveFile;
    private GoogleApiClient googleApiClient;

    ProgressDialog parserProgress;
    ProgressDialog downloadProgress;

    public static MainImportFragment newInstance() {
        MainImportFragment fragment = new MainImportFragment();
        return fragment;
    }


    public MainImportFragment() {
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_import, container, false);

        Button btnImport = (Button) rootView.findViewById(R.id.btnImportData);
        Button btnGoogle = (Button) rootView.findViewById(R.id.btnGoogleDrive);
        Button btnReload = (Button) rootView.findViewById(R.id.btn_reload);
        btnImport.setOnClickListener(this);
        btnGoogle.setOnClickListener(this);
        btnReload.setOnClickListener(this);

        dataImportListener = (IDataImportListener) getActivity();

        btnImport.setCompoundDrawablesWithIntrinsicBounds(R.drawable.file, 0, 0, 0);

        parserProgress = new ProgressDialog(getActivity());

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Uri pendingFile = dataImportListener.GetPendingExternalFile();
        if (pendingFile != null) {
            OnFileSelected(new ArrayList<Uri>(Arrays.asList(pendingFile)));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnImportData:
                openLocalFolder();
                break;
            case R.id.btnGoogleDrive:
                openGoogleFolder();
                break;
            case R.id.btn_reload:
                reloadFile();
                break;
        }
    }

    public void reloadFile() {

        if (selectedGoogleDriveFile != null) {
            OnGoogleDriveFileSelected(selectedGoogleDriveFile);
        } else {
            parserProgress = new ProgressDialog(getActivity());
            parserProgress.setCancelable(true);
            parserProgress.setMessage("Parsing ...");
            parserProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            parserProgress.setProgress(0);
            parserProgress.setMax(100);
            parserProgress.show();
            new FileProcessor(selectedLocalFiles).execute();
        }
    }

    public File getGpsLoggerPath() {
        File gpsLoggerFilePath = null;

        try {

            String URL = "content://com.mendhak.gpslogger/gpslogger_folder";
            Uri u = Uri.parse(URL);
            Cursor c = getActivity().getContentResolver().query(u, null, null, null, null);
            if (c != null) {
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    Log.d("GPSVisualizer", c.getString(0));
                    gpsLoggerFilePath = new File(c.getString(0));
                    c.moveToNext();
                }
                c.close();
            }

        } catch (Exception e) {
            Log.e("GPSVisualizer", "Could not determine GPSLogger's files dir", e);
        }

        return gpsLoggerFilePath;

    }

    public void openLocalFolder() {

        Intent mediaIntent = null;
        File gpsLoggerFilePath = getGpsLoggerPath();

        if (!Prefs.ShouldAlwaysOpenGPSLoggerFolder(getActivity())) {
            File lastSelectedFile = new File(Uri.parse(Prefs.GetLastOpenedFile(getActivity())).getPath());
            if (lastSelectedFile != null && !lastSelectedFile.getPath().isEmpty()) {
                gpsLoggerFilePath = new File(lastSelectedFile.getParent());
            }
        }

        mediaIntent = new Intent(getActivity(), SortedFilePickerActivity.class);
        mediaIntent.putExtra(SortedFilePickerActivity.EXTRA_ALLOW_MULTIPLE, true);
        mediaIntent.putExtra(SortedFilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        mediaIntent.putExtra(SortedFilePickerActivity.EXTRA_MODE, SortedFilePickerActivity.MODE_FILE);

        if (gpsLoggerFilePath != null && gpsLoggerFilePath.isDirectory()) {
            mediaIntent.putExtra(SortedFilePickerActivity.EXTRA_START_PATH, gpsLoggerFilePath.getPath());
        }

        startActivityForResult(mediaIntent, ACTION_FILE_PICKER);
    }


    public void openGoogleFolder() {
        googleApiClient = new GoogleApiClient.Builder(this.getActivity())
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        googleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        IntentSender intentSender = Drive.DriveApi
                .newOpenFileActivityBuilder()
                .setSelectionFilter(Filters.or(Filters.contains(SearchableField.TITLE,"gpx"),Filters.contains(SearchableField.TITLE, "nmea")))
                .build(googleApiClient);
        try {
            getActivity().startIntentSenderForResult(intentSender, GDRIVE_REQUEST_CODE_OPENER, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            Log.w("GPSVisualizer", "Unable to send intent", e);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(getActivity(), GDRIVE_RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                Log.e("GPSVisualizer", "Could not connect to Google Drive", e);
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), getActivity(), 0).show();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTION_FILE_PICKER && resultCode == Activity.RESULT_OK) {

            ArrayList<Uri> chosenFiles = new ArrayList<>();
            ClipData clip = data.getClipData();

            if (clip != null) {
                for (int i = 0; i < clip.getItemCount(); i++) {
                    //Uri uri = clip.getItemAt(i).getUri();
                    File file = com.nononsenseapps.filepicker.Utils.getFileForUri(clip.getItemAt(i).getUri());
                    Uri uri = Uri.fromFile(file);
                    Log.d("GPSVisualizer", "File URI= " + uri);
                    chosenFiles.add(uri);
                }

                parserProgress.setCancelable(true);
                parserProgress.setMessage("Parsing ...");
                parserProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                parserProgress.setProgress(0);
                parserProgress.setMax(100);
                parserProgress.show();

                selectedLocalFiles = chosenFiles;
                selectedGoogleDriveFile = null;

                if (!Prefs.ShouldAlwaysOpenGPSLoggerFolder(getActivity())) {
                    Log.d("GPSVisualizer", "Saving user selected path");
                    Prefs.SetLastOpenedFile(chosenFiles.get(0), getActivity());
                }

                new FileProcessor(chosenFiles).execute();
            }

        }

        if (requestCode == GDRIVE_RESOLVE_CONNECTION_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
            googleApiClient.connect();
        }
    }

    private class FileProcessor extends AsyncTask<Void, Void, Void> {

        private ArrayList<Uri> chosenFiles;

        FileProcessor(ArrayList<Uri> chosenFiles) {
            this.chosenFiles = chosenFiles;
        }

        @Override
        protected Void doInBackground(Void... params) {
            ProcessUserGpsFiles(chosenFiles);
            return null;
        }
    }

    public void ProcessUserGpsFiles(ArrayList<Uri> chosenFiles) {

        GpsTrack mainTrack = new GpsTrack();

        for (Uri uri : chosenFiles) {
            final File gpsFile = new File(uri.getPath());
            BaseParser parser = BaseParser.GetParserFromFilename(gpsFile.getPath());


            try {
                GpsTrack t = parser.GetTrack(new FileInputStream(gpsFile));
                mainTrack.getTrackPoints().addAll(t.getTrackPoints());
                mainTrack.getWayPoints().addAll(t.getWayPoints());

                Collections.sort(mainTrack.getTrackPoints(), new Comparator<GpsPoint>() {
                    @Override
                    public int compare(GpsPoint first, GpsPoint second) {
                        return first.getCalendar().compareTo(second.getCalendar());
                    }
                });

                Collections.sort(mainTrack.getWayPoints(), new Comparator<GpsPoint>() {
                    @Override
                    public int compare(GpsPoint first, GpsPoint second) {
                        return first.getCalendar().compareTo(second.getCalendar());
                    }
                });


            } catch (Exception e) {
                Log.e("GPSVisualizer", "Could not parse file. ", e);
            }
        }

        dataImportListener.OnDataImported(mainTrack);

        getActivity().runOnUiThread(new FileImportMessage(chosenFiles));
    }


    private void ProcessUserGpsFile(String gpxContents, String fileName) {

        InputStream stream = new ByteArrayInputStream(gpxContents.getBytes(Charsets.UTF_8));
        BaseParser parser = BaseParser.GetParserFromFilename(fileName);
        GpsTrack track = parser.GetTrack(stream);
        dataImportListener.OnDataImported(track);


        ArrayList<Uri> chosenFiles = new ArrayList<Uri>();
        chosenFiles.add(Uri.parse(fileName));
        getActivity().runOnUiThread(new FileImportMessage(new ArrayList<Uri>(Arrays.asList(Uri.parse(fileName)))));
    }

    @Override
    public void OnFileSelected(ArrayList<Uri> chosenFiles) {
        selectedLocalFiles = chosenFiles;
        selectedGoogleDriveFile = null;
        ProcessUserGpsFiles(chosenFiles);
    }

    @Override
    public void OnGoogleDriveFileSelected(final DriveId driveId) {

        downloadProgress = new ProgressDialog(getActivity());
        downloadProgress.setCancelable(true);
        downloadProgress.setMessage("File downloading ...");
        downloadProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        downloadProgress.setProgress(0);
        downloadProgress.setMax(100);
        downloadProgress.show();

        selectedGoogleDriveFile = driveId;
        processGoogleDriveFile(driveId);

    }

    private void processGoogleDriveFile(DriveId driveId) {

        final DriveFile.DownloadProgressListener listener = new DriveFile.DownloadProgressListener() {
            @Override
            public void onProgress(long bytesDownloaded, long bytesExpected) {
                int progress = (int) (bytesDownloaded * 100 / bytesExpected);
                Log.d("GPSVisualizer", String.format("Loading progress: %d percent", progress));
                downloadProgress.setProgress(progress);
            }
        };

        Log.d("GPSVisualizer", driveId.getResourceId());
        final DriveFile file = Drive.DriveApi.getFile(googleApiClient, driveId);

        file.getMetadata(googleApiClient).setResultCallback(new ResultCallback<DriveResource.MetadataResult>() {
            @Override
            public void onResult(DriveResource.MetadataResult metadataResult) {

                final String importedFileName = metadataResult.getMetadata().getTitle();

                file.open(googleApiClient, DriveFile.MODE_READ_ONLY, listener)
                        .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                            @Override
                            public void onResult(DriveApi.DriveContentsResult contentsResult) {

                                if (!contentsResult.getStatus().isSuccess()) {
                                    downloadProgress.setMessage("Failed");
                                    downloadProgress.hide();
                                    Log.e("GPSVisualizer", "Could not open Google Drive file");
                                    return;
                                }

                                downloadProgress.setProgress(100);
                                downloadProgress.hide();

                                DriveContents contents = contentsResult.getDriveContents();
                                String fileContents = convertStreamToString(contents.getInputStream());

                                ProcessUserGpsFile(fileContents, importedFileName);
                            }

                            String convertStreamToString(InputStream is) {
                                java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                                return s.hasNext() ? s.next() : "";
                            }

                        });

            }
        });
    }

    private class FileImportMessage implements Runnable {

        private ArrayList<Uri> chosenFiles;

        public FileImportMessage(ArrayList<Uri> chosenFiles) {
            this.chosenFiles = chosenFiles;
        }

        @Override
        public void run() {

            parserProgress.hide();
            TextView txtIntroduction = (TextView) rootView.findViewById(R.id.import_message);
            Button btnReload = (Button) rootView.findViewById(R.id.btn_reload);
            StringBuilder importedNames = new StringBuilder();
            for (Uri chosenFile : chosenFiles) {
                importedNames.append(new File(chosenFile.getPath()).getName());
                importedNames.append(",");
            }
            importedNames.deleteCharAt(importedNames.length() - 1);
            String importedText = "<strong>Imported " + importedNames.toString() + "</strong>";

            if (!ProcessedData.isQualityTrack()) {
                importedText += "<br />Your track does not have many elevation points. You can still view the map, " +
                        "but for best results, use a GPS logger that records elevation to get more details on the" +
                        " stats and charts tabs.";
                txtIntroduction.setText(Html.fromHtml(importedText));
                txtIntroduction.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.fade));
            } else {
                txtIntroduction.setText(Html.fromHtml(importedText));
                ((MainActivity) getActivity()).viewPager.setCurrentItem(1, true);
            }

            btnReload.setVisibility(View.VISIBLE);
        }
    }
}
