

package cn.doraro.flexedge.driver.nbiot.msg;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class WMMsgValveReq extends WMMsg {
    boolean bOpen;

    public WMMsgValveReq() {
        this.bOpen = true;
        final byte[] F = {2, 2};
        this.setMsgFunc(F);
    }

    public void setValveOpen(final boolean b) {
        this.bOpen = b;
    }

    @Override
    protected ArrayList<byte[]> parseMsgBody(final InputStream inputs) throws IOException {
        return null;
    }

    @Override
    protected ArrayList<byte[]> getMsgBody() {
        final ArrayList<byte[]> bbs = super.getMsgBody();
        if (this.bOpen) {
            bbs.add(new byte[]{85});
        } else {
            bbs.add(new byte[]{-86});
        }
        return bbs;
    }
}
