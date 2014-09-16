package org.cobbzilla.util.io;

import java.io.File;
import java.io.FileFilter;

public class DirFilter implements FileFilter {

    public static final DirFilter instance = new DirFilter();

    @Override public boolean accept(File pathname) { return pathname.isDirectory(); }

}
