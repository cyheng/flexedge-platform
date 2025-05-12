// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common;

import cn.doraro.flexedge.core.ConnPt;
import cn.doraro.flexedge.core.DevAddr;
import cn.doraro.flexedge.core.DevDriver;
import cn.doraro.flexedge.core.basic.IConnEndPoint;
import cn.doraro.flexedge.core.basic.PropGroup;
import cn.doraro.flexedge.core.basic.PropItem;
import cn.doraro.flexedge.core.conn.ConnPtStream;
import cn.doraro.flexedge.core.util.Lan;

import java.util.ArrayList;
import java.util.List;

public abstract class ModbusDrv extends DevDriver {
    private static ModbusAddr msAddr;

    static {
        ModbusDrv.msAddr = new ModbusAddr();
    }

    public Class<? extends ConnPt> supportConnPtClass() {
        return (Class<? extends ConnPt>) ConnPtStream.class;
    }

    public List<PropGroup> getPropGroupsForDevDef() {
        final ArrayList<PropGroup> pgs = new ArrayList<PropGroup>();
        PropGroup gp = null;
        final Lan lan = Lan.getPropLangInPk((Class) this.getClass());
        gp = new PropGroup("timing", lan);
        gp.addPropItem(new PropItem("conn_tryc", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 3));
        gp.addPropItem(new PropItem("req_to", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 1000));
        gp.addPropItem(new PropItem("inter_req", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 100));
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

    public DevAddr getSupportAddr() {
        return ModbusDrv.msAddr;
    }

    public boolean checkPropValue(final String groupn, final String itemn, final String strv, final StringBuilder failedr) {
        return true;
    }

    public abstract IConnEndPoint getConnEndPoint();
}
