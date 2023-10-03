package net.qiujuer.library.clink.impl;

import net.qiujuer.library.clink.core.Scheduler;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class SchedulerImpl implements Scheduler {
    private final ScheduledExecutorService scheduledExecutorService;

    public SchedulerImpl(int poolSize) {
        this.scheduledExecutorService = Executors.newScheduledThreadPool(poolSize,new IoSelectorProvider.IoProviderThreadFactory("Scheduler-Thread-"));
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit unit) {
        return scheduledExecutorService.schedule(runnable,delay,unit);
    }

    @Override
    public void close() throws IOException {
        scheduledExecutorService.shutdownNow();
    }
}
