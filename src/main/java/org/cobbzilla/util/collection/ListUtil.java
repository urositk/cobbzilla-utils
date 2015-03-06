package org.cobbzilla.util.collection;

import java.util.ArrayList;
import java.util.List;

public class ListUtil {

    public static <T> List<T> concat (List<T> list1, List<T> list2) {
        if (list1 == null || list1.isEmpty()) return list2 == null ? null : new ArrayList<>(list2);
        if (list2 == null || list2.isEmpty()) return new ArrayList<>(list1);
        final List<T> newList = new ArrayList<>(list1.size() + list2.size());
        newList.addAll(list1);
        newList.addAll(list2);
        return newList;
    }
}
