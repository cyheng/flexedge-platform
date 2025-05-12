// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.omron.hostlink;

import cn.doraro.flexedge.core.DevAddr;
import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.util.Convert;

public class HLAddrSeg implements DevAddr.IAddrDefSeg {
    String title;
    int valStart;
    int valEnd;
    int digitNum;
    boolean bHasBit;
    UAVal.ValTP[] valTPs;
    boolean bWrite;
    transient HLAddrDef belongTo;

    HLAddrSeg(final String title, final int valstart, final int valend, final int digit_num, final UAVal.ValTP[] tps) {
        this.valStart = 0;
        this.valEnd = -1;
        this.digitNum = -1;
        this.bHasBit = false;
        this.valTPs = null;
        this.bWrite = true;
        this.title = title;
        this.valStart = valstart;
        this.valEnd = valend;
        this.digitNum = digit_num;
        this.valTPs = tps;
    }

    public HLAddrSeg asWrite(final boolean bw) {
        this.bWrite = bw;
        return this;
    }

    public HLAddrSeg asHasSubBit(final boolean b) {
        this.bHasBit = b;
        return this;
    }

    public boolean isValBitOnly() {
        return this.valTPs.length == 1 && this.valTPs[0] == UAVal.ValTP.vt_bool;
    }

    public String getTitle() {
        return this.title;
    }

    public int getValStart() {
        return this.valStart;
    }

    public int getValEnd() {
        return this.valEnd;
    }

    public boolean isHasBit() {
        return this.bHasBit;
    }

    public boolean isWritable() {
        return this.bWrite;
    }

    public int getDigitNum() {
        return this.digitNum;
    }

    public UAVal.ValTP[] getValTPs() {
        return this.valTPs;
    }

    public boolean matchValTP(final UAVal.ValTP vtp) {
        for (final UAVal.ValTP vt : this.valTPs) {
            if (vt == vtp) {
                return true;
            }
        }
        return vtp == UAVal.ValTP.vt_bool && this.bHasBit;
    }

    public boolean matchAddr(final int addr_num, final int bit_num) {
        try {
            if (bit_num >= 0) {
                if (!this.bHasBit) {
                    return false;
                }
                if (bit_num < 0 || bit_num > 15) {
                    return false;
                }
            }
            return addr_num >= this.valStart && addr_num <= this.valEnd;
        } catch (final Exception ee) {
            ee.printStackTrace();
            return false;
        }
    }

    public boolean matchAddr(final HLAddr addr) {
        final int addrn = addr.getAddrNum();
        return addrn >= this.valStart && addrn <= this.valEnd;
    }

    public String getRangeFrom() {
        return this.belongTo.prefix + Convert.toIntDigitsStr(this.valStart, this.digitNum);
    }

    public String getRangeTo() {
        return this.belongTo.prefix + Convert.toIntDigitsStr(this.valEnd, this.digitNum);
    }

    public String getSample() {
        return "";
    }
}
