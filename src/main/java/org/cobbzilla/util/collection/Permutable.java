package org.cobbzilla.util.collection;

import java.util.Collection;
import java.util.List;

public interface Permutable<T> {

    Collection<List<T>> permute();

}
