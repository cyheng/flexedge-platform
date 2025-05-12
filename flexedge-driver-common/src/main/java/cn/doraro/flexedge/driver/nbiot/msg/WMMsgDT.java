// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.nbiot.msg;

import cn.doraro.flexedge.core.util.Convert;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public abstract class WMMsgDT extends WMMsg {
    byte[] msgDT;

    public WMMsgDT() {
        this.msgDT = null;
    }

    public byte[] getMsgDT() {
        return this.msgDT;
    }

    public void setMsgDT(final byte[] msgdt) {
        if (msgdt.length != 6) {
            throw new IllegalArgumentException("invalid dt info");
        }
        this.msgDT = msgdt;
    }

    public void setMsgDT(final Date dt) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        final int y = cal.get(1) - 2000;
        final int m = cal.get(2) + 1;
        final int d = cal.get(5);
        final int h = cal.get(11);
        final int min = cal.get(12);
        final int s = cal.get(13);
        final byte[] bs = {this.int2bcd(y), this.int2bcd(m), this.int2bcd(d), this.int2bcd(h), this.int2bcd(min), this.int2bcd(s)};
        this.msgDT = bs;
    }

    public Date getMsgDTDate() {
        if (this.msgDT == null) {
            return null;
        }
        final int y = 2000 + this.bcd2int(this.msgDT[0]);
        final int m = this.bcd2int(this.msgDT[1]);
        final int d = this.bcd2int(this.msgDT[2]);
        final int h = this.bcd2int(this.msgDT[3]);
        final int min = this.bcd2int(this.msgDT[4]);
        final int s = this.bcd2int(this.msgDT[5]);
        final Calendar cal = Calendar.getInstance();
        cal.set(1, y);
        cal.set(2, m - 1);
        cal.set(5, d);
        cal.set(11, h);
        cal.set(12, min);
        cal.set(13, s);
        return cal.getTime();
    }

    @Override
    protected ArrayList<byte[]> getMsgBody() {
        final ArrayList<byte[]> bbs = super.getMsgBody();
        bbs.add(this.msgDT);
        return bbs;
    }

    @Override
    protected ArrayList<byte[]> parseMsgBody(final InputStream inputs) throws IOException {
        if (inputs.available() < 6) {
            return null;
        }
        inputs.read(this.msgDT = new byte[6]);
        final ArrayList<byte[]> bbs = new ArrayList<byte[]>();
        bbs.add(this.msgDT);
        return bbs;
    }

    @Override
    public String toString() {
        String ret = super.toString();
        ret = ret + " dt=" + Convert.toFullYMDHMS(this.getMsgDTDate());
        return ret;
    }
}
