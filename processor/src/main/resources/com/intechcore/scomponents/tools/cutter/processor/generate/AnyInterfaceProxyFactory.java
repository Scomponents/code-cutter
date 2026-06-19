package com.intechcore.scomponents.tools.cutter.processor.generate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class AnyInterfaceProxyFactory {
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

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> iface) {
        return (T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class<?>[]{iface},
                new DefaultHandler()
        );
    }

    private static class DefaultHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Class<?> returnType = method.getReturnType();
            return defaultValue(returnType);
        }
    }

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
