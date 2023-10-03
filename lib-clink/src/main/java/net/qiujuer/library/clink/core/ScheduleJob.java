package net.qiujuer.library.clink.core;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class ScheduleJob implements Runnable{
   protected final long idleTimeoutMilliseconds;
   protected final ConnectorFinal connector;
   private volatile Scheduler scheduler;

   private volatile ScheduledFuture scheduledFuture;

    protected ScheduleJob(long idleTime, TimeUnit unit, ConnectorFinal connector) {
        this.idleTimeoutMilliseconds = unit.toMillis(idleTime);
        this.connector = connector;
    }

    synchronized void schedule(Scheduler scheduler){
        this.scheduler=scheduler;
        schedule(idleTimeoutMilliseconds);
    }

    synchronized void unSchedule(){
        if(scheduler!=null){
            scheduler=null;
        }

        if(scheduledFuture!=null){
            scheduledFuture.cancel(true);
            scheduledFuture=null;
        }
    }

    protected void schedule(long timeoutMilliseconds){
        if(scheduler!=null){
            scheduler.schedule(this,timeoutMilliseconds,TimeUnit.MILLISECONDS);
        }
    }
}
