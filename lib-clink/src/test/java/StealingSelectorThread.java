import net.qiujuer.library.clink.core.IoProvider;
import net.qiujuer.library.clink.utils.CloseUtils;

import java.io.IOException;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public abstract class StealingSelectorThread extends Thread{
    private static final int VALID_OPS= SelectionKey.OP_READ | SelectionKey.OP_WRITE;
    private final Selector selector;
    //是否还处于运行中
    private volatile boolean isRunning=true;
    //已就绪任务队列
    private final LinkedBlockingQueue<IoTask> readyTaskQueue = new LinkedBlockingQueue<>();
    //待注册的任务队列
    private final LinkedBlockingQueue<IoTask> registerTaskQueue = new LinkedBlockingQueue<>();
    //单次就绪的任务缓存，随后一次性加入到就绪队列中
    private final List<IoTask> onceReadyTaskCache = new ArrayList<>(200);
    //任务饱和度度量
    private final AtomicLong staturatingCapacity=new AtomicLong();
    //用于多线程协同的Service
    private volatile StealingService stealingService;

    public void setStealingService(StealingService stealingService){
        this.stealingService=stealingService;
    }

    protected StealingSelectorThread(Selector selector) {
        this.selector = selector;
    }

    public void unregister(SocketChannel channel) {
        SelectionKey selectionKey = channel.keyFor(selector);
        if(selectionKey!=null && selectionKey.attachment()!=null){
            selectionKey.attach(null);
            IoTask ioTask=new IoTask(channel,0,null);
            registerTaskQueue.offer(ioTask);
        }
    }

    public boolean register(SocketChannel channel, int ops, IoProvider.HandleProviderCallback callback) {
       if(channel.isOpen()){
           IoTask ioTask=new IoTask(channel,ops,callback);
           registerTaskQueue.offer(ioTask);
           return true;
       }else{
           return false;
       }
    }

    private void joinTaskQueue(final LinkedBlockingQueue<IoTask> readyTaskQueue, final List<IoTask> onceReadyTaskCache){
        readyTaskQueue.addAll(onceReadyTaskCache);
    }

    protected abstract boolean processTask(IoTask task);

    @Override
    public final void run(){
        super.run();
        final Selector selector=this.selector;
        final LinkedBlockingQueue<IoTask> readyTaskQueue = this.readyTaskQueue;
        final LinkedBlockingQueue<IoTask> registerTaskQueue = this.registerTaskQueue;
        final List<IoTask> onceReadyTaskCache = this.onceReadyTaskCache;
        try{
            while (isRunning){
                consumeRegisterTodoTasks(registerTaskQueue);

                if((selector.selectNow()) == 0){
                    Thread.yield();
                    continue;
                }

                //处理已就绪的通道
                Set<SelectionKey> selectionKeys=selector.selectedKeys();
                Iterator<SelectionKey> iterator=selectionKeys.iterator();

                while(iterator.hasNext()){
                    SelectionKey selectionKey=iterator.next();
                    Object attachmentObj=selectionKey.attachment();
                    //检查有效性
                    if(selectionKey.isValid() && attachmentObj instanceof KeyAttachment){
                        final KeyAttachment attachment=(KeyAttachment) attachmentObj;
                        try{
                            final int readyops = selectionKey.readyOps();
                            int interestOps = selectionKey.interestOps();

                            //是否可读
                            if((readyops & SelectionKey.OP_READ) !=0){
                                onceReadyTaskCache.add(attachment.taskForReadable);
                                interestOps=interestOps & ~SelectionKey.OP_READ;
                            }

                            //是否可写
                            if((readyops & SelectionKey.OP_WRITE) !=0){
                                onceReadyTaskCache.add(attachment.taskForWritable);
                                interestOps=interestOps & ~SelectionKey.OP_WRITE;
                            }
                            //取消已就绪关注
                            selectionKey.interestOps(interestOps);
                        }catch (CancelledKeyException ignored){
                                onceReadyTaskCache.remove(attachment.taskForReadable);
                                onceReadyTaskCache.remove(attachment.taskForWritable);
                        }
                    }
                    iterator.remove();
                }
                 //判断本次是否有待执行的任务
                if(!onceReadyTaskCache.isEmpty()){
                    //加入到总队列中
                    joinTaskQueue(readyTaskQueue,onceReadyTaskCache);
                    onceReadyTaskCache.clear();
                }
                //消费总队列中的任务
                consumeTodoTasks(readyTaskQueue,registerTaskQueue);
            }
        }catch (ClosedSelectorException ignored){
        }catch (IOException e){
            CloseUtils.close(selector);
        }finally {
            readyTaskQueue.clear();
            registerTaskQueue.clear();
            onceReadyTaskCache.clear();
        }
    }

    private void consumeTodoTasks(LinkedBlockingQueue<IoTask> readyTaskQueue, LinkedBlockingQueue<IoTask> registerTaskQueue) {
        //循环把所有的任务做完
        IoTask doTask=readyTaskQueue.poll();
        while (doTask!=null){
            staturatingCapacity.incrementAndGet();
            //做任务
            if(processTask(doTask)){
                //做完工作后添加待注册的列表
                registerTaskQueue.offer(doTask);
            }
            //下个任务
            doTask=readyTaskQueue.poll();
        }
        
        //窃取其他任务
        final StealingService stealingService=this.stealingService;
        if(stealingService!=null){
            doTask=stealingService.steal(readyTaskQueue);
            while(doTask!=null){
                staturatingCapacity.incrementAndGet();
                if(processTask(doTask)){
                    registerTaskQueue.offer(doTask);
                }
                doTask = stealingService.steal(readyTaskQueue);
            }
        }
    }

    private void consumeRegisterTodoTasks(LinkedBlockingQueue<IoTask> registerTaskQueuee) {
        final Selector selector = this.selector;
        IoTask registerTask = registerTaskQueuee.poll();
        while (registerTask!=null){
            try{
                final SocketChannel channel=registerTask.channel;
                int ops=registerTask.ops;
                if(ops==0){
                    SelectionKey key=channel.keyFor(selector);
                    if(key!=null){
                        key.cancel();
                    }
                }else if((ops & ~VALID_OPS)==0){
                    SelectionKey key = channel.keyFor(selector);
                    if(key==null){
                        key = channel.register(selector,ops, new KeyAttachment());
                    }else{
                        //这段代码是用于设置感兴趣的操作位（interestOps）的。它使用位运算（bitwise OR）将现有的操作位与新的操作位进行合并。具体来说，`key.interestOps()`表示获取当前的操作位，`ops`表示新的操作位，`|`表示位运算的按位或操作。经过这个操作后，感兴趣的操作位将包括之前的操作位以及新的操作位。
                        key.interestOps(key.interestOps() | ops);
                    }

                    Object attachment=key.attachment();
                    if(attachment instanceof KeyAttachment){
                        ((KeyAttachment) attachment).attach(ops,registerTask);
                    }else{
                        key.cancel();
                    }
                }
            }catch (ClosedChannelException |
                    CancelledKeyException |
                    ClosedSelectorException ignored){
            }finally {
                registerTask = registerTaskQueuee.poll();
            }
        }
    }

    public void exit(){
        isRunning=false;
        CloseUtils.close(selector);
        interrupt();
    }

    LinkedBlockingQueue<IoTask> getReadyTaskQueue() {
        return readyTaskQueue;
    }

    public long getStaturatingCapacity() {
        if(selector.isOpen()){
            return staturatingCapacity.get();
        }else {
            return -1;
        }
    }

    static class KeyAttachment{
        IoTask taskForReadable;
        IoTask taskForWritable;

        void attach(int ops, IoTask task){
            if(ops==SelectionKey.OP_READ){
                taskForReadable=task;
            }else{
                taskForWritable=task;
            }
        }
    }
}
