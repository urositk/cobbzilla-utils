package org.cobbzilla.util.reflect;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.cobbzilla.util.collection.ArrayUtil;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.string.StringUtil.uncapitalize;

/**
 * Handy tools for working quickly with reflection APIs, which tend to be verbose.
 */
@Slf4j
public class ReflectionUtil {

    /**
     * Do a Class.forName and only throw unchecked exceptions.
     * @param clazz full class name
     * @param <T> The class type
     * @return A Class&lt;clazz&gt; object
     */
    public static <T> Class<? extends T> forName(String clazz) {
        try {
            return (Class<? extends T>) Class.forName(clazz);
        } catch (Exception e) {
            return die("Class.forName("+clazz+") error: "+e, e);
        }
    }

    /**
     * Create an instance of a class, only throwing unchecked exceptions. The class must have a default constructor.
     * @param clazz we will instantiate an object of this type
     * @param <T> The class type
     * @return An Object that is an instance of Class&lt;clazz&gt; object
     */
    public static <T> T instantiate(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            return die("Error instantiating "+clazz+": "+e, e);
        }
    }

    /**
     * Create an instance of a class based on a class name, only throwing unchecked exceptions. The class must have a default constructor.
     * @param clazz full class name
     * @param <T> The class type
     * @return An Object that is an instance of Class&lt;clazz&gt; object
     */
    public static <T> T instantiate(String clazz) {
        try {
            return (T) instantiate(forName(clazz));
        } catch (Exception e) {
            return die("Error instantiating "+clazz+": "+e, e);
        }
    }

    private enum Accessor { get, set }

    /**
     * Copies fields from src to dest.
     *
     * For each field in src that (1) starts with "get" (2) takes zero arguments and (3) has a return value:
     * The value returned from the source getter will be copied to the destination (via setter), if a setter exists, and:
     * (1) No getter exists on the destination, or (2) the destination's getter returns a different value (.equals returns false)
     *
     * @param dest destination object
     * @param src source object
     * @param <T> objects must share a type (can this be relaxed? probably)
     * @return count of fields copied
     */
    public static <T> int copy (T dest, T src) {
        return copy(dest, src, null);
    }

    /**
     * Same as copy(dest, src) but only named fields are copied
     * @param dest destination object
     * @param src source object
     * @param fields only fields with these names will be considered for copying
     * @param <T> objects must share a type
     * @return count of fields copied
     */
    public static <T> int copy (T dest, T src, String[] fields) {
        int copyCount = 0;
        try {
            for (Method getter : src.getClass().getMethods()) {
                // only look for getters on the source object (methods with no arguments that have a return value)
                final Class<?>[] types = getter.getParameterTypes();
                if (types.length != 0) continue;
                if (getter.getReturnType().equals(Void.class)) continue;;

                // and it must be named appropriately
                final String fieldName = fieldName(getter.getName());
                if (fieldName == null) continue;

                // if specific fields were given, it must be one of those
                if (fields != null && !ArrayUtils.contains(fields, fieldName)) continue;

                // what would the setter be called?
                final String setterName = setterForGetter(getter.getName());
                if (setterName == null) continue;

                // get the setter method on the destination object
                final Method setter;
                try {
                    setter = dest.getClass().getMethod(setterName, getter.getReturnType());
                } catch (Exception e) {
                    log.debug("copy: setter not found: "+setterName);
                    continue;
                }

                // do not copy null fields (should this be configurable?)
                final Object srcValue = getter.invoke(src);
                if (srcValue == null) continue;

                // does the dest have a getter? if so grab the current value
                Object destValue = null;
                try {
                    destValue = getter.invoke(dest);
                } catch (Exception e) {
                    log.debug("copy: error calling getter on dest: "+e);
                }

                // copy the value from src to dest, if it's different
                if (!srcValue.equals(destValue)) {
                    setter.invoke(dest, srcValue);
                    copyCount++;
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error copying "+dest.getClass().getSimpleName()+" from src="+src+": "+e, e);
        }
        return copyCount;
    }

    public static String fieldName(String method) {
        if (method.startsWith("get")) return uncapitalize(method.substring(3));
        if (method.startsWith("set")) return uncapitalize(method.substring(3));
        if (method.startsWith("is")) return uncapitalize(method.substring(2));
        return null;
    }

    public static String setterForGetter(String getter) {
        if (getter.startsWith("get")) return "set"+getter.substring(3);
        if (getter.startsWith("is")) return "set"+getter.substring(2);
        return null;
    }

    /**
     * Call setters on an object based on keys and values in a Map
     * @param dest destination object
     * @param src map of field name -> value
     * @param <T> type of object
     * @return the destination object
     */
    public static <T> T copyFromMap (T dest, Map<String, Object> src) {
        for (Map.Entry<String, Object> entry : src.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            if (value != null && Map.class.isAssignableFrom(value.getClass())) {
                if (hasGetter(dest, key)) {
                    Map m = (Map) value;
                    if (m.isEmpty()) continue;
                    if (m.keySet().iterator().next().getClass().equals(String.class)) {
                        copyFromMap(get(dest, key), (Map<String, Object>) m);
                    } else {
                        log.info("copyFromMap: not recursively copying Map (has non-String keys): " + key);
                    }
                }
            } else {
                if (Map.class.isAssignableFrom(dest.getClass())) {// || dest.getClass().getName().equals(HashMap.class.getName())) {
                    ((Map) dest).put(key, value);
                } else {
                    if (hasSetter(dest, key, value.getClass())) {
                        set(dest, key, value);
                    }
                }
            }
        }
        return dest;
    }

    public static Class getFirstTypeParam(Class clazz) {
        Class check = clazz;
        while (check.getGenericSuperclass() == null || !(check.getGenericSuperclass() instanceof ParameterizedType)) {
            check = check.getSuperclass();
            if (check.equals(Object.class)) die("getFirstTypeParam("+clazz.getName()+"): no type parameters found");
        }
        final ParameterizedType parameterizedType = (ParameterizedType) check.getGenericSuperclass();
        final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        return (Class) actualTypeArguments[0];
    }

    public static Class getFirstTypeParam(Class clazz, Class impl) {
        if (impl.isInterface()) {
            Class check = clazz;
            while (!check.equals(Object.class)) {
                final Type[] interfaces = check.getGenericInterfaces();
                for (Type t : interfaces) {
                    if (t instanceof ParameterizedType) {
                        final ParameterizedType ptype = (ParameterizedType) t;
                        if (impl.isAssignableFrom((Class<?>) ptype.getRawType())) {
                            return (Class) ptype.getActualTypeArguments()[0];
                        }
                    }
                }
                check = check.getSuperclass();
            }
        } else {
            Type check = clazz.getGenericSuperclass();
            while (check != null) {
                if (check instanceof ParameterizedType) {
                    final ParameterizedType ptype = (ParameterizedType) check;
                    final Class<?> rawType = (Class<?>) ptype.getRawType();
                    if (impl.isAssignableFrom(rawType)) return getFirstTypeParam(rawType);
                    check = rawType.getGenericSuperclass();
                } else {
                    break;
                }
            }
        }
        return null;
    }

    /**
     * Call a getter. getXXX and isXXX will both be checked.
     * @param object the object to call get(field) on
     * @param field the field name
     * @return the value of the field
     * @throws IllegalArgumentException If no getter for the field exists
     */
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

    /**
     * Call a setter
     * @param object the object to call set(field) on
     * @param field the field name
     * @param value the value to set
     */
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
            final String isMethod = methodName.replaceFirst("get", "is");
            try {
                target = MethodUtils.invokeMethod(target, isMethod, null);
            } catch (Exception e2) {
                return die("Error calling "+methodName+" and "+isMethod+": "+e+", "+e2);
            }
        }
        return target;
    }

    private static void invoke_set(Object target, String token, Object value) {
        final String methodName = getAccessorMethodName(Accessor.set, token);
        if (value == null) {
            // try to find a single-arg method named methodName...
            Method found = null;
            for (Method m : target.getClass().getMethods()) {
                if (m.getName().equals(methodName) && m.getParameterTypes().length == 1) {
                    if (found != null) {
                        die("invoke_set: value was null and multiple single-arg methods named " + methodName + " exist");
                    } else {
                        found = m;
                    }
                }
            }
            if (found == null) die("invoke_set: no method " + methodName + " found on target: " + target);
            try {
                found.invoke(target, ArrayUtil.SINGLE_NULL_OBJECT);
            } catch (Exception e) {
                die("Error calling " + methodName + ": " + e);
            }
        } else {
            try {
                MethodUtils.invokeMethod(target, methodName, value);
            } catch (Exception e) {
                die("Error calling " + methodName + ": " + e);
            }
        }
    }

    private static void invoke_set_null(Object target, String token, Class type) {
        final String methodName = getAccessorMethodName(Accessor.set, token);
        try {
            MethodUtils.invokeMethod(target, methodName, new Object[] { null }, new Class[] { type });
        } catch (Exception e) {
            die("Error calling "+methodName+": "+e);
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
        return die("Cannot figure out type parameterization for " + klass.getName());
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
