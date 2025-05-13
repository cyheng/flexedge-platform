

package cn.doraro.flexedge.driver.gb.szy;

import cn.doraro.flexedge.core.DevAddr;
import cn.doraro.flexedge.core.UADev;
import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.util.Convert;

import java.util.List;

public class SZYAddr extends DevAddr implements Comparable<SZYAddr> {
    byte[] terminal;
    SZYFrame.FC fc;
    int idx;

    public SZYAddr() {
        this.terminal = null;
        this.fc = null;
        this.idx = -1;
    }

    static SZYAddr parseAddrStr(final String str) {
        if (Convert.isNullOrEmpty(str)) {
            return null;
        }
        final List<String> ss = Convert.splitStrWith(str, ".");
        final int n = ss.size();
        if (n != 3) {
            return null;
        }
        final SZYAddr ret = new SZYAddr();
        ret.terminal = Convert.hexStr2ByteArray((String) ss.get(0));
        final String mk = ss.get(1).toUpperCase();
        ret.fc = SZYFrame.FC.fromMk(mk);
        if (ret.fc == null) {
            return null;
        }
        ret.idx = Convert.parseToInt32((String) ss.get(2), -1);
        if (ret.idx < 0) {
            return null;
        }
        return ret;
    }

    public DevAddr parseAddr(final UADev dev, final String str, final UAVal.ValTP vtp, final StringBuilder failedr) {
        return parseAddrStr(str);
    }

    public String toString() {
        if (this.terminal != null) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(Convert.byteArray2HexStr(this.terminal));
        if (this.fc != null) {
            sb.append(".").append(this.fc.getMark());
        }
        if (this.idx >= 0) {
            sb.append(".").append(this.idx);
        }
        return sb.toString();
    }

    public String toCheckAdjStr() {
        final String str = this.getAddr();
        final SZYAddr addr = parseAddrStr(str);
        if (addr == null) {
            return null;
        }
        return this.toString();
    }

    public boolean isSupportGuessAddr() {
        return false;
    }

    public DevAddr guessAddr(final UADev dev, final String str, final UAVal.ValTP vtp) {
        return null;
    }

    public List<String> listAddrHelpers() {
        return null;
    }

    public UAVal.ValTP[] getSupportValTPs() {
        return null;
    }

    public boolean canRead() {
        return false;
    }

    public boolean canWrite() {
        return false;
    }

    public int compareTo(final SZYAddr o) {
        return 0;
    }
}
