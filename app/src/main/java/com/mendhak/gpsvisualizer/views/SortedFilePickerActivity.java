package com.mendhak.gpsvisualizer.views;


import com.nononsenseapps.filepicker.AbstractFilePickerFragment;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;

public class SortedFilePickerActivity extends FilePickerActivity {

    public SortedFilePickerActivity() {
        super();
    }

    @Override
    protected AbstractFilePickerFragment<File> getFragment(
            final String startPath, final int mode, final boolean allowMultiple,
            final boolean allowCreateDir) {
        // Only the fragment in this line needs to be changed
        SortedFilePickerFragment fragment = new SortedFilePickerFragment();
        fragment.setArgs(startPath, mode, allowMultiple, allowCreateDir);
        return fragment;
    }
}
