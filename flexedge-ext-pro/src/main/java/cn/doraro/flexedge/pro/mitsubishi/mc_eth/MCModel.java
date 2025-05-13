

package cn.doraro.flexedge.pro.mitsubishi.mc_eth;

import cn.doraro.flexedge.core.DevAddr;
import cn.doraro.flexedge.core.DevDriver;
import cn.doraro.flexedge.core.UAVal;

import java.util.*;

public class MCModel extends DevDriver.Model {
    private LinkedHashMap<String, MCAddrDef> prefix2addrdef;

    public MCModel(final String name, final String t) {
        super(name, t);
        this.prefix2addrdef = new LinkedHashMap<String, MCAddrDef>();
    }

    public void setAddrDef(final MCAddrDef addr_def) {
        this.prefix2addrdef.put(addr_def.prefix, addr_def);
    }

    public List<String> listPrefix() {
        final ArrayList<String> rets = new ArrayList<String>();
        rets.addAll(this.prefix2addrdef.keySet());
        return rets;
    }

    public MCAddrDef getAddrDef(final String prefix) {
        return this.prefix2addrdef.get(prefix);
    }

    public MCAddr transAddr(final String prefix, final String num_str, final String bit_num, UAVal.ValTP vtp, final StringBuilder failedr) {
        final MCAddrDef def = this.prefix2addrdef.get(prefix);
        if (def == null) {
            failedr.append("no MCAddrDef found with prefix=" + prefix);
            return null;
        }
        MCAddrSeg addrseg = null;
        Integer iv = null;
        if (vtp != null) {
            for (final MCAddrSeg seg : def.segs) {
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
        } else {
            for (final MCAddrSeg seg : def.segs) {
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
        final MCAddr ret = new MCAddr(addrstr, vtp, this, prefix, iv, addrseg.digitNum, bitnum).asDef(def, addrseg);
        ret.setWritable(addrseg.bWrite);
        return ret;
    }

    public HashMap<MCAddrSeg, List<MCAddr>> filterAndSortAddrs(final String prefix, final List<MCAddr> addrs) {
        final MCAddrDef def = this.getAddrDef(prefix);
        if (def == null) {
            return null;
        }
        final HashMap<MCAddrSeg, List<MCAddr>> rets = new HashMap<MCAddrSeg, List<MCAddr>>();
        for (final MCAddr ma : addrs) {
            if (!prefix.equals(ma.getPrefix())) {
                continue;
            }
            final MCAddrSeg seg = def.findSeg(ma);
            if (seg == null) {
                continue;
            }
            List<MCAddr> ads = rets.get(seg);
            if (ads == null) {
                ads = new ArrayList<MCAddr>();
                rets.put(seg, ads);
            }
            ads.add(ma);
        }
        for (final List<MCAddr> ads2 : rets.values()) {
            Collections.sort(ads2);
        }
        return rets;
    }

    public List<DevAddr.IAddrDef> getAddrDefs() {
        final ArrayList<DevAddr.IAddrDef> rets = new ArrayList<DevAddr.IAddrDef>();
        rets.addAll((Collection<? extends DevAddr.IAddrDef>) this.prefix2addrdef.values());
        return rets;
    }
}
