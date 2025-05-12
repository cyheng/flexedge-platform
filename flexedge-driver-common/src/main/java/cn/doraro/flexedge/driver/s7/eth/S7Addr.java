// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.s7.eth;

import cn.doraro.flexedge.core.DevAddr;
import cn.doraro.flexedge.core.UADev;
import cn.doraro.flexedge.core.UAVal;

import java.util.List;

public class S7Addr extends DevAddr implements Comparable<S7Addr> {
    int dbNum;
    S7MemTp memTp;
    int offsetBytes;
    int inBit;
    S7ValTp memValTp;
    int bytesNum;

    public S7Addr(final String addr, final UAVal.ValTP vtp) {
        super(addr, vtp);
        this.dbNum = -1;
        this.offsetBytes = 0;
        this.inBit = -1;
        this.memValTp = null;
        if (vtp != null) {
            this.bytesNum = vtp.getValByteLen();
        }
    }

    public static S7Addr parseS7Addr(final String addr, UAVal.ValTP vtp, final StringBuilder failedr) {
        final S7Addr apt = parseAddrPt(addr, failedr);
        if (apt == null) {
            return null;
        }
        if (vtp == null) {
            vtp = apt.getValTP();
            if (vtp == null) {
                vtp = apt.getFitMemValTp().getValTP();
            }
        }
        if (!checkFit(addr, apt, vtp, failedr)) {
            return null;
        }
        apt.valTP = vtp;
        apt.bytesNum = vtp.getValByteLen();
        return apt;
    }

    private static boolean checkFit(final String addr, final S7Addr apt, final UAVal.ValTP vtp, final StringBuilder failedr) {
        final DevAddr.ChkRes cr = checkFit(addr, apt, vtp);
        if (cr == null || cr.isChkOk()) {
            return true;
        }
        failedr.append(cr.getChkPrompt());
        return false;
    }

    private static DevAddr.ChkRes checkFit(final String addr, final S7Addr apt, final UAVal.ValTP vtp) {
        if (apt.isBitAddr()) {
            if (vtp != UAVal.ValTP.vt_bool) {
                return new DevAddr.ChkRes(0, addr, UAVal.ValTP.vt_bool, "S7 Addr [" + addr + "] must use bool value type");
            }
            return S7Addr.CHK_RES_OK;
        } else {
            if (apt.memTp == S7MemTp.T && vtp != UAVal.ValTP.vt_uint32) {
                return new DevAddr.ChkRes(0, addr, UAVal.ValTP.vt_uint32, "S7 Addr [" + addr + "] must use uint32");
            }
            if (apt.memTp == S7MemTp.C && vtp != UAVal.ValTP.vt_uint16) {
                return new DevAddr.ChkRes(0, addr, UAVal.ValTP.vt_uint16, "S7 Addr [" + addr + "] must use uint16");
            }
            final S7ValTp s7vt = apt.getMemValTp();
            switch (s7vt) {
                case X: {
                    if (vtp != UAVal.ValTP.vt_bool) {
                        return new DevAddr.ChkRes(0, addr, UAVal.ValTP.vt_bool, "S7 Addr [" + addr + "] must use bool value type");
                    }
                    break;
                }
                case B: {
                    if (vtp != UAVal.ValTP.vt_byte && vtp != UAVal.ValTP.vt_uint8) {
                        return new DevAddr.ChkRes(0, addr, UAVal.ValTP.vt_uint8, "S7 Addr [" + addr + "] may use vt_uint8 value type");
                    }
                    break;
                }
                case W: {
                    if (vtp != UAVal.ValTP.vt_int16 && vtp != UAVal.ValTP.vt_uint16) {
                        return new DevAddr.ChkRes(0, addr, UAVal.ValTP.vt_uint16, "S7 Addr [" + addr + "] may use vt_uint16 value type");
                    }
                    break;
                }
                case D: {
                    if (vtp != UAVal.ValTP.vt_int32 && vtp != UAVal.ValTP.vt_uint32) {
                        return new DevAddr.ChkRes(0, addr, UAVal.ValTP.vt_uint32, "S7 Addr [" + addr + "] may use vt_uint32 value type");
                    }
                    break;
                }
            }
            return S7Addr.CHK_RES_OK;
        }
    }

    private static S7Addr parseAddrDB(String addr, final StringBuilder failedr) {
        int k = addr.indexOf(44);
        if (k <= 0) {
            failedr.append("DB address may be like DB200,D2 no [,] found");
            return null;
        }
        final String fulladdr = addr;
        final String dbstr = addr.substring(0, k);
        try {
            final S7MemTp ret_memTp = S7MemTp.DB;
            final int ret_dbNum = Integer.parseInt(dbstr.substring(2));
            addr = addr.substring(k + 1);
            if (addr.startsWith("DB")) {
                addr = addr.substring(2);
            }
            k = 0;
            for (int addrlen = addr.length(); k < addrlen; ++k) {
                final int c = addr.charAt(k);
                if (c >= 48 && c <= 57) {
                    break;
                }
            }
            if (k <= 0) {
                failedr.append("no data type found in addr" + addr);
                return null;
            }
            final String vtstr = addr.substring(0, k);
            final S7ValTp vtp = S7ValTp.valOf(vtstr);
            if (vtp == null) {
                failedr.append("unknown data type [" + vtstr + "] found in addr " + addr);
                return null;
            }
            final S7Addr ret = new S7Addr(fulladdr, vtp.getValTP());
            ret.memTp = ret_memTp;
            ret.dbNum = ret_dbNum;
            ret.memValTp = vtp;
            addr = addr.substring(k);
            k = addr.indexOf(46);
            if (k > 0) {
                ret.offsetBytes = Integer.parseInt(addr.substring(0, k));
                ret.inBit = Integer.parseInt(addr.substring(k + 1));
            } else {
                ret.offsetBytes = Integer.parseInt(addr);
            }
            return ret;
        } catch (final Exception e) {
            failedr.append(e.getMessage());
            return null;
        }
    }

