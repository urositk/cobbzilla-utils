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
public class FieldTransfomer implements Transformer {

    public static final FieldTransfomer TO_NAME = new FieldTransfomer("name");
    public static final FieldTransfomer TO_ID = new FieldTransfomer("id");
    public static final FieldTransfomer TO_UUID = new FieldTransfomer("uuid");

    @Getter @Setter private String field;

    @Override public Object transform(Object o) { return ReflectionUtil.get(o, field); }

    public <E> List<E> collect (Collection c) { return (List<E>) CollectionUtils.collect(c, this); }

}