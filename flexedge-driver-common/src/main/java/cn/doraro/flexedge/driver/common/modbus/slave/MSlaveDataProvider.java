// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common.modbus.slave;

import cn.doraro.flexedge.core.util.Convert;
import org.w3c.dom.Element;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.ByteBuffer;
import cn.doraro.flexedge.core.util.xmldata.XmlVal;

public abstract class MSlaveDataProvider
{
    short devAddr;
    int regIdx;
    int regNum;
    long intervalMS;
    XmlVal.XmlValType valType;
    int acqIdx;
    transient SlaveData slaveData;
    transient long lastActDt;
    
    public MSlaveDataProvider() {
        this.devAddr = 0;
        this.regIdx = 0;
        this.regNum = 0;
        this.intervalMS = 3000L;
        this.valType = null;
        this.acqIdx = 0;
        this.slaveData = null;
        this.lastActDt = -1L;
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
    
    protected void init(final Element ele) {
        this.devAddr = Convert.parseToInt16(ele.getAttribute("dev"), (short)0);
        this.regIdx = Convert.parseToInt32(ele.getAttribute("reg_idx"), 0);
        this.regNum = Convert.parseToInt32(ele.getAttribute("reg_num"), 0);
        this.acqIdx = Convert.parseToInt32(ele.getAttribute("acq_idx"), 0);
        this.intervalMS = Convert.parseToInt64(ele.getAttribute("acquire_interval"), 3000L);
        final String strt = ele.getAttribute("type");
        this.valType = XmlVal.StrType2ValType(strt);
        if (this.valType == null) {
            throw new IllegalArgumentException("no type in MSlaveDataProvider");
        }
    }
    
    public short getDevAddr() {
        return this.devAddr;
    }
    
    public int getRegIdx() {
        return this.regIdx;
    }
    
    public int getRegNum() {
        return this.regNum;
    }
    
    public XmlVal.XmlValType getDataType() {
        return this.valType;
    }
    
    public int getAcqIdx() {
        return this.acqIdx;
    }
    
    public SlaveData getSlaveData() {
        return this.slaveData;
    }
    
    void pulseAcquireData() {
        final long ct = System.currentTimeMillis();
        if (ct - this.lastActDt < this.intervalMS) {
            return;
        }
        final SlaveData sd = this.acquireData(this.regIdx, this.regNum);
        sd.updateUsingData(this.regIdx, this.regNum, this.acqIdx);
        this.slaveData = sd;
        this.lastActDt = ct;
    }
    
    protected abstract SlaveData acquireData(final int p0, final int p1);
    
    protected abstract boolean injectData(final SlaveData p0);
    
    public static class BoolDatas implements SlaveData
    {
        public boolean[] datas;
        boolean[] usingDatas;
        
        public BoolDatas() {
            this.datas = null;
            this.usingDatas = null;
        }
        
        public boolean[] getBoolUsingDatas() {
            return this.usingDatas;
        }
        
        @Override
        public void updateUsingData(final int regidx, final int regnum, final int acqidx) {
            if (this.datas == null) {
                return;
            }
            if (acqidx >= this.datas.length) {
                return;
            }
            int dlen = this.datas.length - acqidx;
            if (dlen > regnum) {
                dlen = regnum;
            }
            final boolean[] bs = new boolean[dlen];
            System.arraycopy(this.datas, acqidx, bs, 0, dlen);
            this.usingDatas = bs;
        }
        
        @Override
        public byte[] toModbusBytes() {
            return null;
        }
    }
    
    public static class Int16Datas implements SlaveData, SlaveDataWord
    {
        public short[] datas;
        short[] usingDatas;
        
        public Int16Datas() {
            this.datas = null;
            this.usingDatas = null;
        }
        
        public short[] getInt16Datas() {
            return this.datas;
        }
        
        @Override
        public short[] getInt16UsingDatas() {
            return this.usingDatas;
        }
        
        @Override
        public void updateUsingData(final int regidx, final int regnum, final int acqidx) {
            if (this.datas == null) {
                return;
            }
            if (acqidx >= this.datas.length) {
                return;
            }
            int dlen = this.datas.length - acqidx;
            if (dlen > regnum) {
                dlen = regnum;
            }
            final short[] bs = new short[dlen];
            System.arraycopy(this.datas, acqidx, bs, 0, dlen);
            this.usingDatas = bs;
        }
        
        @Override
        public byte[] toModbusBytes() {
            return null;
        }
    }
    
    public static class Int32Datas implements SlaveData, SlaveDataWord
    {
        public int[] datas;
        short[] usingDatas;
        
        public Int32Datas() {
            this.datas = null;
            this.usingDatas = null;
        }
        
        @Override
        public void updateUsingData(final int regidx, final int regnum, final int acqidx) {
            if (this.datas == null) {
                return;
            }
            if (acqidx >= this.datas.length) {
                return;
            }
            int dlen = this.datas.length - acqidx;
            if (dlen > regnum) {
                dlen = regnum;
            }
            final int[] bs = new int[dlen];
            System.arraycopy(this.datas, acqidx, bs, 0, dlen);
            final short[] r = new short[dlen * 2];
            for (int i = 0; i < dlen; ++i) {
                r[i * 2] = (short)(0xFFFF & bs[i] >> 16);
                r[i * 2 + 1] = (short)(0xFFFF & bs[i]);
            }
            this.usingDatas = r;
        }
        
        @Override
        public short[] getInt16UsingDatas() {
            return this.usingDatas;
        }
        
        @Override
        public byte[] toModbusBytes() {
            return null;
        }
    }
    
    public static class Int64Datas implements SlaveData, SlaveDataWord
    {
        public long[] datas;
        short[] usingDatas;
        
        public Int64Datas() {
            this.datas = null;
            this.usingDatas = null;
        }
        
        @Override
        public void updateUsingData(final int regidx, final int regnum, final int acqidx) {
            if (this.datas == null) {
                return;
            }
            if (acqidx >= this.datas.length) {
                return;
            }
            int dlen = this.datas.length - acqidx;
            if (dlen > regnum) {
                dlen = regnum;
            }
            final long[] bs = new long[dlen];
            System.arraycopy(this.datas, acqidx, bs, 0, dlen);
            final short[] r = new short[dlen * 4];
            for (int i = 0; i < dlen; ++i) {
                r[i * 2] = (short)(0xFFFFL & bs[i] >> 48);
                r[i * 2 + 1] = (short)(0xFFFFL & bs[i] >> 32);
                r[i * 2 + 2] = (short)(0xFFFFL & bs[i] >> 16);
                r[i * 2 + 3] = (short)(0xFFFFL & bs[i]);
            }
            this.usingDatas = r;
        }
        
        @Override
        public short[] getInt16UsingDatas() {
            return this.usingDatas;
        }
        
        @Override
        public byte[] toModbusBytes() {
            return null;
        }
    }
    
    public static class FloatDatas implements SlaveData, SlaveDataWord
    {
        public float[] datas;
        short[] usingDatas;
        
        public FloatDatas() {
            this.datas = null;
            this.usingDatas = null;
        }
        
        @Override
        public void updateUsingData(final int regidx, final int regnum, final int acqidx) {
            if (this.datas == null) {
                return;
            }
            if (acqidx >= this.datas.length) {
                return;
            }
            int dlen = this.datas.length - acqidx;
            if (dlen > regnum) {
                dlen = regnum;
            }
            final float[] bs = new float[dlen];
            System.arraycopy(this.datas, acqidx, bs, 0, dlen);
            final short[] r = new short[dlen * 2];
            for (int i = 0; i < dlen; ++i) {
                final byte[] fbs = MSlaveDataProvider.floatToByte(bs[i]);
                final short t = (short)(0xFF & fbs[0]);
                r[i * 2 + 1] = (short)(t << 8);
                final short[] array = r;
                final int n = i * 2 + 1;
                array[n] |= (short)(0xFF & fbs[1]);
                r[i * 2] = (short)((0xFF & fbs[2]) << 8);
                final short[] array2 = r;
                final int n2 = i * 2;
                array2[n2] |= (short)(0xFF & fbs[3]);
            }
            this.usingDatas = r;
        }
        
        @Override
        public short[] getInt16UsingDatas() {
            return this.usingDatas;
        }
        
        @Override
        public byte[] toModbusBytes() {
            return null;
        }
    }
    
    public static class DoubleDatas implements SlaveData, SlaveDataWord
    {
        public double[] datas;
        short[] usingDatas;
        
        public DoubleDatas() {
            this.datas = null;
            this.usingDatas = null;
        }
        
        @Override
        public void updateUsingData(final int regidx, final int regnum, final int acqidx) {
            if (this.datas == null) {
                return;
            }
            if (acqidx >= this.datas.length) {
                return;
            }
            int dlen = this.datas.length - acqidx;
            if (dlen > regnum) {
                dlen = regnum;
            }
            final double[] bs = new double[dlen];
            System.arraycopy(this.datas, acqidx, bs, 0, dlen);
            final short[] r = new short[dlen * 4];
            for (int i = 0; i < dlen; ++i) {
                final byte[] fbs = MSlaveDataProvider.doubleToByte(bs[i]);
                final short t = (short)(0xFF & fbs[0]);
                r[i * 2 + 1] = (short)(t << 8);
                final short[] array = r;
                final int n = i * 2 + 1;
                array[n] |= (short)(0xFF & fbs[1]);
                r[i * 2] = (short)((0xFF & fbs[2]) << 8);
                final short[] array2 = r;
                final int n2 = i * 2;
                array2[n2] |= (short)(0xFF & fbs[3]);
                r[i * 2 + 3] = (short)((0xFF & fbs[4]) << 8);
                final short[] array3 = r;
                final int n3 = i * 2 + 3;
                array3[n3] |= (short)(0xFF & fbs[5]);
                r[i * 2 + 2] = (short)((0xFF & fbs[6]) << 8);
                final short[] array4 = r;
                final int n4 = i * 2 + 2;
                array4[n4] |= (short)(0xFF & fbs[7]);
            }
            this.usingDatas = r;
        }
        
        @Override
        public short[] getInt16UsingDatas() {
            return this.usingDatas;
        }
        
        @Override
        public byte[] toModbusBytes() {
            return null;
        }
    }
    
    public interface SlaveData
    {
        byte[] toModbusBytes();
        
        void updateUsingData(final int p0, final int p1, final int p2);
    }
    
    public interface SlaveDataWord
    {
        short[] getInt16UsingDatas();
    }
}
