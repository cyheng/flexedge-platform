// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.omron.fins;

import java.io.IOException;
import java.io.InputStream;
import cn.doraro.flexedge.core.util.IBSOutput;

public class FinsCmdResp extends FinsCmd
{
    int errorCode;
    byte[] respVals;
    
    public FinsCmdResp(final FinsMode fins_mode) {
        super(fins_mode);
        this.errorCode = -1;
        this.respVals = null;
    }
    
    @Override
    protected short getMRC() {
        return 1;
    }
    
    @Override
    protected short getSRC() {
        return 1;
    }
    
    @Override
    protected short getICF() {
        return 192;
    }
    
    @Override
    protected int getParamBytesNum() {
        return 0;
    }
    
    @Override
    protected void writeParam(final IBSOutput outputs) {
    }
    
    public static FinsCmdResp readFromStream(final InputStream inputs, final long timeout) throws IOException {
        return null;
    }
}