    private static S7Addr parseAddrPt(String addr0, final StringBuilder failedr) {
        String addr;
        addr0 = (addr = addr0.toUpperCase());
        if (addr.startsWith("DB")) {
            return parseAddrDB(addr, failedr);
        }
        final S7MemTp mt = S7MemTp.parseStrHead(addr);
        if (mt == null) {
            failedr.append("unknown mem tp:" + addr);
            return null;
        }
        addr = addr.substring(mt.name().length());
        int k = 0;
        for (int addrlen = addr.length(); k < addrlen; ++k) {
            final int c = addr.charAt(k);
            if (c >= 48 && c <= 57) {
                break;
            }
        }
        S7ValTp vt = null;
        if (k > 0) {
            final String pstr = addr.substring(0, k);
            vt = S7ValTp.valOf(pstr);
            if (vt == null) {
                failedr.append("unknown val tp:" + pstr);
                return null;
            }
            addr = addr.substring(k);
        }
        if (vt == null) {
            vt = mt.getDefaultS7ValTp();
        }
        final S7Addr ret = new S7Addr(addr0, vt.getValTP());
        ret.memTp = mt;
        ret.memValTp = vt;
        ret.bytesNum = vt.getByteNum();
        try {
            k = addr.indexOf(46);
            if (k > 0) {
                ret.offsetBytes = Integer.parseInt(addr.substring(0, k));
                ret.inBit = Integer.parseInt(addr.substring(k + 1));
            } else {
                if (vt == S7ValTp.X) {
                    failedr.append("bit addr must end with num.num");
                    return null;
                }
                ret.offsetBytes = Integer.parseInt(addr);
            }
        } catch (final Exception e) {
            failedr.append(e.getMessage());
            return null;
        }
        return ret;
    }

    public DevAddr parseAddr(final UADev dev, final String str, final UAVal.ValTP vtp, final StringBuilder failedr) {
        return parseS7Addr(str, vtp, failedr);
    }

    public S7MemTp getMemTp() {
        return this.memTp;
    }

    public S7ValTp getMemValTp() {
        return this.memValTp;
    }

    public boolean isDBMem() {
        return this.dbNum > 0;
    }

    public int getDBNum() {
        return this.dbNum;
    }

    public String getAreaKey() {
        if (this.memTp == S7MemTp.DB) {
            return "DB" + this.dbNum;
        }
        return this.memTp.name();
    }

    public int getInBits() {
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

    public DevAddr.ChkRes checkAddr(final UADev dev, final String addr, final UAVal.ValTP vtp) {
        final StringBuilder failedr = new StringBuilder();
        final S7Addr apt = parseAddrPt(addr, failedr);
        if (apt == null) {
            return new DevAddr.ChkRes(-1, (String) null, (UAVal.ValTP) null, failedr.toString());
        }
        return checkFit(addr, apt, vtp);
    }

    public int compareTo(final S7Addr o) {
        return this.offsetBytes - o.offsetBytes;
    }

    public boolean isSupportGuessAddr() {
        return true;
    }

    public DevAddr guessAddr(final UADev dev, final String str, final UAVal.ValTP vtp) {
        final StringBuilder failedr = new StringBuilder();
        final S7Addr addr = parseS7Addr(str, vtp, failedr);
        if (addr == null) {
            return null;
        }
        return addr;
    }

    public List<String> listAddrHelpers() {
        return null;
    }

    public UAVal.ValTP[] getSupportValTPs() {
        return this.memTp.getFitValTPs();
    }

    public boolean canRead() {
        return true;
    }

    public boolean canWrite() {
        return this.memTp.canWrite();
    }

    public int getRegEnd() {
        return this.offsetBytes + this.getValTP().getValByteLen();
    }

    public boolean chkSameArea(final S7MemTp mtp, final int db_num) {
        return this.memTp == mtp && (mtp != S7MemTp.DB || this.dbNum == db_num);
    }

    public int getBytesNum() {
        return this.bytesNum;
    }

    public S7ValTp getFitMemValTp() {
        if (this.isBitAddr()) {
            return S7ValTp.X;
        }
        switch (this.bytesNum) {
            case 1: {
                return S7ValTp.B;
            }
            case 2: {
                return S7ValTp.W;
            }
            case 4: {
                return S7ValTp.D;
            }
            default: {
                return null;
            }
        }
    }

    public String toString() {
        String ret = this.memTp.name();
        if (this.memTp == S7MemTp.DB) {
            ret = ret + this.dbNum + ",";
        }
        if (this.inBit >= 0 && this.memTp.hasBit()) {
            if (this.memTp == S7MemTp.DB || this.memTp.getDefaultS7ValTp() != this.memValTp) {
                ret += this.memValTp.name();
            }
            ret = ret + this.offsetBytes + "." + this.inBit;
        } else {
            ret += this.memValTp.name();
            ret += this.offsetBytes;
        }
        return ret;
    }

    public String toCheckAdjStr() {
        return this.toString();
    }
}
