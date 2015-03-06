package org.cobbzilla.util.string;

import java.io.File;

import static org.cobbzilla.util.string.StringUtil.empty;

public class LocaleUtil {

    public static File findLocaleFile (File base, String locale) {

        if (empty(locale)) return base.exists() ? base : null;

        final String[] localeParts = locale.toLowerCase().replace("-", "_").split("_");
        final String lang = localeParts[0];
        final String region = localeParts.length > 1 ? localeParts[1] : null;
        final String variant = localeParts.length > 2 ? localeParts[2] : null;

        File found;
        if (!empty(variant)) {
            found = findSpecificLocaleFile(base, lang + "_" + region + "_" + variant);
            if (found != null) return found;
        }
        if (!empty(region)) {
            found = findSpecificLocaleFile(base, lang + "_" + region);
            if (found != null) return found;
        }
        found = findSpecificLocaleFile(base, lang);
        if (found != null) return found;

        return base.exists() ? base : null;
    }

    private static File findSpecificLocaleFile(File base, String locale) {
        final String filename = base.getName();
        final int lastDot = filename.lastIndexOf('.');
        final String prefix;
        final String suffix;
        if (lastDot != -1) {
            prefix = filename.substring(0, lastDot);
            suffix = filename.substring(lastDot);
        } else {
            prefix = filename;
            suffix = "";
        }
        final File localeFile = new File(base.getParent(), prefix + "_" + locale + suffix);
        return localeFile.exists() ? localeFile : null;
    }

}
