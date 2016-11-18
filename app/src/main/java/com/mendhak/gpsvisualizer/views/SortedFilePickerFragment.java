package com.mendhak.gpsvisualizer.views;

import android.support.annotation.NonNull;
import com.nononsenseapps.filepicker.FilePickerFragment;

import java.io.File;

public class SortedFilePickerFragment extends FilePickerFragment {

    /**
     *
     * @param file
     * @return The file extension. If file has no extension, it returns null.
     */
    private String getExtension(@NonNull File file) {
        String path = file.getPath();
        int i = path.lastIndexOf(".");
        if (i < 0) {
            return null;
        } else {
            return path.substring(i);
        }
    }

    /**
     * Compare two files to determine their relative sort order. This follows the usual
     * comparison interface. Override to determine your own custom sort order.
     *
     * @param lhs File on the "left-hand side"
     * @param rhs File on the "right-hand side"
     * @return -1 if if lhs should be placed before rhs, 0 if they are equal,
     * and 1 if rhs should be placed before lhs
     */
    @Override
    protected int compareFiles(File lhs, File rhs) {
        if (lhs.isDirectory() && !rhs.isDirectory()) {
            return -1;
        } else if (rhs.isDirectory() && !lhs.isDirectory()) {
            return 1;
        }
        else if (lhs.lastModified() > rhs.lastModified()) {
            return -1;
        }
        else if (lhs.lastModified() == rhs.lastModified()){
            return 0;
        }
        else {
            return 1;
        }
    }
}