// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.omron.hostlink;

import cn.doraro.flexedge.core.DevAddr;
import cn.doraro.flexedge.core.UADev;
import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.util.Convert;

import java.util.Arrays;
import java.util.List;

public class HLAddr extends DevAddr implements Comparable<HLAddr> {
    protected String prefix;
    protected int addrNum;
    protected int bitNum;
    protected int digitNum;
    protected boolean bWritable;
    HLModel fxModel;
    transient HLAddrDef addrDef;
    transient HLAddrSeg addrSeg;

    public HLAddr() {
        this.prefix = null;
        this.addrNum = -1;
        this.bitNum = -1;
        this.digitNum = 3;
        this.bWritable = false;
        this.addrDef = null;
        this.addrSeg = null;
    }

    protected HLAddr(final UAVal.ValTP vtp, final String prefix, final int addr_num, final int bit_num, final int digit_num) {
        super(combineAddrStr(prefix, addr_num, bit_num, digit_num), vtp);
        this.prefix = null;
        this.addrNum = -1;
        this.bitNum = -1;
        this.digitNum = 3;
        this.bWritable = false;
        this.addrDef = null;
        this.addrSeg = null;
        this.prefix = prefix;
        this.addrNum = addr_num;
        this.bitNum = bit_num;
        this.digitNum = digit_num;
    }

    private static List<String> splitPrefixNum(String str, final StringBuilder failedr) {
        if (Convert.isNullOrTrimEmpty(str)) {
            failedr.append("addr cannot be empty");
            return null;
        }
        str = str.toUpperCase().trim();
        final int n = str.length();
        char c = str.charAt(0);
        int i = 0;
        if (c < 'A' || c > 'Z') {
            failedr.append("addr no prefix");
            return null;
        }
        String prefix = null;
        i = 1;
        while (i < n) {
            c = str.charAt(i);
            if (c < 'A' || c > 'Z') {
                prefix = str.substring(0, i);
                final String num = str.substring(i);
                if (Convert.isNullOrEmpty(num)) {
                    failedr.append("addr no number");
                    return null;
                }
                return Arrays.asList(prefix, num);
            } else {
                ++i;
            }
        }
        failedr.append("addr no number");
        return null;
    }

    public static String combineAddrStr(final String prefix, final int addr_num, final int bit_num, final int digit_num) {
        final StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        final String nstr = Integer.toString(addr_num);
        final int dn = digit_num - nstr.length();
        if (dn > 0) {
            for (int i = 0; i < dn; ++i) {
                sb.append('0');
            }
        }
        sb.append(nstr);
        if (bit_num > 0) {
            if (bit_num < 9) {
                sb.append(".0" + bit_num);
            } else {
                sb.append("." + bit_num);
            }
        }
        return sb.toString();
    }

    HLAddr asDef(final HLAddrDef addr_def, final HLAddrSeg seg) {
        this.addrDef = addr_def;
        this.addrSeg = seg;
        this.bWritable = this.addrSeg.isWritable();
        return this;
    }

    public HLAddrDef getAddrDef() {
        return this.addrDef;
    }

    public HLAddrSeg getAddrSeg() {
        return this.addrSeg;
    }

    public boolean isWritable() {
        return this.bWritable;
    }

    public void setWritable(final boolean bw) {
        this.bWritable = bw;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public int getAddrNum() {
        return this.addrNum;
    }

    public int getBitNum() {
        return this.bitNum;
    }

    public boolean isBitVal() {
        return this.bitNum >= 0 || this.addrSeg.isValBitOnly();
    }

    public int getDigitNum() {
        return this.digitNum;
    }

    public String toCheckAdjStr() {
        return combineAddrStr(this.prefix, this.addrNum, this.bitNum, this.digitNum);
    }

    public String toString() {
        return this.toCheckAdjStr();
    }

    public DevAddr parseAddr(final UADev dev, final String str, final UAVal.ValTP vtp, final StringBuilder failedr) {
        if (dev == null) {
            throw new IllegalArgumentException("no UADev");
        }
        final HLModel fx_m = (HLModel) dev.getDrvDevModel();
        if (fx_m == null) {
            throw new IllegalArgumentException("no HLModel");
        }
        final List<String> ss = splitPrefixNum(str, failedr);
        if (ss == null) {
            return null;
        }
        final String prefix = ss.get(0);
        String addr = ss.get(1);
        final int k = addr.indexOf(".");
        String bitstr = null;
        if (k > 0) {
            bitstr = addr.substring(k + 1);
            addr = addr.substring(0, k);
        }
        return fx_m.transAddr(prefix, addr, bitstr, vtp, failedr);
    }

    public DevAddr.ChkRes checkAddr(final UADev dev, final String addr, final UAVal.ValTP vtp) {
        final StringBuilder failedr = new StringBuilder();
        final List<String> ss = splitPrefixNum(addr, failedr);
        if (ss == null) {
            return new DevAddr.ChkRes(-1, addr, vtp, "Invalid HLAddr=" + addr);
        }
        final HLModel fxm = (HLModel) dev.getDrvDevModel();
        final HLAddrDef addrdef = fxm.getAddrDef(ss.get(0));
        if (addrdef == null) {
            return new DevAddr.ChkRes(-1, addr, vtp, "Invalid HLAddr no address def found");
        }
        if (vtp == null) {
            return new DevAddr.ChkRes(-1, addr, vtp, "Invalid HLAddr no ValTP found");
        }
        final HLAddrSeg seg = addrdef.findSeg(vtp, ss.get(1));
        if (seg == null) {
            return new DevAddr.ChkRes(-1, addr, vtp, "HLAddr [" + ss.get(0) + ss.get(1) + "] is invalid or not match " + vtp.getStr());
        }
        return HLAddr.CHK_RES_OK;
    }

    public boolean isSupportGuessAddr() {
        return true;
    }

    public DevAddr guessAddr(final UADev dev, final String str, UAVal.ValTP vtp) {
        if (dev == null) {
            return null;
        }
        final HLModel fx_m = (HLModel) dev.getDrvDevModel();
        if (fx_m == null) {
            return null;
        }
        final StringBuilder failedr = new StringBuilder();
        final List<String> ss = splitPrefixNum(str, failedr);
        if (ss == null) {
            return null;
        }
        final String prefix = ss.get(0);
        String addr = ss.get(1);
        final int k = addr.indexOf(".");
        String bitstr = null;
        if (k > 0) {
            bitstr = addr.substring(k + 1);
            addr = addr.substring(0, k);
            vtp = UAVal.ValTP.vt_bool;
        } else if (vtp == null) {
            vtp = UAVal.ValTP.vt_uint16;
        } else if (!vtp.isNumberVT()) {
            vtp = UAVal.ValTP.vt_uint16;
        }
        return fx_m.transAddr(prefix, addr, bitstr, vtp, failedr);
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
        return this.bWritable;
    }

    public int compareTo(final HLAddr o) {
        return this.addrNum - o.addrNum;
    }
}
