package com.mendhak.gpsvisualizer.views;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.*;
import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.mendhak.gpsvisualizer.MainActivity;
import com.mendhak.gpsvisualizer.R;
import com.mendhak.gpsvisualizer.common.GpsTrack;
import com.mendhak.gpsvisualizer.common.IDataImportListener;
import com.mendhak.gpsvisualizer.common.IFileSelectedListener;
import com.mendhak.gpsvisualizer.common.ProcessedData;
import com.mendhak.gpsvisualizer.parsers.BaseParser;
import com.mendhak.gpsvisualizer.parsers.Gpx10Parser;
import com.mendhak.gpsvisualizer.parsers.NmeaParser;


import java.io.*;
import java.util.concurrent.TimeUnit;


public class MainImportFragment extends Fragment implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, IFileSelectedListener {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final Integer ACTION_FILE_PICKER = 41792;
    int GDRIVE_RESOLVE_CONNECTION_REQUEST_CODE = 99;
    public static int GDRIVE_REQUEST_CODE_OPENER = 39;
    private View rootView;
    private IDataImportListener dataImportListener;

    ProgressDialog parserProgress;

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
        btnImport.setOnClickListener(this);
        btnGoogle.setOnClickListener(this);

        dataImportListener = (IDataImportListener)getActivity();

        btnImport.setCompoundDrawablesWithIntrinsicBounds(R.drawable.esfileexplorer, 0, 0, 0);

        parserProgress = new ProgressDialog(getActivity());

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Uri pendingFile = dataImportListener.GetPendingExternalFile();
        if(pendingFile != null){
            OnFileSelected(pendingFile);
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
        }
    }

    public void openLocalFolder() {
        Intent mediaIntent = new Intent(Intent.ACTION_GET_CONTENT);
        mediaIntent.setType("file/*"); //set mime type as per requirement
        startActivityForResult(mediaIntent, ACTION_FILE_PICKER);
    }

    GoogleApiClient googleApiClient;

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
                .setMimeType(new String[]{"application/vnd.google-earth.gpx+xml", "text/xml", "text/*", "application/gpx+xml", "application/xml"})
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
                Log.e("GPSVisualizer","Could not connect to Google Drive", e);
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), getActivity(), 0).show();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTION_FILE_PICKER  && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            Log.d("GPSVisualizer", "File URI= " + uri);

            parserProgress.setCancelable(true);
            parserProgress.setMessage("Parsing ...");
            parserProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            parserProgress.setProgress(0);
            parserProgress.setMax(100);
            parserProgress.show();

            new FileProcessor().execute(uri);
        }

        if (requestCode == GDRIVE_RESOLVE_CONNECTION_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
            googleApiClient.connect();
        }
    }

    private class FileProcessor extends AsyncTask<Uri, Void, Void>{
        @Override
        protected Void doInBackground(Uri... uris) {
            ProcessUserGpsFile(uris[0]);
            return null;
        }
    }

    public void ProcessUserGpsFile(Uri uri) {

        final File gpsFile = new File(uri.getPath());

        BaseParser parser = BaseParser.GetParserFromFilename(gpsFile.getPath());

        try {
            parser.Parse(new FileInputStream(gpsFile), dataImportListener);

        } catch (Exception e) {
            Log.e("GPSVisualizer", "Could not parse file. ", e);
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                parserProgress.hide();
                TextView txtIntroduction = (TextView) rootView.findViewById(R.id.section_label);
                txtIntroduction.setText("Imported " + gpsFile.getName());
                ((MainActivity) getActivity()).viewPager.setCurrentItem(1, true);
            }
        });
    }


    private void ProcessUserGpsFile(String gpxContents, String fileName) {

        InputStream stream = new ByteArrayInputStream(gpxContents.getBytes(Charsets.UTF_8));
        Gpx10Parser parser = new Gpx10Parser();
        parser.Parse(stream, dataImportListener);

        TextView txtIntroduction = (TextView) rootView.findViewById(R.id.section_label);
        txtIntroduction.setText("Imported " + fileName);

        ((MainActivity) getActivity()).viewPager.setCurrentItem(1, true);
    }




    @Override
    public void OnFileSelected(Uri uri) {
        ProcessUserGpsFile(uri);
    }


    @Override
    public void OnGoogleDriveFileSelected(final DriveId driveId) {

        final ProgressDialog progressBar = new ProgressDialog(getActivity());
        progressBar.setCancelable(true);
        progressBar.setMessage("File downloading ...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.show();

        final DriveFile.DownloadProgressListener listener = new DriveFile.DownloadProgressListener() {
            @Override
            public void onProgress(long bytesDownloaded, long bytesExpected) {
                int progress = (int) (bytesDownloaded * 100 / bytesExpected);
                Log.d("GPSVisualizer", String.format("Loading progress: %d percent", progress));
                progressBar.setProgress(progress);
            }
        };

        Log.d("GPSVisualizer", driveId.getResourceId());
        final DriveFile file = Drive.DriveApi.getFile(googleApiClient, driveId);

        file.getMetadata(googleApiClient).setResultCallback(new ResultCallback<DriveResource.MetadataResult>() {
            @Override
            public void onResult(DriveResource.MetadataResult metadataResult) {

                final String importedFileName = metadataResult.getMetadata().getTitle();


                file.openContents(googleApiClient, DriveFile.MODE_READ_ONLY, listener)
                        .setResultCallback(new ResultCallback<DriveApi.ContentsResult>() {
                            @Override
                            public void onResult(DriveApi.ContentsResult contentsResult) {

                                if (!contentsResult.getStatus().isSuccess()) {
                                    progressBar.setMessage("Failed");
                                    progressBar.hide();
                                    Log.e("GPSVisualizer", "Could not open Google Drive file");
                                    return;
                                }

                                progressBar.setProgress(100);
                                progressBar.hide();

                                Contents contents = contentsResult.getContents();
                                String fileContents = convertStreamToString(contents.getInputStream());
                                contents.close();
                                ProcessUserGpsFile(fileContents, importedFileName);
                            }

                            String convertStreamToString(java.io.InputStream is) {
                                java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                                return s.hasNext() ? s.next() : "";
                            }

                        });

            }
        });


    }
}
