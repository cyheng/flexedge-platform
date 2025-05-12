// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.mitsubishi.fx;

import cn.doraro.flexedge.core.DevAddr;
import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.util.Convert;

public class FxAddrSeg implements DevAddr.IAddrDefSeg {
    String title;
    int valStart;
    int valEnd;
    int digitNum;
    boolean bValBit;
    boolean bAddrStepInt32;
    UAVal.ValTP[] valTPs;
    boolean bOctal;
    boolean bWrite;
    int baseAddr;
    int baseValStart;
    int baseAddrForceOnOff;
    int extBaseValStart;
    int extBaseAddrForceOnOff;
    transient FxAddrDef belongTo;

    FxAddrSeg(final int baseaddr, final String title, final int valstart, final int valend, final int digit_num, final UAVal.ValTP[] tps, final boolean b_write) {
        this.valStart = 0;
        this.valEnd = -1;
        this.digitNum = -1;
        this.bValBit = false;
        this.bAddrStepInt32 = false;
        this.valTPs = null;
        this.bOctal = false;
        this.bWrite = true;
        this.baseValStart = 0;
        this.baseAddrForceOnOff = -1;
        this.extBaseValStart = -1;
        this.extBaseAddrForceOnOff = -1;
        this.baseAddr = baseaddr;
        this.title = title;
        this.valStart = valstart;
        this.valEnd = valend;
        this.digitNum = digit_num;
        this.valTPs = tps;
        this.bWrite = b_write;
    }

    public FxAddrSeg asBaseValStart(final int v) {
        this.baseValStart = v;
        return this;
    }

    public FxAddrSeg EXT_asBaseValStart(final int v) {
        this.extBaseValStart = v;
        return this;
    }

    public FxAddrSeg EXT_asBaseAddrForceOnOff(final int v) {
        this.extBaseAddrForceOnOff = v;
        return this;
    }

    public boolean isExtCmd() {
        return this.extBaseValStart >= 0 || this.extBaseAddrForceOnOff >= 0;
    }

    public FxAddrSeg asBaseAddrForceOnOff(final int addr) {
        this.baseAddrForceOnOff = addr;
        return this;
    }

    public FxAddrSeg asOctal(final boolean b) {
        this.bOctal = b;
        return this;
    }

    public FxAddrSeg asValBit(final boolean b) {
        this.bValBit = b;
        return this;
    }

    public FxAddrSeg asAddrStepInt32(final boolean b) {
        this.bAddrStepInt32 = b;
        return this;
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

    public boolean isValBit() {
        return this.bValBit;
    }

    public boolean isWritable() {
        return this.bWrite;
    }

    public int getDigitNum() {
        return this.digitNum;
    }

    public int getBaseAddr() {
        return this.baseAddr;
    }

    public int calBytesInBase(final int val) {
        if (this.bValBit) {
            return (val - this.baseValStart) / 8;
        }
        if (this.bAddrStepInt32) {
            return (val - this.baseValStart) * 4;
        }
        return (val - this.baseValStart) * 2;
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
        return false;
    }

    public Integer matchAddr(final String addr_num) {
        int addrn;
        if (this.bOctal) {
            addrn = Integer.parseInt(addr_num, 8);
        } else {
            addrn = Integer.parseInt(addr_num);
        }
        if (addrn >= this.valStart && addrn <= this.valEnd) {
            return addrn;
        }
        return null;
    }

    public boolean matchAddr(final FxAddr addr) {
        final int addrn = addr.getAddrNum();
        return addrn >= this.valStart && addrn <= this.valEnd;
    }

    public String getRangeFrom() {
        if (this.bOctal) {
            return this.belongTo.prefix + Convert.toIntDigitsStr(this.valStart, this.digitNum, 8);
        }
        return this.belongTo.prefix + Convert.toIntDigitsStr(this.valStart, this.digitNum);
    }

    public String getRangeTo() {
        if (this.bOctal) {
            return this.belongTo.prefix + Convert.toIntDigitsStr(this.valEnd, this.digitNum, 8) + "(Octal)";
        }
        return this.belongTo.prefix + Convert.toIntDigitsStr(this.valEnd, this.digitNum);
    }

    public String getSample() {
        return "";
    }
}
