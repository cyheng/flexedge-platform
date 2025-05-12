// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.s7.ppi;

import cn.doraro.flexedge.core.DevAddr;
import cn.doraro.flexedge.core.UADev;
import cn.doraro.flexedge.core.UAVal;

import java.util.List;

public class PPIAddr extends DevAddr implements Comparable<PPIAddr> {
    AddrPt addrPt;
    int bytesNum;

    public PPIAddr() {
        this.addrPt = null;
    }

    public PPIAddr(final String addr, final UAVal.ValTP vtp) throws Exception {
        super(addr, vtp);
        this.addrPt = null;
        final StringBuilder failedr = new StringBuilder();
        this.addrPt = parseAddrPt(addr, failedr);
        if (this.addrPt == null) {
            throw new Exception(failedr.toString());
        }
        if (this.addrPt.memValTp != null) {
            this.bytesNum = this.addrPt.memValTp.getByteNum();
        } else {
            this.bytesNum = vtp.getValByteLen();
        }
    }

    private PPIAddr(final String addr, final AddrPt apt, final UAVal.ValTP vtp) {
        super(addr, vtp);
        this.addrPt = null;
        this.addrPt = apt;
        if (this.addrPt.memValTp != null) {
            this.bytesNum = this.addrPt.memValTp.getByteNum();
        } else {
            this.bytesNum = vtp.getValByteLen();
        }
    }

    public PPIAddr(final PPIMemTp mtp, final int byteoffsets, final int inbit, final UAVal.ValTP vtp) {
        super(mtp.name() + "B" + byteoffsets + ((inbit >= 0) ? ("." + inbit) : ""), vtp);
        this.addrPt = null;
        this.addrPt = new AddrPt();
        this.addrPt.memTp = mtp;
        this.addrPt.offsetBytes = byteoffsets;
        this.addrPt.inBit = inbit;
        if (this.addrPt.memValTp != null) {
            this.bytesNum = this.addrPt.memValTp.getByteNum();
        } else {
            this.bytesNum = vtp.getValByteLen();
        }
    }

    public static PPIAddr parsePPIAddr(final String addr, UAVal.ValTP vtp, final StringBuilder failedr) {
        final AddrPt apt = parseAddrPt(addr, failedr);
        if (apt == null) {
            return null;
        }
        if (vtp == null && apt.getMemTp() != null) {
            vtp = apt.getMemTp().getFitValTPs()[0];
        }
        if (!checkFit(addr, apt, vtp, failedr)) {
            return null;
        }
        return new PPIAddr(addr, apt, vtp);
    }

    private static boolean checkFit(final String addr, final AddrPt apt, final UAVal.ValTP vtp, final StringBuilder failedr) {
        final DevAddr.ChkRes cr = checkFit(addr, apt, vtp);
        if (cr == null || cr.isChkOk()) {
            return true;
        }
        failedr.append(cr.getChkPrompt());
        return false;
    }

    private static DevAddr.ChkRes checkFit(final String addr, final AddrPt apt, final UAVal.ValTP vtp) {
        if (apt.isBitAddr() && vtp != UAVal.ValTP.vt_bool) {
            return new DevAddr.ChkRes(0, addr, UAVal.ValTP.vt_bool, "PPI Addr [" + addr + "] must use bool value type");
        }
        if (apt.memTp == PPIMemTp.T && vtp != UAVal.ValTP.vt_uint32) {
            return new DevAddr.ChkRes(0, addr, UAVal.ValTP.vt_uint32, "PPI Addr [" + addr + "] must use uint32");
        }
        if (apt.memTp == PPIMemTp.C && vtp != UAVal.ValTP.vt_uint16) {
            return new DevAddr.ChkRes(0, addr, UAVal.ValTP.vt_uint32, "PPI Addr [" + addr + "] must use uint16");
        }
        if (apt.memValTp == null) {
        }
        return PPIAddr.CHK_RES_OK;
    }

