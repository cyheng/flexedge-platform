

package cn.doraro.flexedge.driver.common.modbus.sim;

import cn.doraro.flexedge.core.cxt.JsProp;
import cn.doraro.flexedge.core.sim.SimChannel;
import cn.doraro.flexedge.core.sim.SimConn;
import cn.doraro.flexedge.core.sim.SimDev;
import cn.doraro.flexedge.core.util.xmldata.data_class;
import cn.doraro.flexedge.driver.common.modbus.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.List;

@data_class
public class SlaveChannel extends SimChannel implements Runnable {
    static final int BUF_LEN = 255;
    private ArrayList<Integer> limitIds;

    public SlaveChannel() {
        this.limitIds = null;
    }

    public String getTp() {
        return "mslave";
    }

    public String getTpTitle() {
        return "Modbus Slave";
    }

    public SimDev createNewDev() {
        return new SlaveDev();
    }

    private void delay(final int ms) {
        try {
            Thread.sleep(ms);
        } catch (final Exception ex) {
        }
    }

    public boolean RT_init(final StringBuilder failedr) {
        if (!super.RT_init(failedr)) {
            return false;
        }
        final List<SimDev> devs = this.listDevItems();
        if (devs == null || devs.size() <= 0) {
            failedr.append("no devices in channel");
            return false;
        }
        final ArrayList<Integer> ids = new ArrayList<Integer>();
        for (final SimDev dev : devs) {
            if (!dev.RT_init(failedr)) {
                return false;
            }
            final SlaveDev sd = (SlaveDev) dev;
            ids.add(sd.getDevAddr());
        }
        this.limitIds = ids;
        return true;
    }

    protected void RT_runConnInLoop(final SimConn sc) throws Exception {
        ModbusParserReqRTU mp = (ModbusParserReqRTU) sc.getRelatedOb();
        if (mp == null) {
            mp = new ModbusParserReqRTU();
            mp.asLimitDevIds(this.limitIds);
            sc.setRelatedOb((Object) mp);
        }
        final PushbackInputStream inputs = sc.getPushbackInputStream();
        final ModbusCmd reqmc = mp.parseReqCmdInLoop(inputs);
        if (reqmc == null) {
            return;
        }
        final byte[] respbs = this.onReqAndResp(reqmc);
        if (respbs != null) {
            final OutputStream outputs = sc.getConnOutputStream();
            outputs.write(respbs);
            outputs.flush();
        }
    }

    protected void RT_runConnInLoop0(final SimConn sc) throws Exception {
        int last_dlen = 0;
        long last_dt = -1L;
        long last_no_dt = System.currentTimeMillis();
        final byte[] buf = new byte[255];
        final int len = 0;
        Label_0290_Outer:
        while (true) {
            this.delay(1);
            final InputStream inputs = sc.getConnInputStream();
            final OutputStream outputs = sc.getConnOutputStream();
            if (inputs == null) {
                continue;
            }
            if (last_dlen == 0) {
                if (inputs.available() <= 0) {
                    this.delay(5);
                    if (System.currentTimeMillis() - last_no_dt <= 5000L) {
                        continue;
                    }
                    last_no_dt = System.currentTimeMillis();
                    sc.pulseConn();
                } else {
                    last_dlen = inputs.available();
                    last_dt = System.currentTimeMillis();
                }
            } else if (inputs.available() > last_dlen) {
                last_dlen = inputs.available();
                last_dt = System.currentTimeMillis();
            } else {
                if (System.currentTimeMillis() - last_dt < 10L) {
                    continue Label_0290_Outer;
                }
                final int rlen = last_dlen;
                try {
                    if (last_dlen > 255) {
                        inputs.skip(last_dlen);
                        continue Label_0290_Outer;
                    }
                } finally {
                    last_dlen = 0;
                    last_dt = 0L;
                }
                byte[] rdata = new byte[rlen];
                inputs.read(rdata);
                final long st = System.currentTimeMillis();
                final int[] pl = {0};
                while (true) {
                    while (pl[0] >= 0) {
                        if (pl[0] > 0) {
                            final byte[] crbs = new byte[rdata.length - pl[0]];
                            System.arraycopy(rdata, pl[0], crbs, 0, crbs.length);
                            rdata = crbs;
                        }
                        final byte[] respbs = this.onReadReqAndResp(rdata, pl);
                        if (respbs != null) {
                            outputs.write(respbs);
                            outputs.flush();
                        }
                        if (pl[0] < 0) {
                            continue Label_0290_Outer;
                        }
                    }
                    continue;
                }
            }
        }
    }

