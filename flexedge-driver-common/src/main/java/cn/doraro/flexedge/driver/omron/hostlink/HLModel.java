

package cn.doraro.flexedge.driver.omron.hostlink;

import cn.doraro.flexedge.core.DevAddr;
import cn.doraro.flexedge.core.DevDriver;
import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.driver.omron.fins.FinsMode;

import java.util.*;

public abstract class HLModel extends DevDriver.Model {
    static ArrayList<DevDriver.Model> models;

    static {
        (HLModel.models = new ArrayList<DevDriver.Model>()).add(new HLModel_CS1());
        HLModel.models.add(new HLModel_CJ1());
        HLModel.models.add(new HLModel_CJ2());
    }

    private LinkedHashMap<String, HLAddrDef> prefix2addrdef;

    public HLModel(final String name, final String t) {
        super(name, t);
        this.prefix2addrdef = new LinkedHashMap<String, HLAddrDef>();
    }

    public static List<DevDriver.Model> getModelsAll() {
        return HLModel.models;
    }

    public void setAddrDef(final HLAddrDef addr_def) {
        this.prefix2addrdef.put(addr_def.prefix, addr_def);
    }

    public List<String> listPrefix() {
        final ArrayList<String> rets = new ArrayList<String>();
        rets.addAll(this.prefix2addrdef.keySet());
        return rets;
    }

    public HLAddrDef getAddrDef(final String prefix) {
        return this.prefix2addrdef.get(prefix);
    }

    HLAddr transAddr(final String prefix, final String addr_str, final String bit_str, final UAVal.ValTP vtp, final StringBuilder failedr) {
        final int addr_n = Convert.parseToInt32(addr_str, -1);
        if (addr_n < 0) {
            failedr.append("invalid addr " + addr_str);
            return null;
        }
        final int bit_n = Convert.parseToInt32(bit_str, -1);
        if (bit_n > 15) {
            failedr.append("invalid bit_str " + bit_str);
            return null;
        }
        if (bit_n >= 0 && vtp != UAVal.ValTP.vt_bool) {
            failedr.append("address with bit must bool type");
            return null;
        }
        return this.transAddr(prefix, addr_n, bit_n, vtp, failedr);
    }

    public HLAddr transAddr(final String prefix, final int addr_num, final int bit_num, UAVal.ValTP vtp, final StringBuilder failedr) {
        final HLAddrDef def = this.prefix2addrdef.get(prefix);
        if (def == null) {
            failedr.append("no HLAddrDef found with prefix=" + prefix);
            return null;
        }
        HLAddrSeg addrseg = null;
        if (vtp != null) {
            for (final HLAddrSeg seg : def.segs) {
                if (seg.matchValTP(vtp) && seg.matchAddr(addr_num, bit_num)) {
                    addrseg = seg;
                    break;
                }
            }
            if (addrseg == null) {
                failedr.append("no AddrSeg match with ValTP=" + vtp.name());
                return null;
            }
        } else {
            for (final HLAddrSeg seg : def.segs) {
                if (seg.matchAddr(addr_num, bit_num)) {
                    addrseg = seg;
                    break;
                }
            }
            if (addrseg == null) {
                failedr.append("no AddrSeg match with num str=" + addr_num);
                return null;
            }
            vtp = addrseg.valTPs[0];
        }
        final HLAddr ret = new HLAddr(vtp, prefix, addr_num, bit_num, addrseg.digitNum).asDef(def, addrseg);
        ret.setWritable(addrseg.bWrite);
        return ret;
    }

    public HashMap<HLAddrSeg, List<HLAddr>> filterAndSortAddrs(final String prefix, final List<HLAddr> addrs) {
        final HLAddrDef def = this.getAddrDef(prefix);
        if (def == null) {
            return null;
        }
        final HashMap<HLAddrSeg, List<HLAddr>> rets = new HashMap<HLAddrSeg, List<HLAddr>>();
        for (final HLAddr ma : addrs) {
            if (!prefix.equals(ma.getPrefix())) {
                continue;
            }
            final HLAddrSeg seg = def.findSeg(ma);
            if (seg == null) {
                continue;
            }
            List<HLAddr> ads = rets.get(seg);
            if (ads == null) {
                ads = new ArrayList<HLAddr>();
                rets.put(seg, ads);
            }
            ads.add(ma);
        }
        for (final List<HLAddr> ads2 : rets.values()) {
            Collections.sort(ads2);
        }
        return rets;
    }

    public List<DevAddr.IAddrDef> getAddrDefs() {
        final ArrayList<DevAddr.IAddrDef> rets = new ArrayList<DevAddr.IAddrDef>();
        rets.addAll((Collection<? extends DevAddr.IAddrDef>) this.prefix2addrdef.values());
        return rets;
    }

    public abstract FinsMode getFinsMode();
}
