package org.cobbzilla.util.bean;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.cobbzilla.util.reflect.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;

public class BeanMerger {

    private static final Logger LOG = LoggerFactory.getLogger(BeanMerger.class);

    private static final PropertyUtilsBean propertyUtils = new PropertyUtilsBean();

    public static void mergeProperties(Object dest, Object orig) {
        merge(dest, orig, AlwaysCopy.INSTANCE);
    }

    public static void mergeNotNullProperties(Object dest, Object orig) {
        merge(dest, orig, NotNull.INSTANCE);
    }

    private static void merge(Object dest, Object orig, CopyEvaluator evaluator) {

        if (dest == null) throw new IllegalArgumentException ("No destination bean specified");
        if (orig == null) throw new IllegalArgumentException("No origin bean specified");

        PropertyDescriptor[] origDescriptors = propertyUtils.getPropertyDescriptors(orig);
        for (PropertyDescriptor origDescriptor : origDescriptors) {
            String name = origDescriptor.getName();
            if ("class".equals(name)) {
                continue; // No point in trying to set an object's class
            }
            if (propertyUtils.isReadable(orig, name) &&
                    propertyUtils.isWriteable(dest, name)) {
                try {
                    Object value = propertyUtils.getSimpleProperty(orig, name);
                    if (evaluator.shouldCopy(name, value)) {
                        if (value == null) {
                            ReflectionUtil.setNull(dest, name, origDescriptor.getPropertyType());
                        } else {
                            ReflectionUtil.set(dest, name, value);
                        }
                    }
                } catch (NoSuchMethodException e) {
                    // Should not happen
                } catch (Exception e) {
                    throw new IllegalStateException("Error copying properties: "+e, e);
                }
            }
        }
    }

    private interface CopyEvaluator {
        boolean shouldCopy(String name, Object value);
    }
    static class AlwaysCopy implements CopyEvaluator {
        static final AlwaysCopy INSTANCE = new AlwaysCopy();
        @Override
        public boolean shouldCopy(String name, Object value) { return true; }
    }
    static class NotNull implements CopyEvaluator {
        static final NotNull INSTANCE = new NotNull();
        @Override
        public boolean shouldCopy(String name, Object value) {
            return value != null;
        }
    }
}
