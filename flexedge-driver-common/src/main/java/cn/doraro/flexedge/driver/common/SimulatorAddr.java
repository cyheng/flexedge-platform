// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common;

import cn.doraro.flexedge.core.DevAddr;
import cn.doraro.flexedge.core.UADev;
import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.util.Convert;

import java.util.ArrayList;
import java.util.List;

public class SimulatorAddr extends DevAddr {
    static UAVal.ValTP[] TPS0;
    static UAVal.ValTP[] TPS1;
    static UAVal.ValTP[] TPS2;

    static {
        SimulatorAddr.TPS0 = new UAVal.ValTP[]{UAVal.ValTP.vt_bool};
        SimulatorAddr.TPS1 = new UAVal.ValTP[]{UAVal.ValTP.vt_bool, UAVal.ValTP.vt_bool, UAVal.ValTP.vt_byte, UAVal.ValTP.vt_char, UAVal.ValTP.vt_int16, UAVal.ValTP.vt_int32, UAVal.ValTP.vt_int64, UAVal.ValTP.vt_float, UAVal.ValTP.vt_double};
        SimulatorAddr.TPS2 = new UAVal.ValTP[]{UAVal.ValTP.vt_str};
    }

    private char tp;
    private int regpos;
    private int bitpos;
    private SimulatorFunc simFunc;

    SimulatorAddr() {
        this.tp = 'K';
        this.regpos = -1;
        this.bitpos = -1;
        this.simFunc = null;
    }

    public SimulatorAddr(final String addr, final UAVal.ValTP vtp, final SimulatorFunc func) {
        super(addr, vtp);
        this.tp = 'K';
        this.regpos = -1;
        this.bitpos = -1;
        this.simFunc = null;
        this.simFunc = func;
    }

    public SimulatorAddr(final String addr, final UAVal.ValTP vtp, final char tp, final int regpos) {
        super(addr, vtp);
        this.tp = 'K';
        this.regpos = -1;
        this.bitpos = -1;
        this.simFunc = null;
        this.tp = tp;
        this.bitpos = -1;
        this.regpos = regpos;
    }

    public SimulatorAddr(final String addr, final char tp, final int regpos, final int bitpos) {
        super(addr, UAVal.ValTP.vt_bool);
        this.tp = 'K';
        this.regpos = -1;
        this.bitpos = -1;
        this.simFunc = null;
        this.tp = 'B';
        this.regpos = regpos;
        this.bitpos = bitpos;
    }

    static UAVal.ValTP[] getSupportedValTPs(final char tp) {
        switch (tp) {
            case 'B': {
                return SimulatorAddr.TPS0;
            }
            case 'K': {
                return SimulatorAddr.TPS1;
            }
            case 'R': {
                return SimulatorAddr.TPS1;
            }
            case 'S': {
                return SimulatorAddr.TPS2;
            }
            default: {
                return null;
            }
        }
    }

    private static boolean chkValTp(final char tp, final UAVal.ValTP vt) {
        final UAVal.ValTP[] tps = getSupportedValTPs(tp);
        if (tps == null) {
            return false;
        }
        for (final UAVal.ValTP vt2 : tps) {
            if (vt2 == vt) {
                return true;
            }
        }
        return false;
    }

    public char getAddrTp() {
        return this.tp;
    }

    public int getRegPos() {
        return this.regpos;
    }

    public int getBitPos() {
        return this.bitpos;
    }

    public SimulatorFunc getFunc() {
        return this.simFunc;
    }

    public DevAddr parseAddr(final UADev dev, final String str, final UAVal.ValTP vtp, final StringBuilder failedr) {
        final DevAddr r = this.parseSimFuncAddr(str, vtp, failedr);
        if (r != null) {
            return r;
        }
        return this.parseSimRegAddr(str, vtp, failedr);
    }

    private DevAddr parseSimFuncAddr(String str, final UAVal.ValTP vtp, final StringBuilder failedr) {
        final String addr = str;
        str = str.trim().toLowerCase();
        if (!str.endsWith(")")) {
            failedr.append("simulator function must like funcname(,,)");
            return null;
        }
        final int i = str.indexOf(40);
        if (i <= 0) {
            failedr.append("simulator function must like funcname(,,)");
            return null;
        }
        final String funcn = str.substring(0, i);
        final String paramstr = str.substring(i + 1, str.length() - 1);
        final List<String> ss = Convert.splitStrWith(paramstr, ",\uff0c");
        final ArrayList<Object> pobjs = new ArrayList<Object>();
        try {
            for (final String s : ss) {
                final int fpt = s.indexOf(46);
                if (fpt < 0) {
                    pobjs.add(Long.parseLong(s));
                } else {
                    pobjs.add(Double.parseDouble(s));
                }
            }
            final SimulatorFunc sf = SimulatorFunc.createFunc(funcn, pobjs, failedr);
            if (sf == null) {
                return null;
            }
            if (!sf.checkValTp(vtp)) {
                failedr.append("");
                return null;
            }
            return new SimulatorAddr(addr, vtp, sf);
        } catch (final Exception e) {
            failedr.append(e.getMessage());
            return null;
        }
    }

    private DevAddr parseSimRegAddr(String str, final UAVal.ValTP vtp, final StringBuilder failedr) {
        final String addr = str;
        if (Convert.isNullOrEmpty(str)) {
            failedr.append("address is empty");
            return null;
        }
        str = str.toUpperCase();
        final char tp = str.charAt(0);
        switch (tp) {
            case 'B': {
                break;
            }
            case 'K': {
                break;
            }
            case 'R': {
                break;
            }
            case 'S': {
                break;
            }
            default: {
                failedr.append("unknow address type=" + tp);
                return null;
            }
        }
        if (!chkValTp(tp, vtp)) {
            failedr.append("invalid ValTP for this address" + str);
            return null;
        }
        str = str.substring(1);
        final int i = str.indexOf(46);
        if (i < 0) {
            final int v = Integer.parseInt(str);
            return new SimulatorAddr(addr, vtp, tp, v);
        }
        final int v = Integer.parseInt(str.substring(0, i));
        final int bitv = Integer.parseInt(str.substring(i + 1));
        return new SimulatorAddr(addr, tp, v, bitv);
    }

    public boolean isSupportGuessAddr() {
        return false;
    }

    public String toCheckAdjStr() {
        return null;
    }

    public DevAddr guessAddr(final UADev dev, final String str, final UAVal.ValTP vtp) {
        return null;
    }

    public List<String> listAddrHelpers() {
        return null;
    }

    public UAVal.ValTP[] getSupportValTPs() {
        return getSupportedValTPs(this.tp);
    }

    public boolean canRead() {
        return true;
    }

    public boolean canWrite() {
        return true;
    }
}
