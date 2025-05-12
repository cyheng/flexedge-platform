// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.modbuss;

import java.util.Iterator;
import cn.doraro.flexedge.driver.common.modbus.ModbusCmdErr;
import cn.doraro.flexedge.driver.common.modbus.ModbusCmdWriteWords;
import cn.doraro.flexedge.driver.common.modbus.ModbusCmdWriteWord;
import cn.doraro.flexedge.driver.common.modbus.ModbusCmdWriteBit;
import cn.doraro.flexedge.driver.common.modbus.ModbusCmdReadWords;
import cn.doraro.flexedge.driver.common.modbus.ModbusCmdReadBits;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import cn.doraro.flexedge.driver.common.modbus.ModbusParserReqRTU;
import kotlin.NotImplementedError;
import cn.doraro.flexedge.driver.common.modbus.ModbusParserReqTCP;
import cn.doraro.flexedge.driver.common.modbus.ModbusParserReq;
import java.util.List;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.IdCreator;
import org.json.JSONObject;
import cn.doraro.flexedge.driver.common.modbus.ModbusCmd;
import cn.doraro.flexedge.core.util.xmldata.data_class;

@data_class
public abstract class SlaveCP
{
    protected MSBus_M bus;
    String id;
    ModbusCmd.Protocol proto;
    boolean bEnable;
    
    public SlaveCP(final MSBus_M bus) {
        this.id = null;
        this.proto = ModbusCmd.Protocol.rtu;
        this.bEnable = true;
        this.bus = bus;
    }
    
    public String getId() {
        return this.id;
    }
    
    public boolean isEnable() {
        return this.bEnable;
    }
    
    public abstract String getCPTp();
    
    public abstract String getCPTpT();
    
    public abstract boolean isValid(final StringBuilder p0);
    
    public abstract String RT_getRunInf();
    
    public abstract void RT_init();
    
    public abstract String getConnTitle();
    
    public JSONObject toJO() {
        final JSONObject jo = new JSONObject();
        jo.put("id", (Object)this.id);
        jo.put("_tp", (Object)this.getCPTp());
        jo.put("_tpt", (Object)this.getCPTpT());
        jo.put("tt", (Object)this.getConnTitle());
        jo.put("proto", (Object)this.proto.name());
        jo.put("enable", this.bEnable);
        return jo;
    }
    
    public boolean fromJO(final JSONObject jo) {
        this.id = jo.optString("id", IdCreator.newSeqId());
        final String pto = jo.optString("proto");
        if (Convert.isNotNullEmpty(pto)) {
            this.proto = ModbusCmd.Protocol.valueOf(pto);
        }
        if (this.proto == null) {
            this.proto = ModbusCmd.Protocol.rtu;
        }
        this.bEnable = jo.optBoolean("enable", true);
        return true;
    }
    
    public abstract void RT_runInLoop();
    
    public abstract void RT_stop();
    
    public abstract List<SlaveConn> getConns();
    
    public abstract int getConnsNum();
    
    public static SlaveCP fromJO(final MSBus_M bus, final JSONObject jo) {
        final String tp = jo.optString("_tp");
        if (Convert.isNullOrEmpty(tp)) {
            return null;
        }
        SlaveCP ret = null;
        Label_0137: {
            final String s;
            switch (s = tp) {
                case "com": {
                    ret = new SlaveCPCom(bus);
                    break Label_0137;
                }
                case "tcpc": {
                    ret = new SlaveCPTcpClient(bus);
                    break Label_0137;
                }
                case "tcps": {
                    ret = new SlaveCPTcpServer(bus);
                    break Label_0137;
                }
                default:
                    break;
            }
            return null;
        }
        if (!ret.fromJO(jo)) {
            return null;
        }
        return ret;
    }
    
    protected void RT_runConnInLoop(final SlaveConn sc) throws Exception {
        ModbusParserReq mp = (ModbusParserReq)sc.getRelatedOb();
        if (mp == null) {
            switch (this.proto) {
                case tcp: {
                    mp = (ModbusParserReq)new ModbusParserReqTCP();
                    break;
                }
                case ascii: {
                    throw new NotImplementedError();
                }
                default: {
                    mp = (ModbusParserReq)new ModbusParserReqRTU();
                    break;
                }
            }
            mp.asLimitDevIds((List)this.bus.limitIds);
            sc.setRelatedOb(mp);
        }
        final PushbackInputStream inputs = sc.getPushbackInputStream();
        final ModbusCmd reqmc = mp.parseReqCmdInLoop(inputs);
        if (reqmc == null) {
            return;
        }
        final byte[] respbs = this.onReqAndResp(reqmc);
        if (respbs != null) {
            final OutputStream outputs = sc.getOutputStream();
            outputs.write(respbs);
            outputs.flush();
        }
    }
    
