

package cn.doraro.flexedge.driver.common;

import cn.doraro.flexedge.core.*;
import cn.doraro.flexedge.core.basic.PropGroup;
import cn.doraro.flexedge.core.basic.PropItem;
import cn.doraro.flexedge.core.basic.ValChker;
import cn.doraro.flexedge.core.conn.ConnPtStream;
import cn.doraro.flexedge.core.util.Lan;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;
import cn.doraro.flexedge.driver.common.modbus.sniffer.SnifferRTUCh;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ModbusDrvRTU extends DevDriver {
    private static final int SNIFFER_MODEL = 1;
    private static ILogger log;
    private static ModbusAddr msAddr;

    static {
        ModbusDrvRTU.log = LoggerManager.getLogger((Class) ModbusDrvRTU.class);
        ModbusDrvRTU.msAddr = new ModbusAddr();
    }

    protected ArrayList<ModbusDevItem> modbusDevItems;
    private boolean bSniffer;
    private SnifferRTUCh snifferCh;

    public ModbusDrvRTU() {
        this.bSniffer = false;
        this.snifferCh = null;
        this.modbusDevItems = new ArrayList<ModbusDevItem>();
    }

    public String getName() {
        return "modbus_rtu";
    }

    public String getTitle() {
        return "Modbus RTU";
    }

    public DevDriver copyMe() {
        return new ModbusDrvRTU();
    }

    public List<PropGroup> getPropGroupsForCh(final UACh ch) {
        final ArrayList<PropGroup> pgs = new ArrayList<PropGroup>();
        final Lan lan = Lan.getPropLangInPk((Class) this.getClass());
        final PropGroup gp = new PropGroup("modbus_ch", lan);
        final PropItem pi = new PropItem("run_model", lan, PropItem.PValTP.vt_int, false, new String[]{"Normal", "Sniffer"}, new Object[]{0, 1}, (Object) 0);
        gp.addPropItem(pi);
        pgs.add(gp);
        return pgs;
    }

    public List<PropGroup> getPropGroupsForDevInCh(final UADev d) {
        final ArrayList<PropGroup> pgs = new ArrayList<PropGroup>();
        final Lan lan = Lan.getPropLangInPk((Class) this.getClass());
        final PropGroup gp = new PropGroup("modbus_spk", lan);
        final PropItem pi = new PropItem("mdev_addr", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 1);
        pi.setValChker((ValChker) new ValChker<Number>() {
            public boolean checkVal(final Number v, final StringBuilder failedr) {
                final int vi = v.intValue();
                if (vi >= 1 && vi <= 255) {
                    return true;
                }
                failedr.append("modbus device address must between 1-255");
                return false;
            }
        });
        gp.addPropItem(pi);
        pgs.add(gp);
        return pgs;
    }

    public List<PropGroup> getPropGroupsForDevDef() {
        final ArrayList<PropGroup> pgs = new ArrayList<PropGroup>();
        PropGroup gp = null;
        final Lan lan = Lan.getPropLangInPk((Class) this.getClass());
        gp = new PropGroup("timing", lan);
        gp.addPropItem(new PropItem("scan_intv", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 100L));
        gp.addPropItem(new PropItem("req_to", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 1000));
        gp.addPropItem(new PropItem("failed_tryn", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 3));
        gp.addPropItem(new PropItem("recv_to", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 200));
        gp.addPropItem(new PropItem("inter_req", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 0));
        pgs.add(gp);
        gp = new PropGroup("auto_demotion", lan);
        gp.addPropItem(new PropItem("en", lan, PropItem.PValTP.vt_bool, false, new String[]{"Disabled", "Enabled"}, new Object[]{false, true}, (Object) false));
        gp.addPropItem(new PropItem("dm_tryc", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 3));
        gp.addPropItem(new PropItem("dm_ms", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 10000));
        gp.addPropItem(new PropItem("dm_no_req", lan, PropItem.PValTP.vt_bool, false, new String[]{"Disabled", "Enabled"}, new Object[]{false, true}, (Object) false));
        pgs.add(gp);
        gp = new PropGroup("data_access", lan);
        gp.addPropItem(new PropItem("z_b_addr", lan, PropItem.PValTP.vt_bool, false, (String[]) null, (Object[]) null, (Object) true));
        gp.addPropItem(new PropItem("z_b_bit_in_reg", lan, PropItem.PValTP.vt_bool, false, (String[]) null, (Object[]) null, (Object) true));
        gp.addPropItem(new PropItem("h_reg_b_mask_w", lan, PropItem.PValTP.vt_bool, false, (String[]) null, (Object[]) null, (Object) false));
        gp.addPropItem(new PropItem("f06_reg1_w", lan, PropItem.PValTP.vt_bool, false, (String[]) null, (Object[]) null, (Object) true));
        gp.addPropItem(new PropItem("f05_coil1_w", lan, PropItem.PValTP.vt_bool, false, (String[]) null, (Object[]) null, (Object) true));
        pgs.add(gp);
        gp = new PropGroup("data_encod", lan);
        gp.addPropItem(new PropItem("byte_ord_def", lan, PropItem.PValTP.vt_bool, false, (String[]) null, (Object[]) null, (Object) true));
        gp.addPropItem(new PropItem("fw_low32", lan, PropItem.PValTP.vt_bool, false, (String[]) null, (Object[]) null, (Object) true));
        gp.addPropItem(new PropItem("fdw_low64", lan, PropItem.PValTP.vt_bool, false, (String[]) null, (Object[]) null, (Object) false));
        gp.addPropItem(new PropItem("modicon_ord", lan, PropItem.PValTP.vt_bool, false, (String[]) null, (Object[]) null, (Object) false));
        pgs.add(gp);
        gp = new PropGroup("block_size", lan);
        gp.addPropItem(new PropItem("out_coils", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 32));
        gp.addPropItem(new PropItem("in_coils", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 32));
        gp.addPropItem(new PropItem("internal_reg", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 32));
        gp.addPropItem(new PropItem("holding", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 32));
        pgs.add(gp);
        gp = new PropGroup("framing", lan);
        gp.addPropItem(new PropItem("m_tcp_f", lan, PropItem.PValTP.vt_bool, false, (String[]) null, (Object[]) null, (Object) false));
        gp.addPropItem(new PropItem("leading_bs", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 0));
        gp.addPropItem(new PropItem("trailing_bs", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 0));
        pgs.add(gp);
        return pgs;
    }

    public boolean checkPropValue(final String groupn, final String itemn, final String strv, final StringBuilder failedr) {
        return false;
    }

    public Class<? extends ConnPt> supportConnPtClass() {
        return (Class<? extends ConnPt>) ConnPtStream.class;
    }

    public boolean supportDevFinder() {
        return false;
    }

    public DevAddr getSupportAddr() {
        return ModbusDrvRTU.msAddr;
    }

    protected boolean initDriver(final StringBuilder failedr) throws Exception {
        final Object pv = this.getBelongToCh().getPropValue("modbus_ch", "run_model");
        if (pv != null && pv instanceof Number) {
            this.bSniffer = (((Number) pv).intValue() == 1);
        }
        final List<UADev> devs = this.getBelongToCh().getDevs();
        final ArrayList<ModbusDevItem> mdis = new ArrayList<ModbusDevItem>();
        for (final UADev dev : devs) {
            final ModbusDevItem mdi = new ModbusDevItem(dev);
            final StringBuilder devfr = new StringBuilder();
            if (!mdi.init(devfr)) {
                continue;
            }
            mdis.add(mdi);
        }
        this.modbusDevItems = mdis;
        if (this.modbusDevItems.size() <= 0) {
            failedr.append("no modbus cmd inited in driver");
            return false;
        }
        if (this.bSniffer) {
            this.snifferCh = new SnifferRTUCh();
        }
        return true;
    }

    protected void RT_onConnReady(final ConnPt cp, final UACh ch, final UADev dev) {
    }

    protected void RT_onConnInvalid(final ConnPt cp, final UACh ch, final UADev dev) {
    }

    protected long getRunInterval() {
        if (this.bSniffer) {
            return 10L;
        }
        return super.getRunInterval();
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
            if (this.bSniffer) {
                final InputStream inputs = cpt.getInputStream();
                final int dlen = inputs.available();
                if (dlen <= 0) {
                    return true;
                }
                final byte[] bs = new byte[dlen];
                inputs.read(bs);
                this.snifferCh.onSniffedData(bs, sc -> {
                    this.modbusDevItems.iterator();
                    final Iterator iterator3;
                    while (iterator3.hasNext()) {
                        final ModbusDevItem mdi3 = iterator3.next();
                        mdi3.onSnifferCmd(sc);
                    }
                    return;
                });
            } else {
                for (final ModbusDevItem mdi : this.modbusDevItems) {
                    mdi.doModbusCmd(cpt);
                }
                this.checkConnBroken(cpt);
            }
        } catch (final ConnException se) {
            se.printStackTrace();
            if (ModbusDrvRTU.log.isDebugEnabled()) {
                ModbusDrvRTU.log.debug("RT_runInLoop err", (Throwable) se);
            }
            cpt.close();
            for (final ModbusDevItem mdi2 : this.modbusDevItems) {
                mdi2.doModbusCmdErr();
            }
        } catch (final Exception e) {
            e.printStackTrace();
            if (ModbusDrvRTU.log.isErrorEnabled()) {
                ModbusDrvRTU.log.debug("RT_runInLoop err", (Throwable) e);
            }
        }
        return true;
    }

    private void checkConnBroken(final ConnPtStream cpt) throws Exception {
        long lastreadok = -1L;
        for (final ModbusDevItem mdi : this.modbusDevItems) {
            final long tmpdt = mdi.getLastReadOkDT();
            if (tmpdt > 0L && tmpdt > lastreadok) {
                lastreadok = tmpdt;
            }
        }
        if (lastreadok > 0L) {
            final ConnPtStream cpts = (ConnPtStream) this.getBelongToCh().getConnPt();
            final long read_no_to = cpts.getReadNoDataTimeout();
            if (read_no_to > 0L && System.currentTimeMillis() - lastreadok > read_no_to) {
                if (ModbusDrvRTU.log.isDebugEnabled()) {
                    ModbusDrvRTU.log.debug("ModbusDrvRT last read ok timeout with " + read_no_to + ",connpt [" + cpts.getTitle() + "] will be closed");
                }
                cpt.close();
            }
        }
    }

    private ModbusDevItem getDevItem(final UADev dev) {
        for (final ModbusDevItem mdi : this.modbusDevItems) {
            if (mdi.getUADev().equals((Object) dev)) {
                return mdi;
            }
        }
        return null;
    }

    public boolean RT_writeVal(final UACh ch, final UADev dev, final UATag tag, final DevAddr da, final Object v) {
        final ModbusDevItem mdi = this.getDevItem(dev);
        return mdi != null && mdi.RT_writeVal(da, v);
    }

    public boolean RT_writeVals(final UACh ch, final UADev dev, final UATag[] tags, final DevAddr[] da, final Object[] v) {
        throw new RuntimeException("no impl");
    }
}
