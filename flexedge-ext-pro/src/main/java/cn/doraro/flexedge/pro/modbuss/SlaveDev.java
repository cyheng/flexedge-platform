

package cn.doraro.flexedge.pro.modbuss;

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
public class SlaveDev extends SlaveNode {
    ModbusBlock mbCoilIn;
    ModbusBlock mbCoilOut;
    ModbusBlock mbRegIn;
    ModbusBlock mbRegHold;
    @data_val(param_name = "dev_addr")
    int devAddr;
    @data_obj(obj_c = SlaveDevSeg.class, param_name = "segs")
    List<SlaveDevSeg> segs;
    transient MSBus_M belongTo;
    private transient List<ModbusAddr> maddrs;
    private transient List<SlaveVar> simTags;
    private boolean RT_bValid;

    public SlaveDev() {
        this.maddrs = new ArrayList<ModbusAddr>();
        this.mbCoilIn = null;
        this.mbCoilOut = null;
        this.mbRegIn = null;
        this.mbRegHold = null;
        this.devAddr = 1;
        this.segs = new ArrayList<SlaveDevSeg>();
        this.simTags = null;
        this.RT_bValid = true;
    }

    public List<SlaveVar> getSimTags() {
        final List<SlaveVar> r = this.simTags;
        if (r != null) {
            return r;
        }
        synchronized (this) {
            List<SlaveVar> rets = this.listSimTagsInner();
            if (rets == null) {
                rets = new ArrayList<SlaveVar>();
            }
            for (final SlaveVar st : rets) {
                st.belongTo = this;
            }
            return this.simTags = rets;
        }
    }

    protected void clearBuffer() {
        this.simTags = null;
    }

    public SlaveVar getSimTagByName(final String n) {
        final List<SlaveVar> tags = this.getSimTags();
        if (tags == null) {
            return null;
        }
        for (final SlaveVar tag : tags) {
            if (n.equals(tag.getName())) {
                return tag;
            }
        }
        return null;
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

    protected List<SlaveVar> listSimTagsInner() {
        final ArrayList<SlaveVar> rets = new ArrayList<SlaveVar>();
        for (final SlaveDevSeg seg : this.segs) {
            final List<SlaveVar> tags = seg.getSlaveVars();
            if (tags == null) {
                continue;
            }
            for (final SlaveVar st : tags) {
                final SlaveVar t = st;
                st.relatedSeg = seg;
            }
            rets.addAll(tags);
        }
        return rets;
    }

    public SlaveVar findTagBySegIdx(final String segid, final int segidx) {
        for (final SlaveDevSeg seg : this.segs) {
            if (seg.getId().equals(segid)) {
                final List<SlaveVar> tags = seg.getSlaveVars();
                if (tags == null) {
                    return null;
                }
                for (final SlaveVar st : tags) {
                    if (segidx == st.getRegIdx()) {
                        return st;
                    }
                }
            }
        }
        return null;
    }

    public SlaveVar setTag(final String segid, final int regidx, final String name) throws Exception {
        final SlaveDevSeg seg = this.getSegById(segid);
        if (seg == null) {
            return null;
        }
        if (Convert.isNullOrEmpty(name)) {
            return seg.removeSlaveVar(regidx);
        }
        final StringBuilder failedr = new StringBuilder();
        if (!Convert.checkVarName(name, true, failedr)) {
            throw new Exception(failedr.toString());
        }
        SlaveVar st = this.getSimTagByName(name);
        if (st == null) {
            st = seg.setSlaveVar(regidx, name);
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

    public void RT_init() {
        for (final SlaveDevSeg seg : this.segs) {
            seg.belongTo = this;
            seg.RT_init();
        }
    }

    public void RT_setVarVal(final String varn, final Object ob) {
        for (final SlaveDevSeg seg : this.segs) {
            seg.RT_setVarVal(varn, ob);
        }
    }

    public void RT_readBindTags() {
        for (final SlaveDevSeg seg : this.segs) {
            seg.RT_readBindTags();
        }
    }

    public void RT_setDevValid(final boolean b_valid) {
        this.RT_bValid = b_valid;
    }

    public boolean RT_isDevValid() {
        return this.RT_bValid;
    }
}
