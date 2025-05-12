// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.modbuss;

import cn.doraro.flexedge.core.UATag;
import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.basic.ByteOrder;
import cn.doraro.flexedge.core.basic.MemSeg8;
import cn.doraro.flexedge.core.util.CompressUUID;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.xmldata.DataUtil;
import cn.doraro.flexedge.core.util.xmldata.data_class;
import cn.doraro.flexedge.core.util.xmldata.data_obj;
import cn.doraro.flexedge.core.util.xmldata.data_val;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@data_class
public class SlaveDevSeg {
    public static final int MAX_SEG_REG_NUM = 1024;
    private static LinkedHashMap<Integer, String> fc2titles;

    static {
        SlaveDevSeg.fc2titles = null;
    }

    @data_val
    String id;
    @data_val
    String title;
    @data_val
    int fc;
    @data_val(param_name = "reg_idx")
    int regIdx;
    @data_val(param_name = "reg_num")
    int regNum;
    @data_obj(obj_c = SlaveVar.class, param_name = "vars")
    List<SlaveVar> vars;
    @data_obj(obj_c = SlaveBindTag.class, param_name = "tags")
    List<SlaveBindTag> bind_tags;
    transient SlaveDev belongTo;
    private transient boolean[] boolDatas;
    private MemSeg8 numDatas;
    private HashMap<String, SlaveVar> RT_name2var;
    private HashMap<Integer, List<SlaveVar>> RT_idx2vars;
    private HashMap<Integer, SlaveBindTag> RT_idx2tag;
    private boolean RT_bValid;

    public SlaveDevSeg() {
        this.id = null;
        this.title = null;
        this.fc = 1;
        this.regIdx = 0;
        this.regNum = 0;
        this.vars = new ArrayList<SlaveVar>();
        this.bind_tags = new ArrayList<SlaveBindTag>();
        this.boolDatas = null;
        this.numDatas = null;
        this.RT_name2var = null;
        this.RT_idx2vars = null;
        this.RT_idx2tag = null;
        this.RT_bValid = true;
        this.id = CompressUUID.createNewId();
    }

    static byte[] floatToByte(final float v) {
        final ByteBuffer bb = ByteBuffer.allocate(4);
        final byte[] ret = new byte[4];
        final FloatBuffer fb = bb.asFloatBuffer();
        fb.put(v);
        bb.get(ret);
        return ret;
    }

    static float byteToFloat(final byte[] v) {
        final ByteBuffer bb = ByteBuffer.wrap(v);
        final FloatBuffer fb = bb.asFloatBuffer();
        return fb.get();
    }

    static byte[] doubleToByte(final double v) {
        final ByteBuffer bb = ByteBuffer.allocate(8);
        final byte[] ret = new byte[8];
        final DoubleBuffer fb = bb.asDoubleBuffer();
        fb.put(v);
        bb.get(ret);
        return ret;
    }

    static double byteToDouble(final byte[] v) {
        final ByteBuffer bb = ByteBuffer.wrap(v);
        final DoubleBuffer fb = bb.asDoubleBuffer();
        return fb.get();
    }

    public static LinkedHashMap<Integer, String> listFCs() {
        if (SlaveDevSeg.fc2titles != null) {
            return SlaveDevSeg.fc2titles;
        }
        final LinkedHashMap<Integer, String> f2t = new LinkedHashMap<Integer, String>();
        f2t.put(1, "Coil Status(R/W Bool)");
        f2t.put(2, "Input Status(R Bool)");
        f2t.put(3, "Holding Register(R/W Word)");
        f2t.put(4, "Input Register(R Word)");
        return SlaveDevSeg.fc2titles = f2t;
    }

    public static String getAddressStr(final int fc, final int reg) {
        if (reg < 0 || reg > 65535) {
            throw new IllegalArgumentException("reg must be in 0-65535");
        }
        switch (fc) {
            case 1: {
                return String.format("%06d", reg + 1);
            }
            case 2: {
                return "1" + String.format("%05d", reg + 1);
            }
            case 3: {
                return "4" + String.format("%05d", reg + 1);
            }
            case 4: {
                return "3" + String.format("%05d", reg + 1);
            }
            default: {
                return null;
            }
        }
    }

