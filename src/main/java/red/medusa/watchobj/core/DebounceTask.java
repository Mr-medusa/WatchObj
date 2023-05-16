package red.medusa.watchobj.core;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * 连续及其非常频繁的实例初始化或字段值更新触发
 * PropertyValue 值变化或构建过程防抖任务
 *
 * @param <T> 泛型 Callable 返回值类型，返回 List<CollectionUpdateData>
 * @author GHHu
 * @date 2023/5/16
 * @see CollectionUpdateData
 */
public class DebounceTask<T> {
    private Timer timer;
    // 延迟毫秒数
    private final Integer delay;
    // 初始化实例/字段值更新触发任务
    private final Callable<T> callable;
    // 结束初始化实例/字段值更新后触发任务
    private final Consumer<T> consumer;
    // 校验任务集是否为空
    private final CollectionEmptyPredicate predicate;

    public DebounceTask(Callable<T> callable, Integer delay, CollectionEmptyPredicate predicate, Consumer<T> consumer) {
        this.callable = callable;
        this.delay = delay;
        this.predicate = predicate;
        this.consumer = consumer;
    }


    public static <T> DebounceTask<T> build(Callable<T> callable, Integer delay, CollectionEmptyPredicate predicate, Consumer<T> consumer) {
        return new DebounceTask<>(callable, delay, predicate, consumer);
    }

    public void timerRun() {
        if (predicate != null && predicate.isEmpty()) {
            return;
        }
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    T call = callable.call();
                    if (consumer != null) {
                        consumer.accept(call);
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                timer.cancel();
                timer = null;
            }
        }, delay);
    }
}