    private byte[] onReadReqAndResp(final byte[] reqbs, final int[] parseleft) {
        final ModbusCmd mc = ModbusCmd.parseRequest(reqbs, parseleft);
        if (mc == null) {
            return null;
        }
        return this.onReqAndResp(mc);
    }

    private byte[] onReqAndResp(final ModbusCmd mc) {
        if (mc instanceof ModbusCmdReadBits) {
            final ModbusCmdReadBits mcb = (ModbusCmdReadBits) mc;
            return this.onReqAndRespReadBits(mcb);
        }
        if (mc instanceof ModbusCmdReadWords) {
            return this.onReqAndRespReadWords((ModbusCmdReadWords) mc);
        }
        if (mc instanceof ModbusCmdWriteBit) {
            final ModbusCmdWriteBit wb = (ModbusCmdWriteBit) mc;
            return this.onReqAndRespWriteBit(wb);
        }
        if (mc instanceof ModbusCmdWriteWord) {
            final ModbusCmdWriteWord ww = (ModbusCmdWriteWord) mc;
            return this.onReqAndRespWriteWord(ww);
        }
        return null;
    }

    private byte[] onReqAndRespWriteBit(final ModbusCmdWriteBit mcb) {
        final short fc = mcb.getFC();
        final short devid = mcb.getDevAddr();
        final int req_idx = mcb.getRegAddr();
        final boolean bv = mcb.getWriteVal();
        for (final SimDev d : this.listDevItems()) {
            final SlaveDev di = (SlaveDev) d;
            if (di.getDevAddr() != devid) {
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
                seg.setSlaveDataBool(req_idx - seg_regidx, bv);
                break;
            }
        }
        return ModbusCmdWriteBit.createResp(mcb, devid, req_idx, bv);
    }

    private byte[] onReqAndRespWriteWord(final ModbusCmdWriteWord mcb) {
        final short fc = mcb.getFC();
        final short devid = mcb.getDevAddr();
        final int req_idx = mcb.getRegAddr();
        final int bv = mcb.getWriteVal();
        for (final SimDev d : this.listDevItems()) {
            final SlaveDev di = (SlaveDev) d;
            if (di.getDevAddr() != devid) {
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
                seg.setSlaveDataInt16(req_idx - seg_regidx, (short) bv);
                break;
            }
        }
        return ModbusCmdWriteWord.createResp(mcb, devid, req_idx, (short) bv);
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
        for (final SimDev d : this.listDevItems()) {
            final SlaveDev di = (SlaveDev) d;
            if (di.getDevAddr() != devid) {
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
                    } else {
                        System.arraycopy(bs, 0, resp, seg_regidx - req_idx, bs.length);
                    }
                } else if (req_idx + req_num < seg_regidx + bs.length) {
                    System.arraycopy(bs, req_idx - seg_regidx, resp, 0, req_num);
                } else {
                    System.arraycopy(bs, req_idx - seg_regidx, resp, 0, bs.length - (req_idx - seg_regidx));
                }
            }
        }
        return ModbusCmdReadBits.createResp(mcb, devid, mcb.getFC(), resp);
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
        for (final SimDev d : this.listDevItems()) {
            final SlaveDev di = (SlaveDev) d;
            if (di.getDevAddr() != devid) {
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
                final short[] bs = seg.getSlaveDataInt16();
                if (bs == null) {
                    continue;
                }
                if (req_idx < seg_regidx) {
                    if (req_idx + req_num < seg_regidx + bs.length) {
                        System.arraycopy(bs, 0, resp, seg_regidx - req_idx, req_num - (seg_regidx - req_idx));
                    } else {
                        System.arraycopy(bs, 0, resp, seg_regidx - req_idx, bs.length);
                    }
                } else if (req_idx + req_num < seg_regidx + bs.length) {
                    System.arraycopy(bs, req_idx - seg_regidx, resp, 0, req_num);
                } else {
                    System.arraycopy(bs, req_idx - seg_regidx, resp, 0, bs.length - (req_idx - seg_regidx));
                }
            }
        }
        return ModbusCmdReadWords.createResp(mcb, devid, mcb.getFC(), resp);
    }

    protected void onConnOk(final SimConn sc) {
    }

    protected void onConnBroken(final SimConn sc) {
    }

    public Object JS_get(final String key) {
        final Object r = super.JS_get(key);
        if (r != null) {
            return r;
        }
        switch (key) {
            case "_tp": {
                return this.getTp();
            }
            default: {
                return null;
            }
        }
    }

    public List<JsProp> JS_props() {
        final List<JsProp> rets = super.JS_props();
        rets.add(new JsProp("_tp", (Object) null, (Class) String.class, false, this.getTpTitle(), ""));
        return rets;
    }
}