    public static AddrPt parseAddrPt(String addr, final StringBuilder failedr) {
        int k;
        int c;
        for (addr = addr.toUpperCase(), k = 0; k < addr.length(); ++k) {
            c = addr.charAt(k);
            if (c >= 48 && c <= 57) {
                break;
            }
        }
        String pstr = addr.substring(0, k);
        final String sstr = addr.substring(k);
        final char vtc = pstr.charAt(k - 1);
        PPIMemValTp vt = PPIMemValTp.valOf(vtc);
        if (vt != null) {
            pstr = pstr.substring(0, k - 1);
        }
        final PPIMemTp mtp = PPIMemTp.valOf(pstr);
        if (mtp == null) {
            failedr.append("unknown mem tp:" + pstr);
            return null;
        }
        if (vt == null) {
            switch (mtp) {
                case S:
                case SM:
                case I:
                case Q:
                case M:
                case V: {
                    vt = PPIMemValTp.B;
                    break;
                }
                case AI:
                case AQ:
                case C:
                case T: {
                    vt = PPIMemValTp.W;
                    break;
                }
                case HC: {
                    vt = PPIMemValTp.D;
                    break;
                }
                default: {
                    vt = PPIMemValTp.B;
                    break;
                }
            }
        }
        final AddrPt ret = new AddrPt();
        ret.memTp = mtp;
        k = sstr.indexOf(46);
        try {
            if (k > 0) {
                ret.offsetBytes = Integer.parseInt(sstr.substring(0, k));
                ret.inBit = Integer.parseInt(sstr.substring(k + 1));
            } else {
                ret.offsetBytes = Integer.parseInt(sstr);
                if (mtp == PPIMemTp.T) {
                    final AddrPt addrPt = ret;
                    addrPt.offsetBytes *= 4;
                } else if (mtp == PPIMemTp.C) {
                    final AddrPt addrPt2 = ret;
                    addrPt2.offsetBytes *= 2;
                }
            }
        } catch (final Exception e) {
            failedr.append(e.getMessage());
            return null;
        }
        return ret;
    }

    public DevAddr parseAddr(final UADev dev, final String str, final UAVal.ValTP vtp, final StringBuilder failedr) {
        return parsePPIAddr(str, vtp, failedr);
    }

    public DevAddr.ChkRes checkAddr(final UADev dev, final String addr, final UAVal.ValTP vtp) {
        final StringBuilder failedr = new StringBuilder();
        final AddrPt apt = parseAddrPt(addr, failedr);
        if (apt == null) {
            return new DevAddr.ChkRes(-1, (String) null, (UAVal.ValTP) null, failedr.toString());
        }
        return checkFit(addr, apt, vtp);
    }

    public int compareTo(final PPIAddr o) {
        return this.addrPt.offsetBytes - o.addrPt.offsetBytes;
    }

    public boolean isSupportGuessAddr() {
        return true;
    }

    public DevAddr guessAddr(final UADev dev, final String str, final UAVal.ValTP vtp) {
        final StringBuilder failedr = new StringBuilder();
        final PPIAddr ppiaddr = parsePPIAddr(str, vtp, failedr);
        if (ppiaddr == null) {
            return null;
        }
        return ppiaddr;
    }

    public List<String> listAddrHelpers() {
        return null;
    }

    public UAVal.ValTP[] getSupportValTPs() {
        return this.addrPt.memTp.getFitValTPs();
    }

    public boolean canRead() {
        return true;
    }

    public boolean canWrite() {
        return this.addrPt != null && this.addrPt.memTp.canWrite();
    }

    public int getOffsetBytes() {
        return this.addrPt.getOffsetBytes();
    }

    public int getInBits() {
        return this.addrPt.getInBit();
    }

    public int getOffsetBits() {
        return this.addrPt.getOffsetBits();
    }

    public boolean isBitAddr() {
        return this.addrPt.isBitAddr();
    }

    public int getRegEnd() {
        return this.addrPt.offsetBytes + this.getValTP().getValByteLen();
    }

    public PPIMemTp getMemTp() {
        return this.addrPt.memTp;
    }

    public int getBytesNum() {
        return this.bytesNum;
    }

    public PPIMemValTp getFitMemValTp() {
        if (this.isBitAddr()) {
            return PPIMemValTp.BIT;
        }
        switch (this.bytesNum) {
            case 1: {
                return PPIMemValTp.B;
            }
            case 2: {
                return PPIMemValTp.W;
            }
            case 4: {
                return PPIMemValTp.D;
            }
            default: {
                return null;
            }
        }
    }

    public String toString() {
        return this.addrPt + " " + this.valTP;
    }

    public String toCheckAdjStr() {
        return this.addrPt.toString();
    }

    public static class AddrPt {
        PPIMemTp memTp;
        int offsetBytes;
        int inBit;
        PPIMemValTp memValTp;

        public AddrPt() {
            this.offsetBytes = 0;
            this.inBit = -1;
            this.memValTp = null;
        }

        public PPIMemTp getMemTp() {
            return this.memTp;
        }

        public int getInBit() {
            return this.inBit;
        }

        public int getOffsetBytes() {
            return this.offsetBytes;
        }

        public int getOffsetBits() {
            return this.offsetBytes * 8 + ((this.inBit >= 0) ? this.inBit : 0);
        }

        public boolean isBitAddr() {
            return this.inBit >= 0;
        }

        @Override
        public String toString() {
            if (this.inBit >= 0 && this.memTp.hasBit()) {
                return this.memTp.name() + this.offsetBytes + "." + this.inBit;
            }
            String ret = this.memTp.name();
            ret += this.offsetBytes;
            return ret;
        }
    }
}
