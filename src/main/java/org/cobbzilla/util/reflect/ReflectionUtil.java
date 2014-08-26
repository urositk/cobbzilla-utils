package org.cobbzilla.util.reflect;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.MethodUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
public class ReflectionUtil {

    private enum Accessor { get, set }

    public static <T> T copy (T dest, T src) {
        try {
            for (Method getter : src.getClass().getMethods()) {
                final Class<?>[] types = getter.getParameterTypes();
                if (types.length != 0) continue;

                final String setterName = setterForGetter(getter.getName());
                if (setterName != null) {
                    final Method setter;
                    try {
                        setter = dest.getClass().getMethod(setterName, getter.getReturnType());
                    } catch (Exception e) {
                        log.debug("copy: setter not found: "+setterName);
                        continue;
                    }
                    setter.invoke(dest, getter.invoke(src));
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error copying "+dest.getClass().getSimpleName()+" from src="+src+": "+e, e);
        }
        return dest;
    }

    public static String setterForGetter(String getter) {
        if (getter.startsWith("get")) return "set"+getter.substring(3);
        if (getter.startsWith("is")) return "set"+getter.substring(2);
        return null;
    }

    public static <T> T copyFromMap (T dest, Map<String, Object> src) {
        for (Map.Entry<String, Object> entry : src.entrySet()) {
            if (hasSetter(dest, entry.getKey(), entry.getValue().getClass())) {
                set(dest, entry.getKey(), entry.getValue());
            }
        }
        return dest;
    }

    public static Class getFirstTypeParam(Class clazz) {
        final ParameterizedType parameterizedType = (ParameterizedType) clazz.getGenericSuperclass();
        final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        return (Class) actualTypeArguments[0];
//        return ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0].getClass();
    }

    public static Object get(Object object, String field) {
        Object target = object;
        for (String token : field.split("\\.")) {
            if (target == null) return null;
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
        if (target != null) invoke_set(target, tokens[tokens.length-1], value);
    }

    public static void setNull(Object object, String field, Class type) {
        final String[] tokens = field.split("\\.");
        Object target = getTarget(object, tokens);
        if (target != null) invoke_set_null(target, tokens[tokens.length - 1], type);
    }

    private static Object getTarget(Object object, String[] tokens) {
        Object target = object;
        for (int i=0; i<tokens.length-1; i++) {
            target = invoke_get(target, tokens[i]);
            if (target == null) {
                log.warn("getTarget("+object+", "+Arrays.toString(tokens)+"): exiting early, null object found at token="+tokens[i]);
                return null;
            }
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

    // methods below forked from dropwizard-- https://github.com/codahale/dropwizard

    /**
     * Finds the type parameter for the given class.
     *
     * @param klass    a parameterized class
     * @return the class's type parameter
     */
    public static Class<?> getTypeParameter(Class<?> klass) {
        return getTypeParameter(klass, Object.class);
    }

    /**
     * Finds the type parameter for the given class which is assignable to the bound class.
     *
     * @param klass    a parameterized class
     * @param bound    the type bound
     * @param <T>      the type bound
     * @return the class's type parameter
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getTypeParameter(Class<?> klass, Class<? super T> bound) {
        Type t = checkNotNull(klass);
        while (t instanceof Class<?>) {
            t = ((Class<?>) t).getGenericSuperclass();
        }
        /* This is not guaranteed to work for all cases with convoluted piping
         * of type parameters: but it can at least resolve straight-forward
         * extension with single type parameter (as per [Issue-89]).
         * And when it fails to do that, will indicate with specific exception.
         */
        if (t instanceof ParameterizedType) {
            // should typically have one of type parameters (first one) that matches:
            for (Type param : ((ParameterizedType) t).getActualTypeArguments()) {
                if (param instanceof Class<?>) {
                    final Class<T> cls = determineClass(bound, param);
                    if (cls != null) { return cls; }
                }
                else if (param instanceof TypeVariable) {
                    for (Type paramBound : ((TypeVariable<?>) param).getBounds()) {
                        if (paramBound instanceof Class<?>) {
                            final Class<T> cls = determineClass(bound, paramBound);
                            if (cls != null) { return cls; }
                        }
                    }
                }
            }
        }
        throw new IllegalStateException("Cannot figure out type parameterization for " + klass.getName());
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> determineClass(Class<? super T> bound, Type candidate) {
        if (candidate instanceof Class<?>) {
            final Class<?> cls = (Class<?>) candidate;
            if (bound.isAssignableFrom(cls)) {
                return (Class<T>) cls;
            }
        }

        return null;
    }

}
