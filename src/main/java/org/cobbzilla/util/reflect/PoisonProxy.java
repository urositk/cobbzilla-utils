package org.cobbzilla.util.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.cobbzilla.util.daemon.ZillaRuntime.notSupported;

public class PoisonProxy {

    /**
     * Create a proxy object for a class where calling any methods on the object will result in it thowing an exception.
     * @param clazz The class to create a proxy for
     * @param <T> The class to create a proxy for
     * @return A proxy to the class that will throw an exception if any methods are called on it
     */
    public static <T> T wrap(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, PoisonedInvocationHandler.instance);
    }

    private static class PoisonedInvocationHandler implements InvocationHandler {
        public static PoisonedInvocationHandler instance = new PoisonedInvocationHandler();
        @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return notSupported("method not supported by poisonProxy: " + method.getName() + " (in fact, NO methods will work on this object)");
        }
    }

}
