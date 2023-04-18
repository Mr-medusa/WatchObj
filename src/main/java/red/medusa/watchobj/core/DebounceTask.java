package red.medusa.watchobj.core;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class DebounceTask<T> {
    private Timer timer;
    private final Integer delay;
    private final Callable<T> callable;
    private final Consumer<T> consumer;
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
