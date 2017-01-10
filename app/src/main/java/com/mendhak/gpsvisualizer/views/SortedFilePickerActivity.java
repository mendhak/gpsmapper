package com.mendhak.gpsvisualizer.views;


import android.support.annotation.Nullable;
import com.nononsenseapps.filepicker.AbstractFilePickerFragment;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;

public class SortedFilePickerActivity extends FilePickerActivity {

    public SortedFilePickerActivity() {
        super();
    }

    @Override
    protected AbstractFilePickerFragment<File> getFragment(@Nullable String startPath, int mode, boolean allowMultiple, boolean allowCreateDir, boolean allowExistingFile, boolean singleClick) {
        SortedFilePickerFragment fragment = new SortedFilePickerFragment();
        fragment.setArgs(startPath, mode, allowMultiple, allowCreateDir, allowExistingFile, singleClick);
        return fragment;
    }

}