    public String getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public int getFC() {
        return this.fc;
    }

    public String getFCTitle() {
        return listFCs().get(this.fc);
    }

    public int getRegIdx() {
        return this.regIdx;
    }

    public int getRegNum() {
        return this.regNum;
    }

    public String getAddressStr(final int reg) {
        return getAddressStr(this.fc, reg);
    }

    public boolean canWriter() {
        return this.fc == 1 || this.fc == 3;
    }

    public List<SlaveVar> getSlaveVars() {
        return this.vars;
    }

    public SlaveVar getSlaveVar(final int regidx) {
        if (this.vars == null) {
            return null;
        }
        for (final SlaveVar tag : this.vars) {
            if (tag.getRegIdx() == regidx) {
                return tag;
            }
        }
        return null;
    }

    public SlaveVar setSlaveVar(final int idx, final String name) {
        SlaveVar st = this.getSlaveVar(idx);
        if (st == null) {
            st = new SlaveVar(name, idx);
            this.vars.add(st);
        } else {
            st.asName(name);
        }
        return st;
    }

    public SlaveVar removeSlaveVar(final int regidx) {
        if (this.vars == null) {
            return null;
        }
        for (final SlaveVar tag : this.vars) {
            if (tag.getRegIdx() == regidx) {
                this.vars.remove(tag);
                return tag;
            }
        }
        return null;
    }

    public boolean isBoolData() {
        return this.fc == 1 || this.fc == 2;
    }

    public boolean[] getSlaveDataBool() {
        return this.boolDatas;
    }

    public void setSlaveDataBool(final int reg, final boolean v, final boolean b_on_write) {
        if (reg < this.regIdx || reg >= this.regIdx + this.regNum) {
            return;
        }
        this.boolDatas[reg - this.regIdx] = v;
        if (b_on_write) {
            this.RT_onRegIdxWriteBool_Int16(reg, v);
        }
    }

    public short[] getSlaveDataInt16s() {
        final short[] rets = new short[this.regNum];
        for (int i = 0; i < this.regNum; ++i) {
            rets[i] = (short) this.getSlaveDataBool_Int16(this.regIdx + i);
        }
        return rets;
    }

    public void setSlaveDataInt16(final int reg, final short v, final boolean b_on_write) {
        if (reg < this.regIdx || reg >= this.regIdx + this.regNum) {
            return;
        }
        this.numDatas.setValNumber(UAVal.ValTP.vt_int16, (long) (reg * 2), (Number) v, ByteOrder.ModbusWord);
        if (b_on_write) {
            this.RT_onRegIdxWriteBool_Int16(reg, v);
        }
    }

    public void setSlaveDataInt16s(final int reg, final int[] vs, final boolean b_on_write) {
        if (reg < this.regIdx || reg >= this.regIdx + this.regNum) {
            return;
        }
        for (int i = 0; i < vs.length; ++i) {
            this.numDatas.setValNumber(UAVal.ValTP.vt_int16, (long) (reg * 2 + i * 2), (Number) vs[i], ByteOrder.ModbusWord);
        }
        if (b_on_write) {
            this.RT_onRegIdxWriteInt16s(reg, vs);
        }
    }

    public void setSlaveDataInt32(final int reg, final int v) {
        if (reg < this.regIdx || reg >= this.regIdx + this.regNum) {
            return;
        }
        this.numDatas.setValNumber(UAVal.ValTP.vt_int32, (long) (reg * 2), (Number) v, ByteOrder.ModbusWord);
    }

    public void setSlaveDataInt64(final int reg, final long v) {
        if (reg < this.regIdx || reg >= this.regIdx + this.regNum) {
            return;
        }
        this.numDatas.setValNumber(UAVal.ValTP.vt_int64, (long) (reg * 2), (Number) v, ByteOrder.ModbusWord);
    }

    public void setSlaveDataFloat(final int reg, final float v) {
        if (reg < this.regIdx || reg >= this.regIdx + this.regNum) {
            return;
        }
        final byte[] bs = new byte[4];
        DataUtil.floatToBytes(v, bs, 0);
        this.numDatas.setValNumber(UAVal.ValTP.vt_float, (long) (reg * 2), (Number) v, ByteOrder.ModbusWord);
    }

