package com.mendhak.gpsvisualizer;

import java.util.Locale;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Lists;
import com.mendhak.gpsvisualizer.common.GpsPoint;
import com.mendhak.gpsvisualizer.common.GpsTrack;
import com.mendhak.gpsvisualizer.views.ChartFragment;
import com.mendhak.gpsvisualizer.views.MapFragment;


public class MainActivity extends Activity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    GpsTrack flatTrack;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }


        ProcessUserGpsFile();
    }

    private void ProcessUserGpsFile() {
        flatTrack = new GpsTrack();

        flatTrack.addPoints(Lists.newArrayList(
                GpsPoint.from(45.4431641f,-121.7295456f,null),
                GpsPoint.from(45.4428615f,-121.7290800f,null),
                GpsPoint.from(45.4425697f,-121.7279085f,null),
                GpsPoint.from(45.4424274f,-121.7267360f,null),
                GpsPoint.from(45.4422017f,-121.7260429f,null),
                GpsPoint.from(45.4416576f,-121.7252347f,null),
                GpsPoint.from(45.4406144f,-121.7241181f,null),
                GpsPoint.from(45.4398193f,-121.7224890f,null),
                GpsPoint.from(45.4387649f,-121.7226112f,null),
                GpsPoint.from(45.4383933f,-121.7224328f,null),
                GpsPoint.from(45.4377850f,-121.7224159f,null),
                GpsPoint.from(45.4372204f,-121.7226603f,null),
                GpsPoint.from(45.4347837f,-121.7226007f,null),
                GpsPoint.from(45.4332000f,-121.7216480f,null),
                GpsPoint.from(45.4334576f,-121.7223143f,null),
                GpsPoint.from(45.4321730f,-121.7222102f,null),
                GpsPoint.from(45.4316609f,-121.7219974f,null),
                GpsPoint.from(45.4303068f,-121.7220616f,null),
                GpsPoint.from(45.4270753f,-121.7209685f,null),
                GpsPoint.from(45.4267610f,-121.7211872f,null),
                GpsPoint.from(45.4260133f,-121.7212623f,null),
                GpsPoint.from(45.4257683f,-121.7214738f,null),
                GpsPoint.from(45.4257400f,-121.7217762f,null),
                GpsPoint.from(45.4259485f,-121.7226009f,null),
                GpsPoint.from(45.4249972f,-121.7223672f,null),
                GpsPoint.from(45.4246035f,-121.7219816f,null),
                GpsPoint.from(45.4238682f,-121.7219830f,null),
                GpsPoint.from(45.4226721f,-121.7216494f,null),
                GpsPoint.from(45.4224120f,-121.7217998f,null),
                GpsPoint.from(45.4211497f,-121.7218767f,null),
                GpsPoint.from(45.4193319f,-121.7208650f,null),
                GpsPoint.from(45.4186435f,-121.7202956f,null),
                GpsPoint.from(45.4185934f,-121.7200745f,null),
                GpsPoint.from(45.4178963f,-121.7196035f,null),
                GpsPoint.from(45.4171101f,-121.7198115f,null),
                GpsPoint.from(45.4166827f,-121.7193250f,null),
                GpsPoint.from(45.4161855f,-121.7190778f,null),
                GpsPoint.from(45.4159291f,-121.7193146f,null),
                GpsPoint.from(45.4153644f,-121.7193939f,null),
                GpsPoint.from(45.4151268f,-121.7191578f,null),
                GpsPoint.from(45.4148071f,-121.7191043f,null),
                GpsPoint.from(45.4146310f,-121.7187962f,null),
                GpsPoint.from(45.4142524f,-121.7187236f,null),
                GpsPoint.from(45.4142844f,-121.7185595f,null),
                GpsPoint.from(45.4133520f,-121.7180429f,null),
                GpsPoint.from(45.4131406f,-121.7181383f,null),
                GpsPoint.from(45.4130356f,-121.7179036f,null),
                GpsPoint.from(45.4118436f,-121.7168789f,null),
                GpsPoint.from(45.4109205f,-121.7156569f,null),
                GpsPoint.from(45.4104523f,-121.7145250f,null),
                GpsPoint.from(45.4104930f,-121.7143814f,null),
                GpsPoint.from(45.4102075f,-121.7140608f,null),
                GpsPoint.from(45.4099806f,-121.7134527f,null)
        ));

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
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
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
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).


            if (position == 0) {
                return MapFragment.newInstance(position + 1);
            }

            if(position == 1){
                return ChartFragment.newInstance(position + 1);
            }

            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
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
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }


}
