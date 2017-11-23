package com.hianzuo.spring.internal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池创建工具类
 * On 2017/10/18.
 *
 * @author Ryan
 */

public class ThreadFactoryUtil {
    public static ExecutorService createSingle(Class<?> clazz, boolean daemon, int priority) {
        return createSingle(clazz.getSimpleName(), daemon, priority);
    }

    public static ExecutorService createSingle(Class<?> clazz) {
        return createSingle(clazz.getSimpleName(), true, 1);
    }

    public static ExecutorService createSingle(String prefixName, boolean daemon, int priority) {
        return create(1, 1, 0L, TimeUnit.MILLISECONDS, prefixName, daemon, priority);
    }

    public static ExecutorService create(int corePoolSize, Class<?> clazz, boolean daemon, int priority) {
        return create(corePoolSize, clazz.getSimpleName(), daemon, priority);
    }

    public static ExecutorService create(int corePoolSize, String prefixName, boolean daemon, int priority) {
        return create(corePoolSize, corePoolSize, 0L, TimeUnit.MILLISECONDS, prefixName, daemon, priority);
    }

    public static ExecutorService create(int corePoolSize, int maxPoolSize, long keepAliveTime, TimeUnit unit, String prefixName, boolean daemon, int priority) {
        SimpleThreadFactory threadFactory = new SimpleThreadFactory(prefixName);
        threadFactory.setDaemon(daemon);
        threadFactory.setPriority(priority);
        return new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit,
                new LinkedBlockingQueue<Runnable>(1024), threadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    private static class SimpleThreadFactory implements ThreadFactory {
        private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        private boolean isDaemon = false;
        private int priority = 1;

        SimpleThreadFactory(String prefix) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "pool-" + prefix + "-" +
                    POOL_NUMBER.getAndIncrement() +
                    "-thread-";
        }

        public void setDaemon(boolean daemon) {
            isDaemon = daemon;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            t.setDaemon(isDaemon);
            t.setPriority(priority);
            return t;
        }
    }
}
