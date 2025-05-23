package cn.doraro.flexedge.core.util.js;

import cn.doraro.flexedge.core.cxt.JSObMap;
import cn.doraro.flexedge.core.cxt.JsDef;
import cn.doraro.flexedge.core.util.Convert;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Calendar;

@JsDef(name = "util", title = "Util", desc = "System Util", icon = "icon_util")
public class GUtil extends JSObMap {
    /**
     * 取小数位字符串
     *
     * @param d
     * @param plnum
     */
    @JsDef
    public String to_dec_str(double d, int plnum) {
        return Convert.toDecimalDigitsStr(d, plnum);
    }

    /**
     * 更加输入的整数值，和位参数，查找整数对应位bit值，并返回boolean值表达方式
     *
     * @param v
     * @param pos
     * @return
     */
    @JsDef
    public boolean bit_bool(Number vnum, int pos) {
        int v = vnum.intValue();
        return (v & (1 << pos)) > 0;
    }

    /**
     * 输入的v本质上是2字节的word无符号值，要求把值转换为无符号的 int
     *
     * @param v
     * @return
     */
    @JsDef
    public int word_to_int(Number vnum) {
        int v = vnum.intValue();
        return 0xFFFF & v;
    }

    //// float转byte[]
    // byte[] floatToByte(float v) {
    // ByteBuffer bb = ByteBuffer.allocate(4);
    // byte[] ret = new byte [4];
    // FloatBuffer fb = bb.asFloatBuffer();
    // fb.put(v);
    // bb.get(ret);
    // return ret;
    // }


    // byte[]转float
    @JsDef
    float byteToFloat(byte[] v) {
        ByteBuffer bb = ByteBuffer.wrap(v);
        FloatBuffer fb = bb.asFloatBuffer();
        return fb.get();
    }

    @JsDef
    public float int2float(Number intn) {
        int intv = intn.intValue();
        return Float.intBitsToFloat(intv);
    }

    @JsDef
    public int float2int(float v) {
        return Float.floatToIntBits(v);
    }

    @JsDef
    public short[] float2short(float v) {
        int iv = Float.floatToIntBits(v);
        return new short[]{(short) ((iv >> 16) & 0xFFFF), (short) (iv & 0xFFFF)};
    }
    //public float int2float(int intv,)

    /**
     * 把modbus高位2字节，低位2字节转换为float
     *
     * @param hw
     * @param lw
     * @return
     */
    @JsDef
    public float modbus_float(Number hnum, Number lnum) {
        int hw = hnum.intValue();
        int lw = lnum.intValue();
        hw &= 0xffff;
        lw &= 0xffff;
        byte[] fbs = new byte[4];
        fbs[0] = (byte) ((hw >> 8) & 0xFF);
        fbs[1] = (byte) ((hw) & 0xFF);
        fbs[2] = (byte) ((lw >> 8) & 0xFF);
        fbs[3] = (byte) ((lw) & 0xFF);
        return byteToFloat(fbs);
    }

    @JsDef
    public double modbus_double(Number hnum, Number lnum) {
        int hw = hnum.intValue();
        int lw = lnum.intValue();
        hw &= 0xffff;
        lw &= 0xffff;
        byte[] fbs = new byte[4];
        fbs[0] = (byte) ((hw >> 24) & 0xFF);
        fbs[1] = (byte) ((hw >> 16) & 0xFF);
        fbs[2] = (byte) ((hw >> 8) & 0xFF);
        fbs[3] = (byte) ((hw) & 0xFF);

        fbs[4] = (byte) ((lw >> 24) & 0xFF);
        fbs[5] = (byte) ((lw >> 16) & 0xFF);
        fbs[6] = (byte) ((lw >> 8) & 0xFF);
        fbs[7] = (byte) ((lw) & 0xFF);
        return byteToFloat(fbs);
    }

    @JsDef
    public long modbus_int64(Number hnum, Number lnum) {
        int hw = hnum.intValue();
        int lw = lnum.intValue();
        hw &= 0xffff;
        lw &= 0xffff;
        return ((long) hw) * 65536 + lw;
    }

    @JsDef
    public int modbus_int32(Number hnum, Number lnum) {
        int hw = hnum.intValue();
        int lw = lnum.intValue();
        return (hw & 0xFFFF) * 65536 + (lw & 0xFFFF);
    }

    /**
     * 用来模拟一个在一定范围内变化的数。如模拟pH在7左右变化等
     *
     * @param base_val 变化基准值
     * @param down_chg 小于基准值的幅度，大于0
     * @param up_chg   大于基准值的幅度，大于0
     * @return
     */
    @JsDef
    public double simul_double(double base_val, double down_chg, double up_chg) {
        double rd = Math.random();
        double minv = base_val - down_chg;
        // double maxv = base_val + up_chg ;
        return minv + (up_chg + down_chg) * rd;
        // return base_val;
    }

    /**
     * 用来模拟一个在一定范围内变化的数。如模拟pH在7左右变化等
     *
     * @param base_val 变化基准值
     * @param down_chg 小于基准值的幅度，大于0
     * @param up_chg   大于基准值的幅度，大于0
     * @return
     */
    @JsDef
    public float simul_float(float base_val, float down_chg, float up_chg) {
        return (float) simul_double(base_val, down_chg, up_chg);
    }

    @JsDef
    public long str2tick_ms(String str) {
        Calendar cal = Convert.toCalendar(str);
        if (cal == null)
            return -1;
        return cal.getTimeInMillis();
    }
}
