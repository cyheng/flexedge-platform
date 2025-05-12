// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.gb.szy;

import cn.doraro.flexedge.core.*;
import cn.doraro.flexedge.core.basic.PropGroup;
import cn.doraro.flexedge.core.conn.ConnPtStream;
import cn.doraro.flexedge.core.util.ILang;

import java.io.InputStream;
import java.util.List;

public class SZY206_2016Driver extends DevDriver implements ILang {
    private static final int POLYNOMIAL = 4129;
    private static final int INITIAL_VALUE = 65535;
    static SZYAddr szyAddr;

    static {
        SZY206_2016Driver.szyAddr = new SZYAddr();
    }

    SZYListener szyLis;

    public SZY206_2016Driver() {
        this.szyLis = null;
    }

    public static int calculateCRC(final byte[] data) {
        int crc = 65535;
        for (final byte b : data) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; ++i) {
                if ((crc & 0x8000) != 0x0) {
                    crc = (crc << 1 ^ 0x1021);
                } else {
                    crc <<= 1;
                }
            }
        }
        return crc & 0xFFFF;
    }

    public boolean checkPropValue(final String groupn, final String itemn, final String strv, final StringBuilder failedr) {
        return false;
    }

    public DevDriver copyMe() {
        return new SZY206_2016Driver();
    }

    public String getName() {
        return "szy206_2016";
    }

    public String getTitle() {
        return "SZY206-2016";
    }

    public Class<? extends ConnPt> supportConnPtClass() {
        return (Class<? extends ConnPt>) ConnPtStream.class;
    }

    public boolean supportDevFinder() {
        return false;
    }

    public DevAddr getSupportAddr() {
        return SZY206_2016Driver.szyAddr;
    }

    public List<PropGroup> getPropGroupsForDevDef() {
        return null;
    }

    public List<PropGroup> getPropGroupsForCh(final UACh ch) {
        return null;
    }

    public List<PropGroup> getPropGroupsForDevInCh(final UADev d) {
        return null;
    }

    protected boolean initDriver(final StringBuilder failedr) throws Exception {
        this.szyLis = new SZYListener();
        return true;
    }

    protected void RT_onConnReady(final ConnPt cp, final UACh ch, final UADev dev) throws Exception {
    }

    protected void RT_onConnInvalid(final ConnPt cp, final UACh ch, final UADev dev) throws Exception {
    }

    protected boolean RT_runInLoop(final UACh ch, final UADev dev, final StringBuilder failedr) throws Exception {
        final ConnPtStream cpt = (ConnPtStream) this.getBindedConnPt();
        if (cpt == null) {
            return true;
        }
        if (!cpt.isConnReady()) {
            return true;
        }
        try {
            final InputStream inputs = cpt.getInputStream();
            final int dlen = inputs.available();
            if (dlen <= 0) {
                return true;
            }
            final byte[] bs = new byte[dlen];
            inputs.read(bs);
            this.szyLis.onRecvedData(bs, f -> this.onRecvedFrame(f));
        } catch (final ConnException se) {
            se.printStackTrace();
            if (SZY206_2016Driver.log.isDebugEnabled()) {
                SZY206_2016Driver.log.debug("RT_runInLoop err", (Throwable) se);
            }
            cpt.close();
        } catch (final Exception e) {
            e.printStackTrace();
            if (SZY206_2016Driver.log.isErrorEnabled()) {
                SZY206_2016Driver.log.debug("RT_runInLoop err", (Throwable) e);
            }
        }
        return true;
    }

    private void onRecvedFrame(final SZYFrame f) {
        System.out.println("recv f=" + f);
    }

    public boolean RT_writeVal(final UACh ch, final UADev dev, final UATag tag, final DevAddr da, final Object v) {
        return false;
    }

    public boolean RT_writeVals(final UACh ch, final UADev dev, final UATag[] tags, final DevAddr[] da, final Object[] v) {
        return false;
    }

    public class Crc8 {
        private byte[] table;

        public Crc8(final byte poly) {
            this.table = new byte[256];
            for (int i = 0; i < 256; ++i) {
                int temp = i;
                for (int j = 0; j < 8; ++j) {
                    if ((temp & 0x80) != 0x0) {
                        temp = (temp << 1 ^ poly);
                    } else {
                        temp <<= 1;
                    }
                }
                this.table[i] = (byte) temp;
            }
        }

        public byte calcCRC(final byte[] bytes) {
            byte crc = 0;
            if (bytes != null && bytes.length > 0) {
                for (final byte b : bytes) {
                    crc = this.table[crc ^ b];
                }
            }
            return crc;
        }
    }
}
