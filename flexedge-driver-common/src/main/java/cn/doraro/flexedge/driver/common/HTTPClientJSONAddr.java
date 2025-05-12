// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common;

import java.util.List;
import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.UADev;
import cn.doraro.flexedge.core.DevAddr;

public class HTTPClientJSONAddr extends DevAddr
{
    public DevAddr parseAddr(final UADev dev, final String str, final UAVal.ValTP vtp, final StringBuilder failedr) {
        return null;
    }
    
    public boolean isSupportGuessAddr() {
        return false;
    }
    
    public DevAddr guessAddr(final UADev dev, final String str, final UAVal.ValTP vtp) {
        return null;
    }
    
    public List<String> listAddrHelpers() {
        return null;
    }
    
    public UAVal.ValTP[] getSupportValTPs() {
        return null;
    }
    
    public boolean canRead() {
        return true;
    }
    
    public boolean canWrite() {
        return false;
    }
    
    public String toCheckAdjStr() {
        return null;
    }
}
