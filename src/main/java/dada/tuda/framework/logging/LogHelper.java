package dada.tuda.framework.logging;

public class LogHelper {
    private LogHelper() {}
    public static String logException(Throwable e){
        if(e.getCause()!=null){
            return e.getCause().getLocalizedMessage();
        }
        return e.getLocalizedMessage();
    }
}
