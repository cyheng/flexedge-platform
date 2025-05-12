// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.s7.eth;

import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.DevAddr;

public class S7AddrSeg implements DevAddr.IAddrDefSeg
{
    int valStart;
    int valEnd;
    int digitNum;
    boolean bValBit;
    boolean bAddrStepInt32;
    UAVal.ValTP[] valTPs;
    boolean bHex;
    boolean bWrite;
    boolean bBitPos;
    transient S7AddrDef belongTo;
    
    S7AddrSeg(final int valstart, final int valend, final int digit_num, final UAVal.ValTP[] tps, final boolean b_write) {
        this.valStart = 0;
        this.valEnd = -1;
        this.digitNum = -1;
        this.bValBit = false;
        this.bAddrStepInt32 = false;
        this.valTPs = null;
        this.bHex = false;
        this.bWrite = true;
        this.bBitPos = false;
        this.valStart = valstart;
        this.valEnd = valend;
        this.digitNum = digit_num;
        this.valTPs = tps;
        this.bWrite = b_write;
    }
    
    S7AddrSeg(final int valstart, final int valend, final int digit_num, final UAVal.ValTP[] tps) {
        this.valStart = 0;
        this.valEnd = -1;
        this.digitNum = -1;
        this.bValBit = false;
        this.bAddrStepInt32 = false;
        this.valTPs = null;
        this.bHex = false;
        this.bWrite = true;
        this.bBitPos = false;
        this.valStart = valstart;
        this.valEnd = valend;
        this.digitNum = digit_num;
        this.valTPs = tps;
    }
    
    public S7AddrSeg asHex(final boolean b) {
        this.bHex = b;
        return this;
    }
    
    public S7AddrSeg asValBit(final boolean b) {
        this.bValBit = b;
        return this;
    }
    
    public S7AddrSeg asAddrStepInt32(final boolean b) {
        this.bAddrStepInt32 = b;
        return this;
    }
    
    public S7AddrSeg asBitPos(final boolean b) {
        this.bBitPos = b;
        return this;
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
    
    public boolean canBitPos() {
        return this.bBitPos;
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
        if (this.bHex) {
            addrn = Integer.parseInt(addr_num, 16);
        }
        else {
            addrn = Integer.parseInt(addr_num);
        }
        if (addrn >= this.valStart && addrn <= this.valEnd) {
            return addrn;
        }
        return null;
    }
    
    public boolean matchAddr(final S7Addr addr) {
        final int addrn = addr.getOffsetBytes();
        return addrn >= this.valStart && addrn <= this.valEnd;
    }
    
    public String getRangeFrom() {
        if (this.bHex) {
            return this.belongTo.prefix + Convert.toIntDigitsStr(this.valStart, this.digitNum, 16);
        }
        return this.belongTo.prefix + Convert.toIntDigitsStr(this.valStart, this.digitNum);
    }
    
    public String getRangeTo() {
        if (this.bHex) {
            return this.belongTo.prefix + Convert.toIntDigitsStr(this.valEnd, this.digitNum, 16);
        }
        return this.belongTo.prefix + Convert.toIntDigitsStr(this.valEnd, this.digitNum);
    }
    
    public String getSample() {
        return "";
    }
}
