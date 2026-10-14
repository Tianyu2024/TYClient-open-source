package cn.tianyu.client.util.impl;

import cn.tianyu.client.TYClient;
import net.minecraft.util.MathHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author CN_tianyu
 */
public final class Workers {

    public interface WorkerTask {
        void run();
    }

    private Workers() {

    }

    private static int getAvailableBackgroundThreads() {
        int maxCount = 255;
        try {
            maxCount = Integer.parseInt(System.getProperty("max.bg.threads"));
        } catch (NumberFormatException ignored) {}

        return MathHelper.clamp_int(Runtime.getRuntime().availableProcessors() - 1, 1, maxCount);
    }

    private static void uncaughtExceptionHandler(Thread t, Throwable e) {
        TYClient.LOGGER.error("Uncaught Exception in thread {}", t.getName(), e);
    }

    private static ExecutorService createWorker(String name) {
        final AtomicInteger id = new AtomicInteger(1);
        return new ForkJoinPool(getAvailableBackgroundThreads(), new ForkJoinPool.ForkJoinWorkerThreadFactory() {
            @Override
            public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
                final String threadName = "Worker-" + name + "-" + id.getAndIncrement();

                ForkJoinWorkerThread forkJoinWorkerThread = new ForkJoinWorkerThread(pool) {
                    @Override
                    protected void onTermination(Throwable throwable) {
                        if (throwable != null) {
                            TYClient.LOGGER.warn("{} died", threadName, throwable);
                        } else {
                            TYClient.LOGGER.debug("{} stopped", threadName);
                        }
                    }
                };

                forkJoinWorkerThread.setName(threadName);
                return forkJoinWorkerThread;
            }
        }, Workers::uncaughtExceptionHandler, true);
    }

    private static ExecutorService createIoWorker(String name) {
        final AtomicInteger id = new AtomicInteger(1);
        return Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable);
                thread.setName(name + "-" + id.getAndIncrement());
                thread.setDaemon(true);
                thread.setUncaughtExceptionHandler(Workers::uncaughtExceptionHandler);
                return thread;
            }
        });
    }

    @NotNull
    public static final ExecutorService Default = createWorker("Default");

    @NotNull
    public static final ExecutorService IO = createIoWorker("IO");

}