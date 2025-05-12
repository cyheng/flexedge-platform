// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common.clh;

import cn.doraro.flexedge.core.UACh;
import cn.doraro.flexedge.core.UATag;
import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.conn.ConnPtStream;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.driver.common.CmdLineDrv;
import cn.doraro.flexedge.driver.common.CmdLineHandler;

public class ApcSmartUPS extends CmdLineHandler {
    boolean bConnOk;
    boolean bJustConn;
    private UATag tagTemp;
    private UATag tagLowBatteryV;
    private UATag tagUpsOverload;
    private UATag tagUpsUseBattery;
    private UATag tagUpsOnline;
    private UATag tagBatteryStdV;
    private UATag tagBatteryV;
    private UATag tagBatteryCap;

    public ApcSmartUPS() {
        this.bConnOk = false;
        this.tagTemp = null;
        this.tagLowBatteryV = null;
        this.tagUpsOverload = null;
        this.tagUpsUseBattery = null;
        this.tagUpsOnline = null;
        this.tagBatteryStdV = null;
        this.tagBatteryV = null;
        this.tagBatteryCap = null;
        this.bJustConn = false;
    }

    @Override
    public String getName() {
        return "apc_smart_ups";
    }

    @Override
    public String getTitle() {
        return "APC Smart UPS";
    }

    @Override
    public String getDesc() {
        return "APC Smart UPS RS232";
    }

    @Override
    protected CmdLineHandler copyMe() {
        return new ApcSmartUPS();
    }

    @Override
    protected int getRecvMaxLen() {
        return 100;
    }

    public boolean init(final CmdLineDrv cld, final StringBuilder sb) throws Exception {
        super.init(cld, sb);
        final UACh ch = this.belongTo.getBelongToCh();
        this.tagTemp = ch.getOrAddTag("temp", "Temperature", "UPS temperature", UAVal.ValTP.vt_float, false);
        this.tagLowBatteryV = ch.getOrAddTag("low_b_v", "Low battery voltage alarm", "Low battery voltage alarm", UAVal.ValTP.vt_bool, false);
        this.tagUpsOverload = ch.getOrAddTag("st_overload", "UPS Overloaded Running", "Ups Overloaded Running", UAVal.ValTP.vt_bool, false);
        this.tagUpsUseBattery = ch.getOrAddTag("st_use_battery", "UPS Using battery inverter", "Using battery inverter", UAVal.ValTP.vt_bool, false);
        this.tagUpsOnline = ch.getOrAddTag("st_online", "UPS Using Online", "Using Online", UAVal.ValTP.vt_bool, false);
        this.tagBatteryStdV = ch.getOrAddTag("battery_stdv", "Battery standard  voltage", "Battery standard  voltage", UAVal.ValTP.vt_int32, false);
        this.tagBatteryV = ch.getOrAddTag("battery_v", "Battery voltage", "Battery voltage", UAVal.ValTP.vt_float, false);
        this.tagBatteryCap = ch.getOrAddTag("battery_cap", "Battery capacity", "Battery capacity", UAVal.ValTP.vt_float, false);
        if (ch.isDirty()) {
            ch.save();
        }
        return true;
    }

    public void RT_onConned(final ConnPtStream cpt) throws Exception {
        super.RT_onConned(cpt);
        this.bJustConn = true;
    }

    public void RT_onDisconn(final ConnPtStream cpt) throws Exception {
        super.RT_onDisconn(cpt);
        this.bConnOk = false;
    }

    @Override
    protected boolean RT_useNoWait() {
        return false;
    }

    @Override
    public void RT_runInLoop(final ConnPtStream cpt) throws Exception {
        if (this.bJustConn) {
            this.sendRecvSyn("Y\r\n", (bsucc, ret, error) -> {
                if (bsucc) {
                    this.bJustConn = false;
                    this.bConnOk = "SM\r\n".equals(ret);
                } else {
                    this.belongTo.RT_fireDrvWarn("Device has not SM response");
                }
            });
            return;
        }
        if (!this.bConnOk) {
            return;
        }
        this.sendRecvSyn("Q\r\n", (bsucc, ret, error) -> {
            if (bsucc) {
                try {
                    final int intv = Integer.parseInt(ret.trim());
                    final boolean low_b = (intv & 0x40) > 0;
                    final boolean overload = (intv & 0x20) > 0;
                    final boolean use_b = (intv & 0x10) > 0;
                    final boolean use_online = (intv & 0x8) > 0;
                    this.tagLowBatteryV.RT_setValRaw((Object) low_b);
                    this.tagUpsOverload.RT_setValRaw((Object) overload);
                    this.tagUpsUseBattery.RT_setValRaw((Object) use_b);
                    this.tagUpsOnline.RT_setValRaw((Object) use_online);
                } catch (final Exception eee) {
                    final String err = "reponse error:" + eee.getMessage();
                    this.tagLowBatteryV.RT_setValErr(err);
                    this.tagUpsOverload.RT_setValErr(err);
                    this.tagUpsUseBattery.RT_setValErr(err);
                    this.tagUpsOnline.RT_setValErr(err);
                }
            } else {
                final String err2 = "reponse error:" + error;
                this.tagLowBatteryV.RT_setValErr(err2);
                this.tagUpsOverload.RT_setValErr(err2);
                this.tagUpsUseBattery.RT_setValErr(err2);
                this.tagUpsOnline.RT_setValErr(err2);
            }
            return;
        });
        this.sendCmdSyn(this.tagTemp, "C", "C temp");
        this.sendCmdSyn(this.tagBatteryStdV, "g", "Battery standard voltage");
        this.sendCmdSyn(this.tagBatteryV, "B", "Battery voltage");
        this.sendCmdSyn(this.tagBatteryCap, "f", "Battery capacity");
    }

    private void sendCmdSyn(final UATag tag, final String cmd, final String title) throws Exception {
        this.sendRecvSyn(String.valueOf(cmd) + "\r\n", (bsucc, ret, error) -> {
            if (bsucc) {
                try {
                    ret = ret.trim();
                    final UAVal.ValTP vt = uaTag.getValTp();
                    Object v = null;
                    if (vt == UAVal.ValTP.vt_bool) {
                        v = ("true".equalsIgnoreCase(ret) || "1".equals(ret));
                    } else if (vt == UAVal.ValTP.vt_float) {
                        v = Float.parseFloat(ret);
                    } else if (vt == UAVal.ValTP.vt_int32) {
                        v = Integer.parseInt(ret);
                    }
                    if (v != null) {
                        uaTag.RT_setValRaw(v);
                    }
                } catch (final Exception eee) {
                    uaTag.RT_setValErr("Response " + s + " err:" + eee.getMessage(), eee);
                }
            } else {
                uaTag.RT_setValErr("Response " + s + " error:" + error, (Exception) null);
            }
        });
    }

    @Override
    public void RT_onRecved(String cmd) {
        if (Convert.isNullOrEmpty(cmd)) {
            return;
        }
        System.out.println("on recv cmd=" + cmd);
        cmd = cmd.trim();
        final String s;
        switch (s = cmd) {
            case "!": {
                break;
            }
            case "%": {
                this.tagLowBatteryV.RT_setValRaw((Object) true);
                break;
            }
            default:
                break;
        }
    }
}
