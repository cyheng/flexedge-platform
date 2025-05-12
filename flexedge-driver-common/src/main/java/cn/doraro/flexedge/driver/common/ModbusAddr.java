// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common;

import cn.doraro.flexedge.core.DevAddr;
import cn.doraro.flexedge.core.UADev;
import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.util.Convert;

import java.util.List;

public class ModbusAddr extends DevAddr implements Comparable<ModbusAddr> {
    public static final short COIL_OUTPUT = 48;
    public static final short COIL_INPUT = 49;
    public static final short REG_INPUT = 51;
    public static final short REG_HOLD = 52;
    short addrTp;
    int regPos;
    int bitPos;

    ModbusAddr() {
        this.addrTp = -1;
        this.regPos = -1;
        this.bitPos = -1;
    }

    public ModbusAddr(final String addr, final UAVal.ValTP vtp, final char addrtp, final int regpos, final int bitpos) {
        super(addr, vtp);
        this.addrTp = -1;
        this.regPos = -1;
        this.bitPos = -1;
        this.addrTp = (short) (addrtp & '\u00ff');
        this.regPos = regpos;
        this.bitPos = bitpos;
    }

    public static ModbusAddr parseModbusAddr(String str, UAVal.ValTP vtp, final StringBuilder failedr) {
        final String addr = str;
        if (Convert.isNullOrEmpty(str) || str.length() < 2) {
            failedr.append("invalid address,address must 0xxxxx 1xxxxx 3xxxxx 4xxxxx");
            return null;
        }
        final char c = str.charAt(0);
        str = str.substring(1);
        switch (c) {
            case '0':
            case '1': {
                if (vtp == UAVal.ValTP.vt_none) {
                    vtp = UAVal.ValTP.vt_bool;
                }
                if (vtp != UAVal.ValTP.vt_bool) {
                    failedr.append("invalid address,address must 0xxxxx 1xxxxx 3xxxxx 4xxxxx");
                    return null;
                }
                break;
            }
            case '3':
            case '4': {
                break;
            }
            default: {
                failedr.append("invalid address,address must 0xxxxx 1xxxxx 3xxxxx 4xxxxx");
                return null;
            }
        }
        final int i = str.indexOf(46);
        if (i >= 0) {
            final int v = Integer.parseInt(str.substring(0, i));
            final int bitv = Integer.parseInt(str.substring(i + 1));
            return new ModbusAddr(addr, vtp, c, v, bitv);
        }
        final int v = Integer.parseInt(str) - 1;
        if (v < 0) {
            failedr.append("invalid address value=" + str);
            return null;
        }
        return new ModbusAddr(addr, vtp, c, v, 0);
    }

    public DevAddr parseAddr(final UADev dev, final String str, final UAVal.ValTP vtp, final StringBuilder failedr) {
        return parseModbusAddr(str, vtp, failedr);
    }

    public DevAddr.ChkRes checkAddr(final UADev dev, final String addr, final UAVal.ValTP vtp) {
        final StringBuilder failedr = new StringBuilder();
        final ModbusAddr ma = parseModbusAddr(addr, vtp, failedr);
        if (ma == null) {
            return new DevAddr.ChkRes(-1, (String) null, (UAVal.ValTP) null, failedr.toString());
        }
        return ModbusAddr.CHK_RES_OK;
    }

    public DevAddr guessAddr(final UADev dev, String str, UAVal.ValTP vtp) {
        if (Convert.isNullOrEmpty(str) || str.length() < 2) {
            return null;
        }
        char c = str.charAt(0);
        switch (c) {
            case '0':
            case '1': {
                vtp = UAVal.ValTP.vt_bool;
                break;
            }
            case '3':
            case '4': {
                if (vtp != UAVal.ValTP.vt_uint16) {
                    vtp = UAVal.ValTP.vt_int16;
                    break;
                }
                break;
            }
            default: {
                str = "0" + str;
                vtp = UAVal.ValTP.vt_bool;
                break;
            }
        }
        c = str.charAt(0);
        final StringBuilder sb = new StringBuilder();
        return this.parseAddr(dev, str, vtp, sb);
    }

    public short getAddrTp() {
        return this.addrTp;
    }

    public List<String> listAddrHelpers() {
        return null;
    }

    public UAVal.ValTP[] getSupportValTPs() {
        return null;
    }

    public boolean isSupportGuessAddr() {
        return true;
    }

    public boolean canRead() {
        return true;
    }

    public boolean canWrite() {
        switch (this.addrTp) {
            case 48:
            case 52: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

    public int getRegPos() {
        return this.regPos;
    }

    public int getRegEnd() {
        return this.regPos + this.getValTP().getValByteLen();
    }

    public int getBitPos() {
        return this.bitPos;
    }

    private String formatVal() {
        final String s = "" + (this.regPos + 1);
        final int len = s.length();
        if (len > 5) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5 - len; ++i) {
            sb.append('0');
        }
        sb.append(s);
        return sb.toString();
    }

    public String toCheckAdjStr() {
        final String str = this.getAddr();
        final char c = str.charAt(0);
        final String fstr = this.formatVal();
        if (fstr == null) {
            return null;
        }
        switch (c) {
            case '0':
            case '1':
            case '3':
            case '4': {
                return c + this.formatVal();
            }
            default: {
                return null;
            }
        }
    }

    public int compareTo(final ModbusAddr o) {
        final int v = this.regPos - o.regPos;
        if (v == 0) {
            return this.bitPos - o.bitPos;
        }
        return v;
    }
}
