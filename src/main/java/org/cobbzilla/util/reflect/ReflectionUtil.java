package org.cobbzilla.util.reflect;

import org.apache.commons.beanutils.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ReflectionUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ReflectionUtil.class);

    private enum Accessor { get, set }

    public static Class getFirstTypeParam(Class clazz) {
        final ParameterizedType parameterizedType = (ParameterizedType) clazz.getGenericSuperclass();
        final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        return (Class) actualTypeArguments[0];
//        return ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0].getClass();
    }

    public static Object get(Object object, String field) {
        Object target = object;
        for (String token : field.split("\\.")) {
            target = invoke_get(target, token);
        }
        return target;
    }

    public static boolean hasGetter(Object object, String field) {
        Object target = object;
        try {
            for (String token : field.split("\\.")) {
                final String methodName = getAccessorMethodName(Accessor.get, token);
                target = MethodUtils.invokeExactMethod(target, methodName, null);
            }
        } catch (NoSuchMethodException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static void set(Object object, String field, Object value) {
        final String[] tokens = field.split("\\.");
        Object target = getTarget(object, tokens);
        invoke_set(target, tokens[tokens.length-1], value);
    }

    public static void setNull(Object object, String field, Class type) {
        final String[] tokens = field.split("\\.");
        Object target = getTarget(object, tokens);
        invoke_set_null(target, tokens[tokens.length - 1], type);
    }

    private static Object getTarget(Object object, String[] tokens) {
        Object target = object;
        for (int i=0; i<tokens.length-1; i++) {
            target = invoke_get(target, tokens[i]);
        }
        return target;
    }

    public static boolean hasSetter(Object object, String field, Class type) {
        Object target = object;
        final String[] tokens = field.split("\\.");
        try {
            for (int i=0; i<tokens.length-1; i++) {
                target = MethodUtils.invokeExactMethod(target, tokens[i], null);
            }

            target.getClass().getMethod(getAccessorMethodName(Accessor.set, tokens[tokens.length-1]), type);

        } catch (NoSuchMethodException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static String getAccessorMethodName(Accessor accessor, String token) {
        return token.length() == 1 ? accessor.name() +token.toUpperCase() : accessor.name() + token.substring(0, 1).toUpperCase() + token.substring(1);
    }

    private static Object invoke_get(Object target, String token) {
        final String methodName = getAccessorMethodName(Accessor.get, token);
        try {
            target = MethodUtils.invokeMethod(target, methodName, null);
        } catch (Exception e) {
            throw new IllegalStateException("Error calling "+methodName+": "+e);
        }
        return target;
    }

    private static void invoke_set(Object target, String token, Object value) {
        if (value == null) {
            throw new IllegalArgumentException("invoke_set: "+token+" cannot have null value, use setNull");
        }
        final String methodName = getAccessorMethodName(Accessor.set, token);
        try {
            MethodUtils.invokeMethod(target, methodName, value == null ? new Object[] { null } : value);
        } catch (Exception e) {
            throw new IllegalStateException("Error calling "+methodName+": "+e);
        }
    }

    private static void invoke_set_null(Object target, String token, Class type) {
        final String methodName = getAccessorMethodName(Accessor.set, token);
        try {
            MethodUtils.invokeMethod(target, methodName, new Object[] { null }, new Class[] { type });
        } catch (Exception e) {
            throw new IllegalStateException("Error calling "+methodName+": "+e);
        }
    }

}