    public void setSlaveDataDouble(final int reg, final double v) {
        if (reg < this.regIdx || reg >= this.regIdx + this.regNum) {
            return;
        }
        this.numDatas.setValNumber(UAVal.ValTP.vt_double, (long) (reg * 2), (Number) v, ByteOrder.ModbusWord);
    }

    public void setSlaveDataStr(final int reg, final String v) {
        if (this.isBoolData()) {
            final boolean bv = "true".equals(v) || "1".equals(v);
            this.setSlaveDataBool(reg, bv, false);
        } else {
            final short tmpv = (short) Convert.parseToInt32(v, 0);
            this.setSlaveDataInt16(reg, tmpv, false);
        }
    }

    public boolean setSlaveDataObj(final int reg, final UAVal.ValTP vtp, final Object v) {
        if (this.isBoolData()) {
            boolean bv = false;
            if (v instanceof Boolean) {
                bv = (boolean) v;
            } else if (v instanceof String) {
                bv = ("true".equals(v) || "1".equals(v));
            } else if (v instanceof Number) {
                bv = (((Number) v).intValue() > 0);
            } else {
                bv = ("true".equals(v) || "1".equals(v));
            }
            this.setSlaveDataBool(reg, bv, false);
            return true;
        }
        final Number num = (Number) v;
        switch (vtp) {
            case vt_byte:
            case vt_char:
            case vt_int16:
            case vt_uint8:
            case vt_uint16: {
                this.setSlaveDataInt16(reg, num.shortValue(), false);
                return true;
            }
            case vt_int32:
            case vt_uint32: {
                this.setSlaveDataInt32(reg, num.intValue());
                return true;
            }
            case vt_int64:
            case vt_uint64: {
                this.setSlaveDataInt64(reg, num.longValue());
                return true;
            }
            case vt_float: {
                this.setSlaveDataFloat(reg, num.floatValue());
                return true;
            }
            case vt_double: {
                this.setSlaveDataDouble(reg, num.doubleValue());
                return true;
            }
            default: {
                return false;
            }
        }
    }

    public Object getSlaveDataBool_Int16(final int reg) {
        if (reg < this.regIdx || reg >= this.regIdx + this.regNum) {
            return null;
        }
        if (this.isBoolData()) {
            return this.boolDatas[reg - this.regIdx];
        }
        return this.numDatas.getValNumber(UAVal.ValTP.vt_int16, (long) (reg * 2), ByteOrder.ModbusWord);
    }

    public String getSlaveDataStr4Show(final int reg) {
        if (reg < this.regIdx || reg >= this.regIdx + this.regNum) {
            return null;
        }
        if (this.isBoolData()) {
            if (this.boolDatas == null) {
                return "0";
            }
            return this.boolDatas[reg - this.regIdx] ? "1" : "0";
        } else {
            if (this.numDatas == null) {
                return "0";
            }
            final Number obv = this.numDatas.getValNumber(UAVal.ValTP.vt_int16, (long) (reg * 2), ByteOrder.ModbusWord);
            return obv.toString();
        }
    }

    boolean RT_init() {
        switch (this.fc) {
            case 1:
            case 2: {
                this.boolDatas = new boolean[this.regNum];
                for (int i = 0; i < this.boolDatas.length; ++i) {
                    this.boolDatas[i] = false;
                }
                break;
            }
            case 3:
            case 4: {
                this.numDatas = new MemSeg8((long) (this.regIdx * 2), this.regNum * 2);
                break;
            }
        }
        final HashMap<String, SlaveVar> n2v = new HashMap<String, SlaveVar>();
        final HashMap<Integer, List<SlaveVar>> idx2vars = new HashMap<Integer, List<SlaveVar>>();
        for (final SlaveVar v : this.vars) {
            n2v.put(v.name, v);
            v.relatedSeg = this;
            final int regidx = v.regIdx;
            List<SlaveVar> svs = idx2vars.get(regidx);
            if (svs == null) {
                svs = new ArrayList<SlaveVar>(4);
                idx2vars.put(regidx, svs);
            }
            svs.add(v);
        }
        this.RT_name2var = n2v;
        this.RT_idx2vars = idx2vars;
        final HashMap<Integer, SlaveBindTag> idx2tag = new HashMap<Integer, SlaveBindTag>();
        for (final SlaveBindTag bt : this.bind_tags) {
            bt.belongTo = this.belongTo;
            bt.relatedSeg = this;
            idx2tag.put(bt.regIdx, bt);
        }
        this.RT_idx2tag = idx2tag;
        return true;
    }

