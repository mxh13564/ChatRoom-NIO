import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.IntFunction;

public class StealingService {

    private final int minSafetyThreshold;

    private final StealingSelectorThread[] threads;

    private final LinkedBlockingQueue<IoTask>[] queues;

    protected volatile boolean isTerminated = false;

    public StealingService(StealingSelectorThread[] threads, int minSafetyThreshold) {
        this.minSafetyThreshold = minSafetyThreshold;
        this.threads = threads;
        //你可以将线程数组threads中的每个线程的getReadyTaskQueue方法返回的队列转换为一个LinkedBlockingQueue<IoTask>数组。
        this.queues = Arrays.stream(threads).map(StealingSelectorThread::getReadyTaskQueue).toArray((IntFunction<LinkedBlockingQueue<IoTask>[]>) LinkedBlockingQueue[]::new);
    }

    IoTask steal(final LinkedBlockingQueue<IoTask> excludedQueue){
        final int minSafetyThreshold =this.minSafetyThreshold;
        final LinkedBlockingQueue<IoTask>[] queues = this.queues;
        for(LinkedBlockingQueue<IoTask> queue:queues){
            if(queue==excludedQueue){
                continue;
            }

            int size=queue.size();
            if(size>minSafetyThreshold){
                IoTask poll=queue.poll();
                if(poll!=null){
                    return poll;
                }
            }
        }
        return null;
    }


    public StealingSelectorThread getNotBusyThread(){
        StealingSelectorThread targetThread=null;
        long targetKeyCount=Long.MAX_VALUE;
        for(StealingSelectorThread thread : threads){
            long registerKeyCount=thread.getStaturatingCapacity();
            if(registerKeyCount!=-1 && registerKeyCount<targetKeyCount){
                targetKeyCount=registerKeyCount;
                targetThread=thread;
            }
        }
        return targetThread;
    }


    public void shutdown(){
        if(isTerminated){
            return ;
        }
        isTerminated = true;
        for (StealingSelectorThread thread : threads){
            thread.exit();
        }
    }

    public void execute(IoTask task){};

    public boolean isTerminated() {return isTerminated;}
}
