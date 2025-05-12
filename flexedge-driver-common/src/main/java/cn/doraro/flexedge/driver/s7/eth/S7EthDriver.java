// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.s7.eth;

import cn.doraro.flexedge.core.*;
import cn.doraro.flexedge.core.basic.PropGroup;
import cn.doraro.flexedge.core.basic.PropItem;
import cn.doraro.flexedge.core.basic.ValChker;
import cn.doraro.flexedge.core.conn.ConnPtTcpClient;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.Lan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class S7EthDriver extends DevDriver {
    private static final String M_S7_200 = "s7-200";
    private static final String M_S7_200_SMART = "s7-200-smart";
    private static final String M_S7_300 = "s7-300";
    private static final String M_S7_400 = "s7-400";
    private static final String M_S7_1200 = "s7-1200";
    private static final String M_S7_1500 = "s7-1500";
    private static final List<DevDriver.Model> devms;
    private static S7Addr s7addr;

    static {
        devms = Arrays.asList(new DevDriver.Model("s7-200", "S7-200"), new DevDriver.Model("s7-200-smart", "S7-200 SMART"), new DevDriver.Model("s7-300", "S7-300"), new DevDriver.Model("s7-400", "S7-400"), new DevDriver.Model("s7-1200", "S7-1200"), new DevDriver.Model("s7-1500", "S7-1500"));
        S7EthDriver.s7addr = new S7Addr("", null);
    }

    private final S7MsgISOCR msgISOCR;
    private HashMap<UADev, S7TcpConn> dev2conn;
    private HashMap<UADev, S7DevItem> dev2item;

    public S7EthDriver() {
        this.dev2conn = new HashMap<UADev, S7TcpConn>();
        this.msgISOCR = new S7MsgISOCR();
        this.dev2item = new HashMap<UADev, S7DevItem>();
    }

    private static int transStr2TSAP(final String v, final StringBuilder failedr) {
        if (Convert.isNullOrEmpty(v)) {
            failedr.append("TSAP cannot be null");
            return -1;
        }
        try {
            return Integer.parseInt(v, 16) & 0xFFFF;
        } catch (final Exception e) {
            failedr.append("TSAP must be hex string with like 4D57");
            return -1;
        }
    }

    public boolean checkPropValue(final String groupn, final String itemn, final String strv, final StringBuilder failedr) {
        return true;
    }

    public DevDriver copyMe() {
        return new S7EthDriver();
    }

    public String getName() {
        return "s7_tcpip_eth";
    }

    public String getTitle() {
        return "Siemens TCP/IP Ethernet";
    }

    public Class<? extends ConnPt> supportConnPtClass() {
        return (Class<? extends ConnPt>) ConnPtTcpClient.class;
    }

    public List<DevDriver.Model> getDevModels() {
        return S7EthDriver.devms;
    }

    public boolean isConnPtToDev() {
        return true;
    }

    public boolean supportDevFinder() {
        return false;
    }

    public List<PropGroup> getPropGroupsForDevDef() {
        return null;
    }

    public List<PropGroup> getPropGroupsForCh(final UACh ch) {
        return null;
    }

    public List<PropGroup> getPropGroupsForDevInCh(final UADev d) {
        final ArrayList<PropGroup> pgs = new ArrayList<PropGroup>();
        final Lan lan = Lan.getPropLangInPk((Class) this.getClass());
        final PropGroup gp_nor = new PropGroup("s7_comm_pm", lan);
        final PropItem pi_rack = new PropItem("rack", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 0);
        pi_rack.setValChker((ValChker) new ValChker<Number>() {
            public boolean checkVal(final Number v, final StringBuilder failedr) {
                final int vi = v.intValue();
                if (vi >= 0 && vi <= 7) {
                    return true;
                }
                failedr.append("CPU Rack must between 0-7");
                return false;
            }
        });
        gp_nor.addPropItem(pi_rack);
        final PropItem pi_slot = new PropItem("slot", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 1);
        pi_slot.setValChker((ValChker) new ValChker<Number>() {
            public boolean checkVal(final Number v, final StringBuilder failedr) {
                final int vi = v.intValue();
                if (vi >= 1 && vi <= 255) {
                    return true;
                }
                failedr.append("CPU Slot must between 1-31");
                return false;
            }
        });
        gp_nor.addPropItem(pi_slot);
        final ValChker<String> tsapchk = (ValChker<String>) new ValChker<String>() {
            public boolean checkVal(final String v, final StringBuilder failedr) {
                final int r = transStr2TSAP(v, failedr);
                return r > 0;
            }
        };
        final PropGroup gp_tsap = new PropGroup("s7_comm_pm", lan);
        final PropItem pi_local_tsap = new PropItem("tsap_local", lan, PropItem.PValTP.vt_str, false, (String[]) null, (Object[]) null, (Object) "4D57");
        pi_local_tsap.setValChker((ValChker) tsapchk);
        gp_tsap.addPropItem(pi_local_tsap);
        final PropItem pi_remote_tsap = new PropItem("tsap_remote", lan, PropItem.PValTP.vt_str, false, (String[]) null, (Object[]) null, (Object) "4D57");
        pi_remote_tsap.setValChker((ValChker) tsapchk);
        gp_tsap.addPropItem(pi_remote_tsap);
        final String devModel;
        switch ((devModel = d.getDevModel()).hashCode()) {
            case -950431095: {
                if (!devModel.equals("s7-200")) {
                    return pgs;
                }
                pgs.add(gp_tsap);
                return pgs;
            }
            case -950430134: {
                if (!devModel.equals("s7-300")) {
                    return pgs;
                }
                pi_slot.setDefaultVal((Object) 2);
                break;
            }
            case -950429173: {
                if (!devModel.equals("s7-400")) {
                    return pgs;
                }
                break;
            }
            case 283865477: {
                if (!devModel.equals("s7-200-smart")) {
                    return pgs;
                }
                pgs.add(gp_tsap);
                pi_local_tsap.setDefaultVal((Object) "201");
                pi_remote_tsap.setDefaultVal((Object) "201");
                return pgs;
            }
            case 601379306: {
                if (!devModel.equals("s7-1200")) {
                    return pgs;
                }
                break;
            }
            case 601382189: {
                if (!devModel.equals("s7-1500")) {
                    return pgs;
                }
                break;
            }
        }
        pgs.add(gp_nor);
        return pgs;
    }

    public DevAddr getSupportAddr() {
        return S7EthDriver.s7addr;
    }

    protected boolean initDriver(final StringBuilder failedr) throws Exception {
        super.initDriver(failedr);
        final List<UADev> devs = this.getBelongToCh().getDevs();
        final HashMap<UADev, S7DevItem> d2i = new HashMap<UADev, S7DevItem>();
        for (final UADev dev : devs) {
            final S7DevItem mdi = new S7DevItem(this, dev);
            final StringBuilder devfr = new StringBuilder();
            if (!mdi.init(devfr)) {
                continue;
            }
            d2i.put(dev, mdi);
        }
        this.dev2item = d2i;
        if (this.dev2item.size() <= 0) {
            failedr.append("no s7 device inited in driver");
            return false;
        }
        return true;
    }

    S7DevItem getDevItem(final UADev d) {
        return this.dev2item.get(d);
    }

    protected void RT_onConnReady(final ConnPt cp, final UACh ch, final UADev dev) {
        final ConnPtTcpClient tcpc = (ConnPtTcpClient) cp;
        final S7TcpConn conn = new S7TcpConn(tcpc);
        if ("s7-200".equals(dev.getDevModel())) {
            final String tsap_l = dev.getOrDefaultPropValueStr("s7_comm_pm", "tsap_local", "4D57");
            final String tsap_r = dev.getOrDefaultPropValueStr("s7_comm_pm", "tsap_remote", "4D57");
            final StringBuilder failedr = new StringBuilder();
            final int tsap_loc = transStr2TSAP(tsap_l, failedr);
            final int tsap_rrr = transStr2TSAP(tsap_r, failedr);
            conn.withTSAP(tsap_loc, tsap_rrr);
        } else if ("s7-200-smart".equals(dev.getDevModel())) {
            final String tsap_l = dev.getOrDefaultPropValueStr("s7_comm_pm", "tsap_local", "201");
            final String tsap_r = dev.getOrDefaultPropValueStr("s7_comm_pm", "tsap_remote", "201");
            final StringBuilder failedr = new StringBuilder();
            final int tsap_loc = transStr2TSAP(tsap_l, failedr);
            final int tsap_rrr = transStr2TSAP(tsap_r, failedr);
            conn.withTSAP(tsap_loc, tsap_rrr);
        } else {
            final int rack = dev.getOrDefaultPropValueInt("s7_comm_pm", "rack", 0);
            final int slot = dev.getOrDefaultPropValueInt("s7_comm_pm", "slot", 1);
            conn.withRackSlot(rack, slot);
        }
        try {
            this.msgISOCR.processByConn(conn);
            synchronized (this) {
                this.dev2conn.put(dev, conn);
            }
        } catch (final Exception exp) {
            exp.printStackTrace();
            conn.close();
        }
    }

    protected synchronized void RT_onConnInvalid(final ConnPt cp, final UACh ch, final UADev dev) {
        this.dev2conn.remove(dev);
        try {
            final ConnPtTcpClient tcpc = (ConnPtTcpClient) cp;
            tcpc.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    protected boolean RT_runInLoop(final UACh ch, final UADev dev, final StringBuilder failedr) throws Exception {
        S7TcpConn s7conn = null;
        synchronized (this) {
            s7conn = this.dev2conn.get(dev);
        }
        if (s7conn == null) {
            return true;
        }
        final S7DevItem ditem = this.dev2item.get(dev);
        if (ditem == null) {
            return true;
        }
        ditem.doCmd(s7conn);
        return true;
    }

    private void test(final UACh ch, final UADev dev, final S7TcpConn s7conn) throws S7Exception, IOException {
        System.out.println(String.valueOf(dev.getName()) + " S7 run in loop");
        byte[] bs = new byte[4];
        S7MsgRead.readArea(s7conn, S7MemTp.DB, 200, 0, 4, bs);
        System.out.println("read db data=" + Convert.byteArray2HexStr(bs));
        bs = new byte[]{0};
        S7MsgRead.readArea(s7conn, S7MemTp.I, 0, 1, 1, bs);
        System.out.println("read IB1 data=" + Convert.byteArray2HexStr(bs));
        bs = new byte[]{0};
        S7MsgRead.readArea(s7conn, S7MemTp.Q, 0, 1, 1, bs);
        System.out.println("read QB1 data=" + Convert.byteArray2HexStr(bs));
    }

    public boolean RT_writeVal(final UACh ch, final UADev dev, final UATag tag, final DevAddr da, final Object v) {
        final S7DevItem mdi = this.getDevItem(dev);
        return mdi != null && mdi.RT_writeVal(da, v);
    }

    public boolean RT_writeVals(final UACh ch, final UADev dev, final UATag[] tags, final DevAddr[] da, final Object[] v) {
        return false;
    }
}
