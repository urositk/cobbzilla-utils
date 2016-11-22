package org.cobbzilla.util.collection;

import java.util.Comparator;

public interface HasPriority {

    boolean hasPriority ();
    Integer getPriority ();

    Comparator<HasPriority> SORT_PRIORITY = new Comparator<HasPriority>() {
        @Override public int compare(HasPriority r1, HasPriority r2) {
            if (!r2.hasPriority()) return r1.hasPriority() ? -1 : 0;
            if (!r1.hasPriority()) return 1;
            return r1.getPriority().compareTo(r2.getPriority());
        }
    };

}
