

package cn.doraro.flexedge.driver.common.modbus.sim;

import cn.doraro.flexedge.core.util.CompressUUID;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.xmldata.data_class;
import cn.doraro.flexedge.core.util.xmldata.data_obj;
import cn.doraro.flexedge.core.util.xmldata.data_val;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
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
    int fc;
    @data_val(param_name = "reg_idx")
    int regIdx;
    @data_val(param_name = "reg_num")
    int regNum;
    @data_obj(obj_c = SlaveTag.class)
    List<SlaveTag> tags;
    private transient boolean[] boolDatas;
    private transient short[] int16Datas;

    public SlaveDevSeg() {
        this.id = null;
        this.fc = 1;
        this.regIdx = 0;
        this.regNum = 0;
        this.tags = new ArrayList<SlaveTag>();
        this.boolDatas = null;
        this.int16Datas = null;
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

    public List<SlaveTag> getSlaveTags() {
        return this.tags;
    }

    public SlaveTag getSlaveTag(final int regidx) {
        if (this.tags == null) {
            return null;
        }
        for (final SlaveTag tag : this.tags) {
            if (tag.getRegIdx() == regidx) {
                return tag;
            }
        }
        return null;
    }

    public SlaveTag setSlaveTag(final int idx, final String name) {
        SlaveTag st = this.getSlaveTag(idx);
        if (st == null) {
            st = new SlaveTag(name, idx);
            this.tags.add(st);
        } else {
            st.asName(name);
        }
        return st;
    }

    public SlaveTag removeSlaveTag(final int regidx) {
        if (this.tags == null) {
            return null;
        }
        for (final SlaveTag tag : this.tags) {
            if (tag.getRegIdx() == regidx) {
                this.tags.remove(tag);
                return tag;
            }
        }
        return null;
    }

    boolean init() {
        switch (this.fc) {
            case 1:
            case 2: {
                this.boolDatas = new boolean[this.regNum];
                for (int i = 0; i < this.boolDatas.length; ++i) {
                    this.boolDatas[i] = false;
                }
                return true;
            }
            case 3:
            case 4: {
                this.int16Datas = new short[this.regNum];
                for (int i = 0; i < this.int16Datas.length; ++i) {
                    this.int16Datas[i] = 0;
                }
                return true;
            }
            default: {
                return false;
            }
        }
    }

    public boolean isBoolData() {
        return this.fc == 1 || this.fc == 2;
    }

    boolean RT_init(final StringBuilder failedr) {
        switch (this.fc) {
            case 1:
            case 2: {
                this.boolDatas = new boolean[this.regNum];
                for (int i = 0; i < this.boolDatas.length; ++i) {
                    this.boolDatas[i] = false;
                }
                return true;
            }
            case 3:
            case 4: {
                this.int16Datas = new short[this.regNum];
                for (int i = 0; i < this.int16Datas.length; ++i) {
                    this.int16Datas[i] = 0;
                }
                return true;
            }
            default: {
                return false;
            }
        }
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
                for (int i = 0; i < this.int16Datas.length; ++i) {
                    if (i == 0) {
                        sb.append(this.int16Datas[i]);
                    } else {
                        sb.append(",").append(this.int16Datas[i]);
                    }
                }
                sb.append("]");
                return sb.toString();
            }
            default: {
                return null;
            }
        }
    }

    public boolean[] getSlaveDataBool() {
        return this.boolDatas;
    }

    public void setSlaveDataBool(final int reg, final boolean v) {
        this.boolDatas[reg] = v;
    }

    public short[] getSlaveDataInt16() {
        return this.int16Datas;
    }

    public void setSlaveDataInt16(final int reg, final short v) {
        this.int16Datas[reg] = v;
    }

    public void setSlaveDataStr(final int reg, final String v) {
        if (this.isBoolData()) {
            final boolean bv = "true".equals(v) || "1".equals(v);
            this.setSlaveDataBool(reg, bv);
        } else {
            final short tmpv = (short) Convert.parseToInt32(v, 0);
            this.setSlaveDataInt16(reg, tmpv);
        }
    }

    public Object getSlaveData(final int reg) {
        if (this.isBoolData()) {
            return this.boolDatas[reg];
        }
        return this.int16Datas[reg];
    }

    public void onMasterWriteBools(final int idx, final boolean[] datas) {
    }

    public void onMasterWriteInt16s(final int idx, final short[] datas) {
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
