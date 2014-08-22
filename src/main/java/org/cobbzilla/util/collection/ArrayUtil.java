package org.cobbzilla.util.collection;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArrayUtil {

    public static <T> T[] append (T[] array, T element) {
        if (array == null || array.length == 0) {
            final T[] newArray = (T[]) Array.newInstance(element.getClass(), 1);
            newArray[0] = element;
            return newArray;
        } else {
            final List<T> updated = new ArrayList<>(array.length);
            Collections.addAll(updated, array);
            updated.add(element);
            return updated.toArray(array);
        }
    }

}
