// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.s7.eth;

public class S7Exception extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public S7Exception() {
    }
    
    public S7Exception(final String message) {
        super(message);
    }
    
    public S7Exception(final String message, final Throwable cause) {
        super(message, cause);
    }
    
    public S7Exception(final Throwable cause) {
        super(cause);
    }
}
