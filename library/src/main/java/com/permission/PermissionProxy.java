package com.permission;

import android.app.Activity;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * create date:  2018/8/13
 * creator:  luoyonghui
 * functional description:
 */
public final class PermissionProxy {
    private static final String TAG = "PermissionProxy";
    private static boolean debug = false;

    static final Map<Class<?>, Constructor<? extends UnBinder>> BIND_CACHE = new LinkedHashMap<Class<?>, Constructor<? extends UnBinder>>();

    public static void setDebug(boolean debug) {
        PermissionProxy.debug = debug;
    }

    public static UnBinder bind(Activity target) {
        return createBinding(target);
    }

    private static UnBinder createBinding(Object target) {
        Class<?> targetClass = target.getClass();
        if (debug) {
            Log.d(TAG, "bind class name for : " + targetClass.getName());
        }
        Constructor<? extends UnBinder> constructor = findBindingConstructorForClass(targetClass);
        if (constructor == null) {
            return UnBinder.EMPTY;
        }
        try {
            return constructor.newInstance(target);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return UnBinder.EMPTY;
    }

    private static Constructor<? extends UnBinder> findBindingConstructorForClass(Class<?> clazz) {
        Constructor<? extends UnBinder> bindingConstructor = BIND_CACHE.get(clazz);
        if (bindingConstructor != null) {
            if (debug) {
                Log.d(TAG, "get constructor from cache");
            }
            return bindingConstructor;
        }

        String clazzName = clazz.getName();
        if (clazzName.startsWith("android.") || clazzName.startsWith("java.")) {
            if (debug) {
                Log.d(TAG, "it is framework class, return null");
            }
            return null;
        }

        try {
            Class<?> bindingClass = Class.forName(clazzName + "_PermissionsDispatcher");
            bindingConstructor = (Constructor<? extends UnBinder>) bindingClass.getConstructor();
            if (debug) {
                Log.d(TAG, "Loaded binding class and constructor finish");
            }
        } catch (ClassNotFoundException e) {
            if (debug) {
                Log.d(TAG, "Exception : class not found");
                e.printStackTrace();
            }
        } catch (NoSuchMethodException e) {
            if (debug) {
                Log.d(TAG, "Exception : no such method");
                e.printStackTrace();
            }
        }

        BIND_CACHE.put(clazz, bindingConstructor);
        return bindingConstructor;
    }

}
