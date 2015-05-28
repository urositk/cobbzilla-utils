package org.cobbzilla.util.collection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.Transformer;
import org.cobbzilla.util.reflect.ReflectionUtil;

@AllArgsConstructor
public class FieldTransfomer implements Transformer {

    @Getter @Setter private String field;

    @Override public Object transform(Object o) { return ReflectionUtil.get(o, field); }

}