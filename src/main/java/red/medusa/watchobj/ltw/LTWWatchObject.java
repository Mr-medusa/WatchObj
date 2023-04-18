package red.medusa.watchobj.ltw;


import java.lang.instrument.Instrumentation;

/**
 * -javaagent:G:\HWorkspace\WatchObj\target\WatchObj-1.0.jar
 *
 * @see red.medusa.watchobj.aspectj.WatchObjWithFieldProcessAspect
 */
public class LTWWatchObject {
    public static void premain(String args, Instrumentation inst) {
        inst.addTransformer(new WatchObjectTransformer( args));
    }
}
