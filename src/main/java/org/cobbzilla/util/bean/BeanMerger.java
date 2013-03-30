package org.cobbzilla.util.bean;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;

public class BeanMerger {

    private static final Logger LOG = LoggerFactory.getLogger(BeanMerger.class);

    private static final PropertyUtilsBean propertyUtils = new PropertyUtilsBean();

    public static void mergeNotNullProperties(Object dest, Object orig) {

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
                    if (value != null) {
                        BeanUtils.copyProperty(dest, name, value);
                    }
                } catch (NoSuchMethodException e) {
                    // Should not happen
                } catch (Exception e) {
                    throw new IllegalStateException("Error copying properties: "+e, e);
                }
            }
        }

    }

}
