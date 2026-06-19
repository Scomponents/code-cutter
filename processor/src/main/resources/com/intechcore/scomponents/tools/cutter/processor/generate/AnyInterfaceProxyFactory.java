package com.intechcore.scomponents.tools.cutter.processor.generate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * A factory class for creating dynamic proxy instances of interfaces with default return values.
 * This class is used by the CodeGenerator of the Intechcore's Code Cutter
 * to generate default return values for interface types when a method annotated with
 * {@link com.intechcore.scomponents.tools.cutter.annotations.CutCode} is processed.
 *
 * The factory uses a java.lang.reflect.Proxy with a default invocation handler that
 * returns appropriate default values for primitive wrapper types, primitive types, and recursively
 * creates proxies for nested interface types.
 *
 * <p>This class is loaded as a resource and its class name is modified at runtime to create
 * package-specific proxy factories to avoid class loading conflicts.</p>
 */
public class AnyInterfaceProxyFactory {
    /**
     * A map of default values for wrapper and primitive types.
     * Key is the Class object, value is the default instance.
     */
    private static final Map<Class<?>, Object> WRAPPER_DEFAULTS = new HashMap<>();

    static {
        WRAPPER_DEFAULTS.put(Boolean.class,   false);
        WRAPPER_DEFAULTS.put(Byte.class,      (byte) 0);
        WRAPPER_DEFAULTS.put(Short.class,     (short) 0);
        WRAPPER_DEFAULTS.put(Integer.class,   0);
        WRAPPER_DEFAULTS.put(Long.class,      0L);
        WRAPPER_DEFAULTS.put(Character.class, '\0');
        WRAPPER_DEFAULTS.put(Float.class,     0.0f);
        WRAPPER_DEFAULTS.put(Double.class,    0.0d);

        WRAPPER_DEFAULTS.put(boolean.class,   false);
        WRAPPER_DEFAULTS.put(byte.class,      (byte) 0);
        WRAPPER_DEFAULTS.put(short.class,     (short) 0);
        WRAPPER_DEFAULTS.put(int.class,       0);
        WRAPPER_DEFAULTS.put(long.class,      0L);
        WRAPPER_DEFAULTS.put(char.class,      '\0');
        WRAPPER_DEFAULTS.put(float.class,     0.0f);
        WRAPPER_DEFAULTS.put(double.class,    0.0d);
    }

    /**
     * Creates a dynamic proxy instance for the given interface type.
     * The proxy will return default values for all method invocations.
     *
     * @param <T>    The type of the interface.
     * @param iface  The interface class for which to create a proxy.
     * @return A proxy instance implementing the given interface.
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> iface) {
        return (T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class<?>[]{iface},
                new DefaultHandler()
        );
    }

    /**
     * Default invocation handler that returns appropriate default values
     * based on the method's return type.
     */
    private static class DefaultHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Class<?> returnType = method.getReturnType();
            return defaultValue(returnType);
        }
    }

    /**
     * Returns a default value for the given type.
     * For primitive and wrapper types, returns the standard default (0, false, etc.).
     * For interface types, recursively creates a proxy instance.
     * For all other types, returns {@code null}.
     *
     * @param <T>   The type for which to get a default value.
     * @param type  The class representing the type.
     * @return A default value for the type, or {@code null} if not a known type.
     */
    @SuppressWarnings("unchecked")
    public static <T> T defaultValue(Class<T> type) {
        Object val = WRAPPER_DEFAULTS.get(type);
        if (val != null) {
            return (T) val;
        }
        if (type.isInterface()) {
            return create(type);
        }
        return null;
    }
}
