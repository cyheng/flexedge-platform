package cn.doraro.flexedge.core.util.logger;


public class LoggerObj extends AbstractLogger {
    String logId = null;


    boolean b_trace = false;


    //ILogDo logDo = new ConsoleLogDo() ;

    public LoggerObj(String id) {
        logId = id;
    }

    public String getLoggerId() {
        return logId;
    }

    public boolean isStackTrace() {
        return b_trace;
    }

    /**
     * ����debug����ʱ���Ƿ��ӡ���ö�ջ
     */
    public void setStackTrace(boolean b) {
        b_trace = b;
    }

    /**
     * ����System.out.println���������ܿ�������
     *
     * @param msg
     */
    public void println(String msg) {
        LoggerManager.getLogDo().print(msg);
        LoggerManager.getLogDo().print("\n");
    }

    public void print(String msg) {
        LoggerManager.getLogDo().print(msg);
    }

    public void printException(Exception ex) {
        LoggerManager.getLogDo().printException(ex);
    }
//	public void setLogDo(ILogDo ld)
//	{
//		logDo = ld ;
//	}

    public void fatal(String msg) {
        if (ctrl < 0)
            return;

        if (ctrl == 0 && !LoggerManager.Is_Fatal)
            return;

        LoggerManager.getLogDo().fatal(msg);
    }

    public void fatal(Throwable t) {
        if (ctrl < 0)
            return;

        if (ctrl == 0 && !LoggerManager.Is_Fatal)
            return;

        LoggerManager.getLogDo().fatal(t);
    }

    public void error(String msg) {
        if (ctrl < 0)
            return;

        if (ctrl == 0 && !LoggerManager.Is_Error)
            return;

        LoggerManager.getLogDo().error(msg);
    }

    public void error(String msg, Throwable t) {
        if (ctrl < 0)
            return;

        if (ctrl == 0 && !LoggerManager.Is_Error)
            return;

        LoggerManager.getLogDo().error(msg, t);
    }

    public void warn(String msg) {
        if (ctrl < 0)
            return;

        if (ctrl == 0 && !LoggerManager.Is_Warn)
            return;

        LoggerManager.getLogDo().warn(msg);
    }

    public void warn(String msg, Throwable t) {
        if (ctrl < 0)
            return;

        if (ctrl == 0 && !LoggerManager.Is_Warn)
            return;

        LoggerManager.getLogDo().warn(msg, t);
    }

    public void info(String msg) {
        if (ctrl < 0)
            return;

        if (ctrl == 0 && !LoggerManager.Is_Info)
            return;

        LoggerManager.getLogDo().info(msg);
    }

    public void info(String msg, Throwable t) {
        if (ctrl < 0)
            return;

        if (ctrl == 0 && !LoggerManager.Is_Info)
            return;

        LoggerManager.getLogDo().info(msg, t);
    }

    public void debug(String msg) {
        if (!LoggerManager.thLogOpen.get()) {
            if (ctrl < 0)
                return;

            if (ctrl == 0 && !LoggerManager.Is_Debug)
                return;
        }

        ILogDo ld = LoggerManager.getLogDo();
        if (b_trace) {
            for (StackTraceElement st : Thread.currentThread().getStackTrace()) {
                ld.debug(st.toString());
            }
        }

        ld.debug(msg);
    }

    public void debug(String msg, Throwable t) {
        if (!LoggerManager.thLogOpen.get()) {
            if (ctrl < 0)
                return;

            if (ctrl == 0 && !LoggerManager.Is_Debug)
                return;
        }

        LoggerManager.getLogDo().debug(msg, t);
    }

    public void trace(String msg) {
        if (ctrl < 0)
            return;

        if (ctrl == 0 && !LoggerManager.Is_Trace)
            return;

        LoggerManager.getLogDo().trace(msg);
    }

    public void trace(String msg, Throwable t) {
        if (ctrl < 0)
            return;

        if (ctrl == 0 && !LoggerManager.Is_Trace)
            return;

        LoggerManager.getLogDo().trace(msg, t);
    }


}
