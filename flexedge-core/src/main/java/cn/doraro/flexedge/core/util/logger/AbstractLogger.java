package cn.doraro.flexedge.core.util.logger;

public abstract class AbstractLogger implements ILogger {

    int currentLogLevel = -1;

    int ctrl = CTRL_DEFAULT;

    public final int getCurrentLogLevel() {
        if (currentLogLevel < 0)
            return LoggerManager.getDefaultLogLevel();
        return this.currentLogLevel;
    }

    public final void setCurrentLogLevel(int loglvl) {
        if (loglvl == LoggerManager.getDefaultLogLevel()) {
            currentLogLevel = -1;
            return;
        }

        currentLogLevel = loglvl;
    }

    /**
     * @return
     */
    public final int getCtrl() {
        return ctrl;
    }

    /**
     * @param c
     */
    public final void setCtrl(int c) {
        ctrl = c;
    }

    public String getCtrlTitle() {
        switch (ctrl) {

            case CTRL_ENABLE:
                return "enable all";
            case CTRL_DISABLE:
                return "disable all";
            case CTRL_DEFAULT:
            default:
                return "level";
        }
    }

    protected boolean isLevelEnabled(int loglvl) {
//		if(ctrl<0)
//			return false;
//		
//		if(ctrl>0)
//			return true ;
        return (loglvl >= getCurrentLogLevel());
    }

    final public boolean isTraceEnabled() {
        return isLevelEnabled(LOG_LEVEL_TRACE);
    }

    final public boolean isDebugEnabled() {
        return isLevelEnabled(LOG_LEVEL_DEBUG);
    }

    final public boolean isInfoEnabled() {
        return isLevelEnabled(LOG_LEVEL_INFO);
    }

    final public boolean isWarnEnabled() {
        return isLevelEnabled(LOG_LEVEL_WARN);
    }

    final public boolean isErrorEnabled() {
        return isLevelEnabled(LOG_LEVEL_ERROR);
    }

//	public boolean isFatalEnabled()
//	{
//		if(ctrl<0)
//			return false;
//		
//		if(ctrl>0)
//			return true ;
//		
//		return LoggerManager.Is_Fatal;
//	}
}
