// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.omron.hostlink.fins;

import cn.doraro.flexedge.core.DevDriver;
import cn.doraro.flexedge.core.UADev;
import cn.doraro.flexedge.core.basic.PropGroup;
import cn.doraro.flexedge.core.basic.PropItem;
import cn.doraro.flexedge.core.basic.ValChker;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.Lan;

import java.util.ArrayList;
import java.util.List;

public class HLFinsDriverSerial extends HLFinsDriver {
    public boolean checkPropValue(final String groupn, final String itemn, final String strv, final StringBuilder failedr) {
        return false;
    }

    public DevDriver copyMe() {
        return new HLFinsDriverSerial();
    }

    public String getName() {
        return "hostlink_fins_ser";
    }

    public String getTitle() {
        return "Hostlink FINS Serial";
    }

    @Override
    public List<PropGroup> getPropGroupsForDevInCh(final UADev d) {
        final ArrayList<PropGroup> pgs = new ArrayList<PropGroup>();
        final Lan lan = Lan.getPropLangInPk((Class) this.getClass());
        final PropGroup gp = new PropGroup("fins_net", lan);
        final PropItem pi = new PropItem("dev_net_id", lan, PropItem.PValTP.vt_str, false, (String[]) null, (Object[]) null, (Object) "0.0.0");
        gp.addPropItem(pi);
        pi.setValChker((ValChker) new ValChker<String>() {
            public boolean checkVal(final String v, final StringBuilder failedr) {
                if (Convert.isNullOrEmpty(v)) {
                    failedr.append(String.valueOf(lan.g("pi_dev_net_id")) + " " + lan.g("cannot_empty"));
                    return false;
                }
                final List<String> ss = Convert.splitStrWith(v, ".");
                if (ss.size() != 3) {
                    failedr.append(String.valueOf(lan.g("pi_dev_net_id")) + lan.g("fmt_be") + " x.x.x " + lan.g("num_pos"));
                    return false;
                }
                for (final String s : ss) {
                    try {
                        final int iv = Convert.parseToInt32(s, -1);
                        if (iv < 0) {
                            failedr.append(String.valueOf(lan.g("pi_dev_net_id")) + lan.g("fmt_be") + " x.x.x (0.1.0)," + lan.g("num_pos"));
                            return false;
                        }
                        continue;
                    } catch (final Exception ee) {
                        failedr.append(String.valueOf(lan.g("pi_dev_net_id")) + lan.g("fmt_be") + " x.x.x (0.1.0)," + lan.g("num_pos"));
                        return false;
                    }
                }
                return true;
            }
        });
        pgs.add(gp);
        return pgs;
    }
}
