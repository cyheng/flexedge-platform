

package cn.doraro.flexedge.driver.gb.szy;

import cn.doraro.flexedge.core.util.Convert;

import java.util.ArrayList;
import java.util.List;

public class SZYFrame extends SZYMsg {
    public static final short FC_CMD = 0;
    public static final short FC_RAIN = 1;
    public static final short FC_WATER_LVL = 2;
    public static final short FC_FLOW = 3;
    public static final short FC_FLOW_RATE = 4;
    public static final short FC_GATE_POS = 5;
    public static final short FC_POWER = 6;
    public static final short FC_AIR_PRESSURE = 7;
    public static final short FC_WIND_SPEED = 8;
    public static final short FC_WATER_TEMP = 9;
    public static final short FC_WATER_QUALITY = 10;
    public static final short FC_SOIL_MOISTURE = 11;
    public static final short FC_EVAPORATION = 12;
    public static final short FC_ALARM_STATUE = 13;
    public static final short FC_COMPRE = 14;
    public static final short FC_WATER_PRESSURE = 15;
    short c;
    boolean dir;
    boolean div;
    short divs;
    short fcb;
    FC fc;
    byte[] a;
    UserData userData;

    public SZYFrame(final byte[] data) {
        super(data);
        this.dir = true;
        this.div = false;
        this.divs = 0;
        this.fcb = 3;
        this.fc = null;
        this.a = new byte[5];
        this.userData = null;
    }

    public boolean getDir() {
        return this.dir;
    }

    public boolean isDiv() {
        return this.div;
    }

    public short getFCB() {
        return this.fcb;
    }

    public FC getFuncCode() {
        return this.fc;
    }

    public byte[] getAddr() {
        return this.a;
    }

    public int getAddrA1() {
        if (this.a == null || this.a.length < 3) {
            return -1;
        }
        return Integer.parseInt(SZYMsg.transBCD2Str(this.a, 0, 3));
    }

    public int getAddrA2() {
        if (this.a == null || this.a.length < 5) {
            return -1;
        }
        int r = this.a[3] & 0xFF;
        r <<= 8;
        r += (this.a[4] & 0xFF);
        return r;
    }

    public String getAddrHex() {
        return Convert.byteArray2HexStr(this.a);
    }

    boolean parseData() {
        final byte[] bs = this.getData();
        if (bs.length < 6) {
            return false;
        }
        this.c = (short) (bs[0] & 0xFF);
        this.dir = ((this.c & 0x80) > 0);
        this.div = ((this.c & 0x40) > 0);
        this.fcb = (short) ((this.c & 0x30) >> 4);
        this.fc = FC.valueOf(this.c & 0xF);
        System.arraycopy(bs, 1, this.a, 0, 5);
        final byte[] ud = new byte[bs.length - 6];
        System.arraycopy(bs, 6, ud, 0, bs.length - 6);
        this.userData = this.parseUserData(ud);
        return this.userData != null;
    }

    private UserData parseUserData(final byte[] ud) {
        UDTermUpFlow ret = null;
        final int afn = ud[0] & 0xFF;
        switch (afn) {
            case 192: {
                ret = new UDTermUpFlow(ud);
                if (ret.parseDataField()) {
                    return ret;
                }
                return null;
            }
            default: {
                return null;
            }
        }
    }

    public UserData getUserData() {
        return this.userData;
    }

    public enum FC {
        cmd(0, "CMD", "CMD"),
        rain(1, "RN", "RAIN"),
        water_lvl(2, "WL", "WATER_LVL"),
        flow(3, "FL", "FLOW"),
        flow_rate(4, "FR", "FLOW Rate"),
        gate_pos(5, "GP", "Gate Pos");

        private final int val;
        private final String mk;
        private final String title;

        private FC(final int v, final String mk, final String tt) {
            this.val = v;
            this.mk = mk;
            this.title = tt;
        }

        public static FC valueOf(final int v) {
            for (final FC f : values()) {
                if (f.val == v) {
                    return f;
                }
            }
            return null;
        }

        public static FC fromMk(final String mk) {
            for (final FC f : values()) {
                if (f.mk.equals(mk)) {
                    return f;
                }
            }
            return null;
        }

        public int getValue() {
            return this.val;
        }

        public String getMark() {
            return this.mk;
        }

        public String getTitle() {
            return this.title;
        }
    }

    public static class UserData {
        byte[] userBS;
        short afn;
        long createDT;

        UserData(final byte[] ud) {
            this.userBS = null;
            this.createDT = System.currentTimeMillis();
            this.userBS = ud;
            this.afn = (short) (ud[0] & 0xFF);
        }

        public long getCreateDT() {
            return this.createDT;
        }
    }

    public abstract static class UDTerminalUp extends UserData {
        DFTermAlertST termAlertST;
        byte[] df;

        public UDTerminalUp(final byte[] ud) {
            super(ud);
            this.termAlertST = null;
            this.df = null;
            final int dlen = ud.length;
            if (dlen < 2) {
                throw new IllegalArgumentException("not terminal up data");
            }
            if ((ud[0] & 0xFF) != 0xC0) {
                throw new IllegalArgumentException("not terminal up data");
            }
            if (dlen > 4) {
                final byte[] bs = new byte[4];
                System.arraycopy(ud, dlen - 4, bs, 0, 4);
                this.termAlertST = new DFTermAlertST(bs);
            }
            System.arraycopy(ud, 1, this.df = new byte[dlen - 5], 0, dlen - 5);
        }

        protected abstract boolean parseDataField();
    }

    public static class UDTermUpFlow extends UDTerminalUp {
        List<Float> vals;

        public UDTermUpFlow(final byte[] ud) {
            super(ud);
            this.vals = null;
        }

        @Override
        protected boolean parseDataField() {
            final byte[] bs = this.df;
            final int n = bs.length / 5;
            if (n <= 0) {
                return false;
            }
            this.vals = new ArrayList<Float>(n);
            for (int i = 0; i < n; ++i) {
                final float v = this.transByte5BCDToFloat(bs, i * 5);
                this.vals.add(v);
            }
            return true;
        }

        float transByte5BCDToFloat(final byte[] bs, final int offset) {
            final StringBuilder sb = new StringBuilder();
            int b = bs[offset + 4] & 0xFF;
            if ((b & 0xF0) == 0xF0) {
                sb.append('-');
            }
            sb.append(b & 0xF);
            for (int i = 3; i >= 0; --i) {
                b = (bs[offset + i] & 0xFF);
                sb.append(b >> 4 & 0xF).append(b & 0xF);
            }
            return Float.parseFloat(sb.toString()) / 1000.0f;
        }

        public Float getFlow() {
            if (this.vals == null || this.vals.size() < 1) {
                return null;
            }
            return this.vals.get(1);
        }

        public Float getFlowT() {
            if (this.vals == null || this.vals.size() < 2) {
                return null;
            }
            return this.vals.get(0);
        }

        @Override
        public String toString() {
            return "flow " + Convert.combineWith((List) this.vals, ' ');
        }
    }
}
