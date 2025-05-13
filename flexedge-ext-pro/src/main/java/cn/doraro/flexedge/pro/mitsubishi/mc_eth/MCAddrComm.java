

package cn.doraro.flexedge.pro.mitsubishi.mc_eth;

import cn.doraro.flexedge.core.DevAddr;
import cn.doraro.flexedge.core.UADev;
import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.util.Convert;

import java.util.Arrays;
import java.util.List;

public abstract class MCAddrComm extends DevAddr implements Comparable<MCAddr> {
    protected String prefix;
    protected MCCode code;
    protected int addr;
    protected int digitNum;
    protected boolean bWritable;
    private int bitNum;

    public MCAddrComm() {
        this.prefix = null;
        this.code = null;
        this.addr = -1;
        this.digitNum = 3;
        this.bitNum = -1;
        this.bWritable = false;
    }

    protected MCAddrComm(final String addr_str, final UAVal.ValTP vtp, final String prefix, final int addr_num, final int digit_num, final int bitnum) {
        super(addr_str, vtp);
        this.prefix = null;
        this.code = null;
        this.addr = -1;
        this.digitNum = 3;
        this.bitNum = -1;
        this.bWritable = false;
        this.prefix = prefix;
        this.code = MCCode.getCodeBySymbol(prefix.toUpperCase());
        if (this.code == null) {
            throw new IllegalArgumentException("no code found with prefix=" + prefix);
        }
        this.addr = addr_num;
        this.digitNum = digit_num;
        this.bitNum = bitnum;
    }

    private static List<String> splitPrefixNum(String str, final StringBuilder failedr) {
        if (Convert.isNullOrTrimEmpty(str)) {
            failedr.append("addr cannot be empty");
            return null;
        }
        final int bitdot = str.lastIndexOf(46);
        int bitnum = -1;
        if (bitdot > 0) {
            try {
                bitnum = Integer.parseInt(str.substring(bitdot + 1));
            } catch (final Exception ee) {
                failedr.append("invalid bit pos " + str.substring(bitdot));
                return null;
            }
            str = str.substring(0, bitdot);
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
                return Arrays.asList(prefix, num, new StringBuilder().append(bitnum).toString());
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

    public MCCode getCode() {
        return this.code;
    }

    public int getAddrIdx() {
        return this.addr;
    }

    public int getDigitNum() {
        return this.digitNum;
    }

    public boolean isHex() {
        return this.code.sign == MCCode.Sign.hex;
    }

    public int getBitNum() {
        return this.bitNum;
    }

    public void setWritable(final boolean bw) {
        this.bWritable = bw;
    }

    public String toCheckAdjStr() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.prefix);
        String nstr = null;
        if (this.isHex()) {
            nstr = Integer.toHexString(this.addr).toUpperCase();
        } else {
            nstr = Integer.toString(this.addr).toUpperCase();
        }
        final int dn = this.digitNum - nstr.length();
        if (dn > 0) {
            for (int i = 0; i < dn; ++i) {
                sb.append('0');
            }
        }
        sb.append(nstr);
        if (this.bitNum > 0) {
            if (this.bitNum < 9) {
                sb.append(".0").append(this.bitNum);
            } else {
                sb.append(".").append(this.bitNum);
            }
        }
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
            throw new IllegalArgumentException("no MCModel");
        }
        final List<String> ss = splitPrefixNum(str, failedr);
        if (ss == null) {
            return null;
        }
        final String prefix = ss.get(0);
        return fx_m.transAddr(prefix, ss.get(1), ss.get(2), vtp, failedr);
    }

    public DevAddr.ChkRes checkAddr(final UADev dev, final String addr, final UAVal.ValTP vtp) {
        final StringBuilder failedr = new StringBuilder();
        final List<String> ss = splitPrefixNum(addr, failedr);
        if (ss == null) {
            return new DevAddr.ChkRes(-1, addr, vtp, "Invalid MCAddr=" + addr);
        }
        final MCModel fxm = (MCModel) dev.getDrvDevModel();
        final MCAddrDef addrdef = fxm.getAddrDef(ss.get(0));
        if (addrdef == null) {
            return new DevAddr.ChkRes(-1, addr, vtp, "Invalid MCAddr no address def found");
        }
        final MCAddrSeg seg = addrdef.findSeg(vtp, ss.get(1));
        if (seg == null) {
            return new DevAddr.ChkRes(-1, addr, vtp, "MCAddr [" + ss.get(0) + ss.get(1) + "] is invalid or not match " + ((vtp != null) ? vtp.getStr() : "vtp=null"));
        }
        return MCAddrComm.CHK_RES_OK;
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
        return fx_m.transAddr(prefix, ss.get(1), ss.get(2), vtp, failedr);
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

    public int compareTo(final MCAddr o) {
        return this.addr - o.addr;
    }
}
