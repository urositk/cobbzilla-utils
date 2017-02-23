package org.cobbzilla.util.collection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.cobbzilla.util.reflect.ReflectionUtil;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
public class FieldTransformer implements Transformer {

    public static final FieldTransformer TO_NAME = new FieldTransformer("name");
    public static final FieldTransformer TO_ID = new FieldTransformer("id");
    public static final FieldTransformer TO_UUID = new FieldTransformer("uuid");

    @Getter private final String field;

    @Override public Object transform(Object o) { return ReflectionUtil.get(o, field); }

    public <E> List<E> collect (Collection c) { return (List<E>) CollectionUtils.collect(c, this); }

}