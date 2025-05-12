// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.nbiot.msg;

import java.io.IOException;
import java.util.ArrayList;
import java.io.InputStream;

public class WMMsgValveResp extends WMMsg
{
    boolean bOpen;
    
    public WMMsgValveResp() {
        this.bOpen = true;
        final byte[] F = { -126, 2 };
        this.setMsgFunc(F);
    }
    
    public boolean isValveOpen() {
        return this.bOpen;
    }
    
    @Override
    protected ArrayList<byte[]> parseMsgBody(final InputStream inputs) throws IOException {
        if (inputs.available() < 1) {
            return null;
        }
        final byte[] bs = { 0 };
        inputs.read(bs);
        final ArrayList<byte[]> bbs = new ArrayList<byte[]>();
        bbs.add(bs);
        final int v = bs[0] & 0xFF;
        if (v == 85) {
            this.bOpen = true;
        }
        else {
            if (v != 170) {
                throw new IOException("invalid valve status value");
            }
            this.bOpen = false;
        }
        return bbs;
    }
    
    @Override
    public String toString() {
        return super.toString() + " valve_open=" + this.bOpen;
    }
}
