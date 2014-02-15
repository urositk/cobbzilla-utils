package org.cobbzilla.util.mustache;

import lombok.AllArgsConstructor;

import java.io.File;

@AllArgsConstructor
class LAMFCacheKey {

    public File root;
    public String locale;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LAMFCacheKey)) return false;

        LAMFCacheKey that = (LAMFCacheKey) o;

        if (!root.equals(that.root)) return false;
        if (!locale.equals(that.locale)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = root.hashCode();
        result = 31 * result + locale.hashCode();
        return result;
    }
}
