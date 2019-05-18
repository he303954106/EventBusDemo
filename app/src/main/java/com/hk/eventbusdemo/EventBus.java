package com.hk.eventbusdemo;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by hk on 2019/5/18.
 */
public class EventBus {

    //key是Activity或者Fragment
    //value是其中的方法集合
    private Map<Object, List<SubscribeMethod>> cacheMap;

    private Handler mHandler;

    private EventBus() {
        cacheMap = new HashMap<>();
        mHandler = new Handler();
    }

    public static EventBus getInstance() {
        return EventBusHolder.INSTANCE;
    }

    private static class EventBusHolder {
        public static final EventBus INSTANCE = new EventBus();
    }

    /**
     * 在map中加入注解方法
     *
     * @param obj Activity或者Fragment
     */
    public void register(Object obj) {
        List<SubscribeMethod> list = cacheMap.get(obj);
        if (list == null) {
            list = findSubscribeMethods(obj);
            cacheMap.put(obj, list);
        }
    }

    private List<SubscribeMethod> findSubscribeMethods(Object obj) {
        List<SubscribeMethod> list = new ArrayList<>();
        Class<?> clazz = obj.getClass();
        Method[] methods = clazz.getDeclaredMethods();

        while (clazz != null) {
            String name = clazz.getName();
            if (name.startsWith("java.")
                    || name.startsWith("javax.")
                    || name.startsWith("android.")) {
                break;
            }
            for (Method method : methods) {
                Subscribe subscribe = method.getAnnotation(Subscribe.class);
                if (subscribe == null) {
                    continue;
                }
                Class<?>[] types = method.getParameterTypes();
                if (types.length != 1) {
                    Log.e("错误", "eventbus only accept one param");
                }
                ThreadMode threadMode = subscribe.value();
                SubscribeMethod subscribeMethod = new SubscribeMethod(method, threadMode, types[0]);
                list.add(subscribeMethod);
            }

            //查找父类中有没有注解方法
            clazz = clazz.getSuperclass();
        }
        return list;
    }

    public void send(final Object type) {
        Iterator<Object> iterator = cacheMap.keySet().iterator();
        while (iterator.hasNext()) {
            final Object obj = iterator.next();
            List<SubscribeMethod> list = cacheMap.get(obj);//方法集合
            if (list == null || list.isEmpty()) {
                return;
            }
            for (final SubscribeMethod subscribeMethod : list) {
                //判断方法中接收的参数是否和发送的参数类型一致,然后调用
                if (subscribeMethod.getType().isAssignableFrom(type.getClass())) {
                    ThreadMode threadMode = subscribeMethod.getThreadMode();
                    switch (threadMode) {
                        case MAIN:
                            if (Looper.myLooper() == Looper.getMainLooper()) {
                                invoke(subscribeMethod, obj, type);
                            } else {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        invoke(subscribeMethod, obj, type);
                                    }
                                });
                            }
                            break;
                        case BACKGROUND:
                            break;
                    }
                }
            }
        }
    }

    private void invoke(SubscribeMethod subscribeMethod, Object obj, Object type) {
        Method method = subscribeMethod.getMethod();
        try {
            method.invoke(obj, type);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
