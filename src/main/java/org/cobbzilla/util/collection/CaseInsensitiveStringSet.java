package org.cobbzilla.util.collection;

import java.util.TreeSet;

public class CaseInsensitiveStringSet extends TreeSet<String> {

    public CaseInsensitiveStringSet() { super(String.CASE_INSENSITIVE_ORDER); }

}
