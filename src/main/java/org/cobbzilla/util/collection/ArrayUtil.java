package org.cobbzilla.util.collection;

import java.lang.reflect.Array;
import java.util.*;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public class ArrayUtil {

    public static final Object[] SINGLE_NULL_OBJECT = new Object[]{null};

    public static <T> T[] append (T[] array, T... elements) {
        if (array == null || array.length == 0) {
            if (elements.length == 0) return (T[]) new Object[]{}; // punt, it's empty anyway
            final T[] newArray = (T[]) Array.newInstance(elements[0].getClass(), elements.length);
            System.arraycopy(elements, 0, newArray, 0, elements.length);
            return newArray;
        } else {
            if (elements.length == 0) return Arrays.copyOf(array, array.length);
            final T[] copy = Arrays.copyOf(array, array.length + elements.length);
            System.arraycopy(elements, 0, copy, array.length, elements.length);
            return copy;
        }
    }

    public static <T> T[] concat (T[]... arrays) {
        int size = 0;
        for (T[] array : arrays) {
            size += array == null ? 0 : array.length;
        }
        final Class<?> componentType = arrays.getClass().getComponentType().getComponentType();
        final T[] newArray = (T[]) Array.newInstance(componentType, size);
        int destPos = 0;
        for (T[] array : arrays) {
            System.arraycopy(array, 0, newArray, destPos, array.length);
            destPos += array.length;
        }
        return newArray;
    }

    public static <T> T[] remove(T[] array, int indexToRemove) {
        if (array == null) throw new NullPointerException("remove: array was null");
        if (indexToRemove >= array.length || indexToRemove < 0) throw new IndexOutOfBoundsException("remove: cannot remove element "+indexToRemove+" from array of length "+array.length);
        final List<T> list = new ArrayList<>(Arrays.asList(array));
        list.remove(indexToRemove);
        final T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length-1);
        return list.toArray(newArray);
    }

    /**
     * Return a slice of an array. If from == to then an empty array will be returned.
     * @param array the source array
     * @param from the start index, inclusive. If less than zero or greater than the length of the array, an Exception is thrown
     * @param to the end index, NOT inclusive. If less than zero or greater than the length of the array, an Exception is thrown
     * @param <T> the of the array
     * @return A slice of the array. The source array is not modified.
     */
    public static <T> T[] slice(T[] array, int from, int to) {

        if (array == null) throw new NullPointerException("slice: array was null");
        if (from < 0 || from > array.length) die("slice: invalid 'from' index ("+from+") for array of size "+array.length);
        if (to < 0 || to < from || to > array.length) die("slice: invalid 'to' index ("+to+") for array of size "+array.length);

        final T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), to-from);
        if (to == from) return newArray;
        System.arraycopy(array, from, newArray, 0, to-from);
        return newArray;
    }

    public static <T> List<T> merge(Collection<T>... collections) {
        if (empty(collections)) return Collections.emptyList();
        final Set<T> result = new HashSet<>();
        for (Collection<T> c : collections) result.addAll(c);
        return new ArrayList<>(result);
    }

}