    private void RT_onRegIdxWriteBool_Int16(final int regidx, final Object ob) {
        if (this.isBoolData()) {
            if (!(ob instanceof Boolean)) {
                return;
            }
        } else if (!(ob instanceof Short)) {
            return;
        }
        final List<SlaveVar> svs = this.RT_idx2vars.get(regidx);
        if (svs != null && svs.size() > 0) {
            final JSONObject jo = new JSONObject();
            boolean bgit = false;
            for (final SlaveVar sv : svs) {
                if (sv.valTP == UAVal.ValTP.vt_int16 || sv.valTP == UAVal.ValTP.vt_uint16 || sv.valTP == UAVal.ValTP.vt_bool) {
                    final String n = sv.getName();
                    jo.put(n, ob);
                    bgit = true;
                }
            }
            if (bgit) {
                this.belongTo.belongTo.RT_onMasterWriteVar(this, jo);
            }
        }
        final SlaveBindTag bt = this.RT_idx2tag.get(regidx);
        if (bt != null) {
            this.belongTo.belongTo.RT_onMasterWriteBindTag(this, bt, ob);
        }
    }

    private void RT_onRegIdxWriteInt16s(final int regidx, final int[] regvs) {
        final int regn = regvs.length;
        if (regn <= 0) {
            return;
        }
        for (int i = 0; i < regn; ++i) {
            this.RT_onRegIdxWriteInt16s(regidx, regvs, i);
        }
    }

    private void RT_onRegIdxWriteInt16s(final int regidx, final int[] regvs, final int idx) {
        final List<SlaveVar> svs = this.RT_idx2vars.get(regidx + idx);
        if (svs != null && svs.size() > 0) {
            final JSONObject jo = new JSONObject();
            boolean bgit = false;
            for (final SlaveVar sv : svs) {
                final String n = sv.getName();
                final UAVal.ValTP vt = sv.valTP;
                final Object v = this.getOrCombVal(vt, regvs, idx);
                if (v == null) {
                    continue;
                }
                jo.put(n, v);
                bgit = true;
            }
            if (bgit) {
                this.belongTo.belongTo.RT_onMasterWriteVar(this, jo);
            }
        }
        final SlaveBindTag bt = this.RT_idx2tag.get(regidx);
        if (bt != null) {
            final UATag tag = bt.getTag();
            if (tag != null) {
                final UAVal.ValTP tagvt = tag.getValTp();
                final Object ob = this.getOrCombVal(tagvt, regvs, idx);
                if (ob != null) {
                    final JSONObject jo2 = new JSONObject();
                    jo2.put("tag", (Object) bt.getTagPath());
                    jo2.put("value", ob);
                    this.belongTo.belongTo.RT_onMasterWriteTag(this, jo2);
                }
            }
        }
    }

    private Object getOrCombVal(final UAVal.ValTP vt, final int[] regvs, final int idx) {
        if (vt == null) {
            return null;
        }
        switch (vt) {
            case vt_int16: {
                return (short) regvs[idx];
            }
            case vt_uint16: {
                return regvs[idx] & 0xFFFF;
            }
            case vt_int32:
            case vt_uint32:
            case vt_float: {
                if (regvs.length <= idx + 1) {
                    return null;
                }
                final byte[] bs = new byte[4];
                DataUtil.shortToBytes((short) regvs[idx], bs, 0);
                DataUtil.shortToBytes((short) regvs[idx + 1], bs, 2);
                if (vt == UAVal.ValTP.vt_float) {
                    return DataUtil.bytesToFloat(bs, ByteOrder.ModbusWord);
                }
                final int intv = DataUtil.bytesToInt(bs, ByteOrder.ModbusWord);
                if (vt == UAVal.ValTP.vt_int32) {
                    return intv;
                }
                return (long) intv & -1L;
            }
            case vt_int64:
            case vt_double: {
                if (regvs.length <= idx + 3) {
                    return null;
                }
                final byte[] bs = new byte[8];
                DataUtil.shortToBytes((short) regvs[idx], bs, 0);
                DataUtil.shortToBytes((short) regvs[idx + 1], bs, 2);
                DataUtil.shortToBytes((short) regvs[idx + 2], bs, 4);
                DataUtil.shortToBytes((short) regvs[idx + 3], bs, 6);
                if (vt == UAVal.ValTP.vt_double) {
                    return DataUtil.bytesToDouble(bs, 0, ByteOrder.ModbusWord);
                }
                return DataUtil.bytesToLong(bs, ByteOrder.ModbusWord);
            }
            default: {
                return null;
            }
        }
    }

