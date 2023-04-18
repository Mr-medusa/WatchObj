package red.medusa.watchobj.core;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    public static boolean isDebug = true;

    public static boolean isDebug() {
        return isDebug;
    }

    public static void setIsDebug(boolean isDebug) {
        Logger.isDebug = isDebug;
    }

    public static void debug(Object msg) {
        if(!isDebug()){
            return;
        }
        StackTraceElement stackTraceElement = new Throwable().getStackTrace()[1];
        String trace = stackTraceElement.toString();
        int lastIndex = trace.lastIndexOf('.');
        int secondIndex = trace.lastIndexOf('.', lastIndex - 1);
        int thirdIndex = trace.lastIndexOf('.', secondIndex - 1);
        String traceInfo = trace.substring(thirdIndex + 1);
        String sb = new SimpleDateFormat("mm分ss秒.S").format(new Date()) +
                " <" + Thread.currentThread().getName() + "> " +
                "|> " + msg + " <| " + traceInfo;
        System.err.println(sb);
    }


}
