package com.mendhak.gpsvisualizer;

import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.Locale;
import java.util.prefs.Preferences;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceFragment;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.mendhak.gpsvisualizer.common.GpsTrack;
import com.mendhak.gpsvisualizer.common.IDataImportListener;
import com.mendhak.gpsvisualizer.common.IFileSelectedListener;
import com.mendhak.gpsvisualizer.common.ProcessedData;
import com.mendhak.gpsvisualizer.views.ChartFragment;
import com.mendhak.gpsvisualizer.views.MainImportFragment;
import com.mendhak.gpsvisualizer.views.MapFragment;
import com.mendhak.gpsvisualizer.views.StatsFragment;


public class MainActivity extends Activity implements ActionBar.TabListener, IDataImportListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter sectionsPagerAdapter;


    /**
     * The {@link ViewPager} that will host the section contents.
     */
    public ViewPager viewPager;


    private Uri externalFile;

    /** DRIVE_OPEN Intent action. */
    private static final String ACTION_DRIVE_OPEN = "com.google.android.apps.drive.DRIVE_OPEN";
    /** Drive file ID key. */
    private static final String EXTRA_FILE_ID = "resourceId";

    /** Drive file ID. */
    private String mFileId;
    int GDRIVE_CHOOSE_ACCOUNT = 810;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        sectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(4);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < sectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(sectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        // Get the action that triggered the intent filter for this Activity
        final Intent intent = getIntent();
        final String action = intent.getAction();

        // Make sure the Action is DRIVE_OPEN.
        if (ACTION_DRIVE_OPEN.equals(action)) {
            // Get the Drive file ID.
            mFileId = intent.getStringExtra(EXTRA_FILE_ID);
            Log.d("GPSVisualizer", "File ID: " + mFileId);

            Intent accountPickerIntent = AccountPicker.newChooseAccountIntent(null, null,
                    new String[]{"com.google"}, false, null, null, null, null);
            startActivityForResult(accountPickerIntent, GDRIVE_CHOOSE_ACCOUNT);
        }

        if(action.equals(Intent.ACTION_VIEW)){

            Log.d("GPSVisualizer", intent.getData().getPath());
            externalFile = intent.getData();
//            MainImportFragment mainImportFragment = (MainImportFragment) sectionsPagerAdapter.getRegisteredFragment(0);
//            mainImportFragment.OnFileSelected(intent.getData());
//            MainImportFragment mainImportFragment = (MainImportFragment)getFragmentManager().findFragmentByTag("android:switcher:" + viewPager.getId() + ":0");
//            mainImportFragment.ProcessUserGpsFile(intent.getData());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();
        private Fragment currentFragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a MainImportFragment (defined as a static inner class below).

            if (position == 0) {
                return MainImportFragment.newInstance();

            }

            if(position == 1){
                return MapFragment.newInstance(position + 1);

            }

            if(position == 2){
                return ChartFragment.newInstance(position + 1);
            }

            return StatsFragment.newInstance(position +1);
        }

        public Fragment getCurrentFragment() {
            return currentFragment;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (getCurrentFragment() != object) {
                currentFragment = ((Fragment) object);
            }
            super.setPrimaryItem(container, position, object);
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
                case 3:
                    return "Stats".toUpperCase(l);
            }
            return null;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GDRIVE_CHOOSE_ACCOUNT && resultCode == RESULT_OK){

            String mAccountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

            if (mAccountName != null && mAccountName.length() > 0) {
                // Try retrieving existing file.

                DriveId driveId = data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                Log.d("GPSVisualizer", "resource : " + driveId.getResourceId());

            }
        }

        if(requestCode == MainImportFragment.GDRIVE_REQUEST_CODE_OPENER && resultCode == RESULT_OK ){

            DriveId driveId = data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);

            MainImportFragment mainImportFragment = (MainImportFragment) sectionsPagerAdapter.getRegisteredFragment(0);
            mainImportFragment.OnGoogleDriveFileSelected(driveId);
//            MainImportFragment mainImportFragment = (MainImportFragment)getFragmentManager().findFragmentByTag("android:switcher:" + viewPager.getId() + ":0");
//            mainImportFragment.OnGoogleDriveFileSelected(driveId);
        }
    }

    /**
     * Replace the current application-wide track
     *
     * @param track
     */
    @Override
    public void OnDataImported(GpsTrack track) {
        Log.i("GPSVisualizer", "Data imported");
        ProcessedData.SetTrack(track);
    }

    @Override
    public Uri GetPendingExternalFile() {
        return externalFile;
    }


}
