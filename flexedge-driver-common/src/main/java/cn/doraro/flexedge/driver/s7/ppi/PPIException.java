// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.s7.ppi;

public class PPIException extends Exception
{
    private static final long serialVersionUID = 7350994271896884309L;
    
    public PPIException() {
    }
    
    public PPIException(final String message) {
        super(message);
    }
    
    public PPIException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
    public PPIException(final Throwable cause) {
        super(cause);
    }
    
    protected PPIException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
