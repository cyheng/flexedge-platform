// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.omron.hostlink;

public class HLException extends Exception
{
    public static final int ERR_TIMEOUT_NOR = 1;
    public static final int ERR_TIMEOUT_SERIOUS = 2;
    int errCode;
    
    public HLException(final int err_c, final String msg) {
        super(msg);
        this.errCode = -1;
        this.errCode = err_c;
    }
    
    public int getErrCode() {
        return this.errCode;
    }
}
