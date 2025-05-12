// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.s7.eth;

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

public class S7Model extends DevDriver.Model
{
    private LinkedHashMap<String, S7AddrDef> prefix2addrdef;
    
    public S7Model(final String name, final String t) {
        super(name, t);
        this.prefix2addrdef = new LinkedHashMap<String, S7AddrDef>();
    }
    
    public void setAddrDef(final S7AddrDef addr_def) {
        this.prefix2addrdef.put(addr_def.prefix, addr_def);
    }
    
    public List<String> listPrefix() {
        final ArrayList<String> rets = new ArrayList<String>();
        rets.addAll(this.prefix2addrdef.keySet());
        return rets;
    }
    
    public S7AddrDef getAddrDef(final String prefix) {
        return this.prefix2addrdef.get(prefix);
    }
    
    public S7Addr transAddr(final String prefix, final String num_str, final String bit_num, UAVal.ValTP vtp, final StringBuilder failedr) {
        final S7AddrDef def = this.prefix2addrdef.get(prefix);
        if (def == null) {
            failedr.append("no S7AddrDef found with prefix=" + prefix);
            return null;
        }
        S7AddrSeg addrseg = null;
        Integer iv = null;
        if (vtp != null) {
            for (final S7AddrSeg seg : def.segs) {
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
            for (final S7AddrSeg seg : def.segs) {
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
        final int bitnum = Integer.parseInt(bit_num);
        if (bitnum >= 0) {
            if (!addrseg.bBitPos) {
                failedr.append("not support bit access with prefix=" + prefix);
                return null;
            }
            switch (vtp) {
                case vt_int16:
                case vt_uint16: {
                    if (bitnum > 15) {
                        failedr.append("bit access must in [0,15]");
                        return null;
                    }
                    break;
                }
                case vt_int32:
                case vt_uint32: {
                    if (bitnum > 31) {
                        failedr.append("bit access must in [0,31]");
                        return null;
                    }
                    break;
                }
            }
        }
        String addrstr = String.valueOf(prefix) + num_str;
        if (bitnum >= 0) {
            addrstr = String.valueOf(addrstr) + ((bitnum < 9) ? (".0" + bitnum) : ("." + bitnum));
        }
        final S7Addr ret = S7Addr.parseS7Addr(addrstr, vtp, failedr);
        return ret;
    }
    
    public HashMap<S7AddrSeg, List<S7Addr>> filterAndSortAddrs(final String prefix, final List<S7Addr> addrs) {
        final S7AddrDef def = this.getAddrDef(prefix);
        if (def == null) {
            return null;
        }
        final HashMap<S7AddrSeg, List<S7Addr>> rets = new HashMap<S7AddrSeg, List<S7Addr>>();
        for (final S7Addr ma : addrs) {
            if (!prefix.equals(ma.getMemTp().name())) {
                continue;
            }
            final S7AddrSeg seg = def.findSeg(ma);
            if (seg == null) {
                continue;
            }
            List<S7Addr> ads = rets.get(seg);
            if (ads == null) {
                ads = new ArrayList<S7Addr>();
                rets.put(seg, ads);
            }
            ads.add(ma);
        }
        for (final List<S7Addr> ads2 : rets.values()) {
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
