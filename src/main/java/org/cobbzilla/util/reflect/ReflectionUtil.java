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
            final String methodName = getAccessorMethodName(Accessor.get, token);
            target = invoke(target, methodName);
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
        Object target = object;
        final String[] tokens = field.split("\\.");
        for (int i=0; i<tokens.length-1; i++) {
            final String methodName = getAccessorMethodName(Accessor.get, tokens[i]);
            target = invoke(target, methodName);
        }
        invoke(target, getAccessorMethodName(Accessor.set, tokens[tokens.length-1]));
    }

    public static boolean hasSetter(Object object, String field, Class type) {
        Object target = object;
        final String[] tokens = field.split("\\.");
        try {
            for (int i=0; i<tokens.length-1; i++) {
                final String methodName = getAccessorMethodName(Accessor.get, tokens[i]);
                target = MethodUtils.invokeExactMethod(target, methodName, null);
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

    private static Object invoke(Object target, String methodName) {
        try {
            target = MethodUtils.invokeExactMethod(target, methodName, null);
        } catch (Exception e) {
            throw new IllegalStateException("Error calling "+methodName+": "+e);
        }
        return target;
    }

}
