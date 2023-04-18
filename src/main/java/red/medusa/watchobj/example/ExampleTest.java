package red.medusa.watchobj.example;


import red.medusa.watchobj.example.bean.GenericListTestBean;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class ExampleTest extends AutoUpdateValueBaseTest {
    public static Set<Object> cache = new HashSet<>();

    // -javaagent:G:/HWorkspace/WatchObj/target/WatchObj-1.0.jar
    public static void main(String[] args) {
        GenericListTestBean<Integer> listIntegerTestBean
                = new GenericListTestBean<>(0);

        cache.add(listIntegerTestBean);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                autoUpdateValue(listIntegerTestBean, false);
            }
        }, 5000L);
    }
}