    byte[] onReqAndResp(final ModbusCmd mc) {
        if (mc instanceof ModbusCmdReadBits) {
            final ModbusCmdReadBits mcb = (ModbusCmdReadBits)mc;
            return this.onReqAndRespReadBits(mcb);
        }
        if (mc instanceof ModbusCmdReadWords) {
            return this.onReqAndRespReadWords((ModbusCmdReadWords)mc);
        }
        if (mc instanceof ModbusCmdWriteBit) {
            final ModbusCmdWriteBit wb = (ModbusCmdWriteBit)mc;
            return this.onReqAndRespWriteBit(wb);
        }
        if (mc instanceof ModbusCmdWriteWord) {
            final ModbusCmdWriteWord ww = (ModbusCmdWriteWord)mc;
            return this.onReqAndRespWriteWord(ww);
        }
        if (mc instanceof ModbusCmdWriteWords) {
            final ModbusCmdWriteWords ww2 = (ModbusCmdWriteWords)mc;
            return this.onReqAndRespWriteWords(ww2);
        }
        if (mc instanceof ModbusCmdErr) {
            return ((ModbusCmdErr)mc).getRespData();
        }
        return null;
    }
    
    private byte[] onReqAndRespWriteBit(final ModbusCmdWriteBit mcb) {
        final short fc = mcb.getFC();
        final short devid = mcb.getDevAddr();
        final int req_idx = mcb.getRegAddr();
        final boolean bv = mcb.getWriteVal();
        boolean b_set = false;
        for (final SlaveDev di : this.bus.listDevItems()) {
            final SlaveDev d = di;
            if (di.getDevAddr() != devid) {
                continue;
            }
            if (!di.RT_isDevValid()) {
                continue;
            }
            final List<SlaveDevSeg> segs = di.getSegs();
            for (final SlaveDevSeg seg : segs) {
                if (seg.getFC() != 1) {
                    continue;
                }
                final int seg_regidx = seg.getRegIdx();
                final int seg_regnum = seg.getRegNum();
                if (req_idx < seg_regidx) {
                    continue;
                }
                if (req_idx >= seg_regidx + seg_regnum) {
                    continue;
                }
                seg.setSlaveDataBool(req_idx, bv, true);
                b_set = true;
                break;
            }
        }
        if (b_set) {
            return ModbusCmdWriteBit.createResp((ModbusCmd)mcb, devid, req_idx, bv);
        }
        return ModbusCmdWriteBit.createRespError((ModbusCmd)mcb, devid, mcb.getFC());
    }
    
    private byte[] onReqAndRespWriteWord(final ModbusCmdWriteWord mcb) {
        final short fc = mcb.getFC();
        final short devid = mcb.getDevAddr();
        final int req_idx = mcb.getRegAddr();
        final int bv = mcb.getWriteVal();
        boolean b_set = false;
        for (final SlaveDev di : this.bus.listDevItems()) {
            final SlaveDev d = di;
            if (di.getDevAddr() != devid) {
                continue;
            }
            if (!di.RT_isDevValid()) {
                continue;
            }
            final List<SlaveDevSeg> segs = di.getSegs();
            for (final SlaveDevSeg seg : segs) {
                if (seg.getFC() != 3) {
                    continue;
                }
                final int seg_regidx = seg.getRegIdx();
                final int seg_regnum = seg.getRegNum();
                if (req_idx < seg_regidx) {
                    continue;
                }
                if (req_idx >= seg_regidx + seg_regnum) {
                    continue;
                }
                seg.setSlaveDataInt16(req_idx, (short)bv, true);
                b_set = true;
                break;
            }
        }
        if (b_set) {
            return ModbusCmdWriteWord.createResp((ModbusCmd)mcb, devid, (int)(short)req_idx, (short)bv);
        }
        return ModbusCmdWriteWord.createRespError((ModbusCmd)mcb, devid, mcb.getFC());
    }
    
    private byte[] onReqAndRespWriteWords(final ModbusCmdWriteWords mcb) {
        final short fc = mcb.getFC();
        final short devid = mcb.getDevAddr();
        final int req_idx = mcb.getRegAddr();
        final int[] bvs = mcb.getWriteVals();
        boolean b_set = false;
        for (final SlaveDev di : this.bus.listDevItems()) {
            final SlaveDev d = di;
            if (di.getDevAddr() != devid) {
                continue;
            }
            if (!di.RT_isDevValid()) {
                continue;
            }
            final List<SlaveDevSeg> segs = di.getSegs();
            for (final SlaveDevSeg seg : segs) {
                if (seg.getFC() != 3) {
                    continue;
                }
                final int seg_regidx = seg.getRegIdx();
                final int seg_regnum = seg.getRegNum();
                if (req_idx < seg_regidx) {
                    continue;
                }
                if (req_idx >= seg_regidx + seg_regnum) {
                    continue;
                }
                seg.setSlaveDataInt16s(req_idx, bvs, true);
                b_set = true;
                break;
            }
        }
        if (b_set) {
            return ModbusCmdWriteWords.createResp((ModbusCmd)mcb, devid, (int)(short)req_idx, bvs);
        }
        return ModbusCmdWriteWords.createRespError((ModbusCmd)mcb, devid, mcb.getFC());
    }
    