    private void RT_onRegIdxWriteBits(final int regidx, final boolean[] regvs) {
    }

    public String RT_getDataJsonArrStr() {
        final StringBuilder sb = new StringBuilder();
        switch (this.fc) {
            case 1:
            case 2: {
                sb.append("[");
                for (int i = 0; i < this.boolDatas.length; ++i) {
                    if (i == 0) {
                        sb.append(this.boolDatas[i]);
                    } else {
                        sb.append(",").append(this.boolDatas[i]);
                    }
                }
                sb.append("]");
                return sb.toString();
            }
            case 3:
            case 4: {
                sb.append("[");
                for (int i = 0; i < this.regNum; ++i) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append(this.numDatas.getValNumber(UAVal.ValTP.vt_int16, (long) ((this.regIdx + i) * 2), ByteOrder.ModbusWord));
                }
                sb.append("]");
                return sb.toString();
            }
            default: {
                return null;
            }
        }
    }

    public void RT_setVarVal(final String varn, final Object ob) {
        if (ob == null) {
            return;
        }
        final SlaveVar sv = this.RT_name2var.get(varn);
        if (sv == null) {
            return;
        }
        final UAVal.ValTP vtp = sv.getValueTp();
        this.setSlaveDataObj(sv.regIdx, vtp, ob);
    }

    public void RT_setSegValid(final boolean b_valid) {
        this.RT_bValid = b_valid;
    }

    public boolean RT_isSegValid() {
        return this.RT_bValid;
    }

    void RT_readBindTags() {
        if (this.bind_tags == null) {
            return;
        }
        for (final SlaveBindTag bt : this.bind_tags) {
            final UATag tag = bt.getTag();
            if (tag == null) {
                continue;
            }
            final UAVal uav = tag.RT_getVal();
            if (!uav.isValid()) {
                continue;
            }
            final Object objv = uav.getObjVal();
            this.setSlaveDataObj(bt.regIdx, tag.getValTp(), objv);
        }
    }

    public abstract static class SlaveData {
        int startIdx;

        public SlaveData() {
            this.startIdx = -1;
        }

        public final int getStartIdx() {
            return this.startIdx;
        }

        public abstract int getDataLen();
    }

    public static class BoolDatas extends SlaveData {
        boolean[] usingDatas;

        public BoolDatas(final int len) {
            this.usingDatas = null;
            this.usingDatas = new boolean[len];
            for (int i = 0; i < len; ++i) {
                this.usingDatas[i] = false;
            }
        }

        public boolean[] getBoolDatas() {
            return this.usingDatas;
        }

        @Override
        public int getDataLen() {
            if (this.usingDatas == null) {
                return 0;
            }
            return this.usingDatas.length;
        }

        public void setDataBool(final int idx, final boolean[] bs) {
            System.arraycopy(bs, 0, this.usingDatas, idx, bs.length);
        }

        public void getDataBool(final int idx, final int len, final boolean[] buf, final int buf_offset) {
            System.arraycopy(this.usingDatas, idx, buf, buf_offset, len);
        }
    }

    public static class Int16Datas extends SlaveData {
        short[] usingDatas;

        public Int16Datas(final int len) {
            this.usingDatas = null;
            this.usingDatas = new short[len];
            for (int i = 0; i < len; ++i) {
                this.usingDatas[i] = 0;
            }
        }

        public short[] getInt16Datas() {
            return this.usingDatas;
        }

        public byte[] toModbusBytes() {
            return null;
        }

        @Override
        public int getDataLen() {
            if (this.usingDatas == null) {
                return 0;
            }
            return this.usingDatas.length;
        }
    }
}
