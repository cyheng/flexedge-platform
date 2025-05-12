// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.mitsubishi;

import cn.doraro.flexedge.core.DevAddr;
import cn.doraro.flexedge.core.UADev;
import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.driver.mitsubishi.fx.FxAddrDef;
import cn.doraro.flexedge.driver.mitsubishi.fx.FxAddrSeg;
import cn.doraro.flexedge.driver.mitsubishi.fx.MCModel;

import java.util.Arrays;
import java.util.List;

public abstract class Addr extends DevAddr implements Comparable<Addr> {
    protected String prefix;
    protected int addrNum;
    protected int digitNum;
    protected boolean bOct;
    protected boolean bValBit;
    protected boolean bWritable;

    public Addr() {
        this.prefix = null;
        this.addrNum = -1;
        this.digitNum = 3;
        this.bOct = false;
        this.bValBit = false;
        this.bWritable = false;
    }

    protected Addr(final String addr_str, final UAVal.ValTP vtp, final String prefix, final int addr_num, final boolean b_valbit, final int digit_num, final boolean b_oct) {
        super(addr_str, vtp);
        this.prefix = null;
        this.addrNum = -1;
        this.digitNum = 3;
        this.bOct = false;
        this.bValBit = false;
        this.bWritable = false;
        this.prefix = prefix;
        this.addrNum = addr_num;
        this.bValBit = b_valbit;
        this.digitNum = digit_num;
        this.bOct = b_oct;
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

    public String getPrefix() {
        return this.prefix;
    }

    public int getAddrNum() {
        return this.addrNum;
    }

    public boolean isValBit() {
        return this.bValBit;
    }

    public int getDigitNum() {
        return this.digitNum;
    }

    public boolean isOctal() {
        return this.bOct;
    }

    public abstract int getBytesInBase();

    public void setWritable(final boolean bw) {
        this.bWritable = bw;
    }

    public int getInBits() {
        return this.addrNum % 8;
    }

    public String toCheckAdjStr() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.prefix);
        String nstr = null;
        if (this.bOct) {
            nstr = Integer.toOctalString(this.addrNum);
        } else {
            nstr = Integer.toString(this.addrNum);
        }
        final int dn = this.digitNum - nstr.length();
        if (dn > 0) {
            for (int i = 0; i < dn; ++i) {
                sb.append('0');
            }
        }
        sb.append(nstr);
        return sb.toString();
    }

    public String toString() {
        return this.toCheckAdjStr();
    }

    public DevAddr parseAddr(final UADev dev, final String str, final UAVal.ValTP vtp, final StringBuilder failedr) {
        if (dev == null) {
            throw new IllegalArgumentException("no UADev");
        }
        final MCModel fx_m = (MCModel) dev.getDrvDevModel();
        if (fx_m == null) {
            throw new IllegalArgumentException("no FxModel");
        }
        final List<String> ss = splitPrefixNum(str, failedr);
        if (ss == null) {
            return null;
        }
        final String prefix = ss.get(0);
        return fx_m.transAddr(prefix, ss.get(1), vtp, failedr);
    }

    public DevAddr.ChkRes checkAddr(final UADev dev, final String addr, final UAVal.ValTP vtp) {
        final StringBuilder failedr = new StringBuilder();
        final List<String> ss = splitPrefixNum(addr, failedr);
        if (ss == null) {
            return new DevAddr.ChkRes(-1, addr, vtp, "Invalid FxAddr=" + addr);
        }
        final MCModel fxm = (MCModel) dev.getDrvDevModel();
        final FxAddrDef addrdef = fxm.getAddrDef(ss.get(0));
        if (addrdef == null) {
            return new DevAddr.ChkRes(-1, addr, vtp, "Invalid FxAddr no address def found");
        }
        final FxAddrSeg seg = addrdef.findSeg(vtp, ss.get(1));
        if (seg == null) {
            return new DevAddr.ChkRes(-1, addr, vtp, "FxAddr [" + ss.get(0) + ss.get(1) + "] is invalid or not match " + ((vtp != null) ? vtp.getStr() : "vtp=null"));
        }
        return Addr.CHK_RES_OK;
    }

    public boolean isSupportGuessAddr() {
        return true;
    }

    public DevAddr guessAddr(final UADev dev, final String str, final UAVal.ValTP vtp) {
        if (dev == null) {
            return null;
        }
        final MCModel fx_m = (MCModel) dev.getDrvDevModel();
        if (fx_m == null) {
            return null;
        }
        final StringBuilder failedr = new StringBuilder();
        final List<String> ss = splitPrefixNum(str, failedr);
        if (ss == null) {
            return null;
        }
        final String prefix = ss.get(0);
        return fx_m.transAddr(prefix, ss.get(1), vtp, failedr);
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

    public int compareTo(final Addr o) {
        return this.addrNum - o.addrNum;
    }
}