    private byte[] onReqAndRespReadBits(final ModbusCmdReadBits mcb) {
        final short fc = mcb.getFC();
        final short devid = mcb.getDevAddr();
        final int req_idx = mcb.getRegAddr();
        final int req_num = mcb.getRegNum();
        final boolean[] resp = new boolean[req_num];
        for (int i = 0; i < req_num; ++i) {
            resp[i] = false;
        }
        boolean b_get = false;
        for (final SlaveDev di : this.bus.listDevItems()) {
            final SlaveDev d = di;
            if (di.getDevAddr() != devid) {
                continue;
            }
            if (!di.RT_isDevValid()) {
                continue;
            }
            final List<SlaveDevSeg> segs = di.getSegs();
            for (final SlaveDevSeg seg : segs) {
                if (seg.getFC() != fc) {
                    continue;
                }
                final int seg_regidx = seg.getRegIdx();
                final int seg_regnum = seg.getRegNum();
                if (req_idx + req_num <= seg_regidx) {
                    continue;
                }
                if (req_idx > seg_regidx + seg_regnum) {
                    continue;
                }
                final boolean[] bs = seg.getSlaveDataBool();
                if (bs == null) {
                    continue;
                }
                if (req_idx < seg_regidx) {
                    if (req_idx + req_num < seg_regidx + bs.length) {
                        System.arraycopy(bs, 0, resp, seg_regidx - req_idx, req_num - (seg_regidx - req_idx));
                    }
                    else {
                        System.arraycopy(bs, 0, resp, seg_regidx - req_idx, bs.length);
                    }
                }
                else if (req_idx + req_num < seg_regidx + bs.length) {
                    System.arraycopy(bs, req_idx - seg_regidx, resp, 0, req_num);
                }
                else {
                    System.arraycopy(bs, req_idx - seg_regidx, resp, 0, bs.length - (req_idx - seg_regidx));
                }
                b_get = true;
            }
        }
        if (b_get) {
            return ModbusCmdReadBits.createResp((ModbusCmd)mcb, devid, mcb.getFC(), resp);
        }
        return ModbusCmdReadBits.createRespError((ModbusCmd)mcb, devid, mcb.getFC());
    }
    
    private byte[] onReqAndRespReadWords(final ModbusCmdReadWords mcb) {
        final int fc = mcb.getFC();
        final short devid = mcb.getDevAddr();
        final int req_idx = mcb.getRegAddr();
        final int req_num = mcb.getRegNum();
        final short[] resp = new short[req_num];
        for (int i = 0; i < req_num; ++i) {
            resp[i] = 0;
        }
        boolean b_get = false;
        for (final SlaveDev di : this.bus.listDevItems()) {
            final SlaveDev d = di;
            if (di.getDevAddr() != devid) {
                continue;
            }
            if (!di.RT_isDevValid()) {
                continue;
            }
            final List<SlaveDevSeg> segs = di.getSegs();
            for (final SlaveDevSeg seg : segs) {
                if (seg.getFC() != fc) {
                    continue;
                }
                final int seg_regidx = seg.getRegIdx();
                final int seg_regnum = seg.getRegNum();
                if (req_idx + req_num <= seg_regidx) {
                    continue;
                }
                if (req_idx > seg_regidx + seg_regnum) {
                    continue;
                }
                final short[] bs = seg.getSlaveDataInt16s();
                if (bs == null) {
                    continue;
                }
                if (req_idx < seg_regidx) {
                    if (req_idx + req_num < seg_regidx + bs.length) {
                        System.arraycopy(bs, 0, resp, seg_regidx - req_idx, req_num - (seg_regidx - req_idx));
                    }
                    else {
                        System.arraycopy(bs, 0, resp, seg_regidx - req_idx, bs.length);
                    }
                }
                else if (req_idx + req_num < seg_regidx + bs.length) {
                    System.arraycopy(bs, req_idx - seg_regidx, resp, 0, req_num);
                }
                else {
                    System.arraycopy(bs, req_idx - seg_regidx, resp, 0, bs.length - (req_idx - seg_regidx));
                }
                b_get = true;
            }
        }
        if (b_get) {
            return ModbusCmdReadWords.createResp((ModbusCmd)mcb, devid, mcb.getFC(), resp);
        }
        return ModbusCmdReadWords.createRespError((ModbusCmd)mcb, devid, mcb.getFC());
    }
}
