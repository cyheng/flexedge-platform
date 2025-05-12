// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.mitsubishi.fx;

import cn.doraro.flexedge.core.DevAddr;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import cn.doraro.flexedge.core.UAVal;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import cn.doraro.flexedge.core.DevDriver;

public class MCModel extends DevDriver.Model
{
    public static final int TP_X_START = 128;
    public static final int TP_S_START = 0;
    public static final int TP_Y_START = 160;
    public static final int TP_X_FORCE_ONOFF = 1024;
    public static final int TP_Y_FORCE_ONOFF = 1280;
    public static final int TP_S_FORCE_ONOFF = 0;
    public static final int TP_TC_START = 192;
    public static final int TP_TCOIL_START = 704;
    public static final int TP_TV_START = 2048;
    public static final int TP_TR_START = 1216;
    public static final int TP_T_FORCE_ONOFF = 1536;
    public static final int TP_CC_START = 448;
    public static final int TP_CCOIL_START = 960;
    public static final int TP_CR_START = 1472;
    public static final int TP_C_FORCE_ONOFF = 3584;
    public static final int TP_CV16_START = 2560;
    public static final int TP_CV32_START = 3072;
    public static final int TP_MC_START = 256;
    public static final int TP_MS_START = 480;
    public static final int TP_PM_START = 768;
    public static final int TP_M_FORCE_ONOFF = 2048;
    public static final int TP_MS_FORCE_ONOFF = 3840;
    public static final int TP_OC_START = 960;
    public static final int TP_RC_START = 1472;
    public static final int TP_OT_START = 704;
    public static final int TP_RT_START = 1216;
    public static final int TP_PY_START = 672;
    public static final int TP_D_SPEC_START = 3584;
    public static final int TP_D_START = 4096;
    private LinkedHashMap<String, FxAddrDef> prefix2addrdef;
    
    public MCModel(final String name, final String t) {
        super(name, t);
        this.prefix2addrdef = new LinkedHashMap<String, FxAddrDef>();
    }
    
    public void setAddrDef(final FxAddrDef addr_def) {
        this.prefix2addrdef.put(addr_def.prefix, addr_def);
    }
    
    public List<String> listPrefix() {
        final ArrayList<String> rets = new ArrayList<String>();
        rets.addAll(this.prefix2addrdef.keySet());
        return rets;
    }
    
    public FxAddrDef getAddrDef(final String prefix) {
        return this.prefix2addrdef.get(prefix);
    }
    
    public FxAddr transAddr(final String prefix, final String num_str, UAVal.ValTP vtp, final StringBuilder failedr) {
        final FxAddrDef def = this.prefix2addrdef.get(prefix);
        if (def == null) {
            failedr.append("no FxAddrDef found with prefix=" + prefix);
            return null;
        }
        FxAddrSeg addrseg = null;
        Integer iv = null;
        if (vtp != null) {
            for (final FxAddrSeg seg : def.segs) {
                if (seg.matchValTP(vtp)) {
                    iv = seg.matchAddr(num_str);
                    if (iv != null) {
                        addrseg = seg;
                        break;
                    }
                    continue;
                }
            }
            if (addrseg == null) {
                failedr.append("no AddrSeg match with ValTP=" + vtp.name());
                return null;
            }
        }
        else {
            for (final FxAddrSeg seg : def.segs) {
                iv = seg.matchAddr(num_str);
                if (iv != null) {
                    addrseg = seg;
                    break;
                }
            }
            if (addrseg == null) {
                failedr.append("no AddrSeg match with num str=" + num_str);
                return null;
            }
            vtp = addrseg.valTPs[0];
        }
        if (iv == null) {
            failedr.append("no AddrSeg match with ValTP=" + vtp.name());
            return null;
        }
        final FxAddr ret = new FxAddr(String.valueOf(prefix) + num_str, vtp, this, prefix, iv, addrseg.bValBit, addrseg.digitNum, addrseg.bOctal).asDef(def, addrseg);
        ret.setWritable(addrseg.bWrite);
        return ret;
    }
    
    public HashMap<FxAddrSeg, List<FxAddr>> filterAndSortAddrs(final String prefix, final List<FxAddr> addrs) {
        final FxAddrDef def = this.getAddrDef(prefix);
        if (def == null) {
            return null;
        }
        final HashMap<FxAddrSeg, List<FxAddr>> rets = new HashMap<FxAddrSeg, List<FxAddr>>();
        for (final FxAddr ma : addrs) {
            if (!prefix.equals(ma.getPrefix())) {
                continue;
            }
            final FxAddrSeg seg = def.findSeg(ma);
            if (seg == null) {
                continue;
            }
            List<FxAddr> ads = rets.get(seg);
            if (ads == null) {
                ads = new ArrayList<FxAddr>();
                rets.put(seg, ads);
            }
            ads.add(ma);
        }
        for (final List<FxAddr> ads2 : rets.values()) {
            Collections.sort(ads2);
        }
        return rets;
    }
    
    public List<DevAddr.IAddrDef> getAddrDefs() {
        final ArrayList<DevAddr.IAddrDef> rets = new ArrayList<DevAddr.IAddrDef>();
        rets.addAll((Collection<? extends DevAddr.IAddrDef>)this.prefix2addrdef.values());
        return rets;
    }
}
