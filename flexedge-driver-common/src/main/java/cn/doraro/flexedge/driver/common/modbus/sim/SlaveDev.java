

package cn.doraro.flexedge.driver.common.modbus.sim;

import cn.doraro.flexedge.core.UATag;
import cn.doraro.flexedge.core.sim.SimDev;
import cn.doraro.flexedge.core.sim.SimTag;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.xmldata.data_class;
import cn.doraro.flexedge.core.util.xmldata.data_obj;
import cn.doraro.flexedge.core.util.xmldata.data_val;
import cn.doraro.flexedge.driver.common.ModbusAddr;
import cn.doraro.flexedge.driver.common.modbus.ModbusBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@data_class
public class SlaveDev extends SimDev {
    ModbusBlock mbCoilIn;
    ModbusBlock mbCoilOut;
    ModbusBlock mbRegIn;
    ModbusBlock mbRegHold;
    @data_val(param_name = "dev_addr")
    int devAddr;
    @data_obj(obj_c = SlaveDevSeg.class, param_name = "segs")
    List<SlaveDevSeg> segs;
    private transient List<ModbusAddr> maddrs;

    public SlaveDev() {
        this.maddrs = new ArrayList<ModbusAddr>();
        this.mbCoilIn = null;
        this.mbCoilOut = null;
        this.mbRegIn = null;
        this.mbRegHold = null;
        this.devAddr = 1;
        this.segs = new ArrayList<SlaveDevSeg>();
    }

    public String getDevTitle() {
        return "Addr=" + this.devAddr + " Seg Num=" + this.segs.size();
    }

    public int getDevAddr() {
        return this.devAddr;
    }

    public List<SlaveDevSeg> getSegs() {
        return this.segs;
    }

    public SlaveDevSeg getSegById(final String id) {
        for (final SlaveDevSeg seg : this.segs) {
            if (seg.getId().equals(id)) {
                return seg;
            }
        }
        return null;
    }

    protected List<SimTag> listSimTagsInner() {
        final ArrayList<SimTag> rets = new ArrayList<SimTag>();
        for (final SlaveDevSeg seg : this.segs) {
            final List<SlaveTag> tags = seg.getSlaveTags();
            if (tags == null) {
                continue;
            }
            for (final SimTag t : tags) {
                final SlaveTag st = (SlaveTag) t;
                st.relatedSeg = seg;
            }
            rets.addAll(tags);
        }
        return rets;
    }

    public SlaveTag findTagBySegIdx(final String segid, final int segidx) {
        for (final SlaveDevSeg seg : this.segs) {
            if (seg.getId().equals(segid)) {
                final List<SlaveTag> tags = seg.getSlaveTags();
                if (tags == null) {
                    return null;
                }
                for (final SlaveTag st : tags) {
                    if (segidx == st.getRegIdx()) {
                        return st;
                    }
                }
            }
        }
        return null;
    }

    public SlaveTag setTag(final String segid, final int regidx, final String name) throws Exception {
        final SlaveDevSeg seg = this.getSegById(segid);
        if (seg == null) {
            return null;
        }
        if (Convert.isNullOrEmpty(name)) {
            return seg.removeSlaveTag(regidx);
        }
        final StringBuilder failedr = new StringBuilder();
        if (!Convert.checkVarName(name, true, failedr)) {
            throw new Exception(failedr.toString());
        }
        SlaveTag st = (SlaveTag) this.getSimTagByName(name);
        if (st == null) {
            st = seg.setSlaveTag(regidx, name);
            this.clearBuffer();
            return st;
        }
        if (st.getRegIdx() != regidx || !st.getRelatedSeg().getId().equals(segid)) {
            throw new Exception("tag name [" + name + "] is existed!");
        }
        st.asName(name);
        this.clearBuffer();
        return st;
    }

    public SimDev asDevTags(final List<UATag> tags) {
        for (UATag uaTag : tags) {
        }
        return this;
    }

    public void init() {
        for (final SlaveDevSeg seg : this.segs) {
            seg.init();
        }
    }

    public boolean RT_init(final StringBuilder failedr) {
        return true;
    }

    private List<ModbusAddr> filterAndSortAddrs(final short addrtp) {
        final ArrayList<ModbusAddr> r = new ArrayList<ModbusAddr>();
        for (final ModbusAddr ma : this.maddrs) {
            if (ma.getAddrTp() == addrtp) {
                r.add(ma);
            }
        }
        Collections.sort(r);
        return r;
    }
}
