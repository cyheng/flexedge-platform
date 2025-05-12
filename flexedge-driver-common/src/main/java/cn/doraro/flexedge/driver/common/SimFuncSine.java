// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common;

import cn.doraro.flexedge.core.UAVal;

import java.util.Arrays;
import java.util.List;

class SimFuncSine extends SimulatorFunc {
    public static final String NAME = "sine";
    static List<UAVal.ValTP> VAL_TPS;

    static {
        SimFuncSine.VAL_TPS = Arrays.asList(UAVal.ValTP.vt_float, UAVal.ValTP.vt_double);
    }

    int rate;
    double lowLimit;
    double highLimit;
    float freq;
    float phase;
    double A;
    double B;
    double DX;
    private transient double curX;

    SimFuncSine() {
        this.rate = -1;
        this.curX = 0.0;
    }

    @Override
    public String getName() {
        return "sine";
    }

    @Override
    protected boolean setParams(final List<Object> parms, final StringBuilder failedr) {
        if (parms.size() < 5) {
            failedr.append("sine(Rate, Low Limit, High Limit, Frequency, Phase)");
            return false;
        }
        final Object p1 = parms.get(0);
        final Integer rate = this.parseToInt(p1);
        final Double lowLimit = this.parseToDouble(parms.get(1));
        final Double highLimit = this.parseToDouble(parms.get(2));
        final Double freq = this.parseToDouble(parms.get(3));
        final Double phase = this.parseToDouble(parms.get(4));
        if (rate == null) {
            failedr.append("unknown first param: " + p1);
            return false;
        }
        if (lowLimit == null || highLimit == null) {
            failedr.append("lowLimit or highLimit cannot find");
            return false;
        }
        if (freq == null || phase == null) {
            failedr.append("freq or phase cannot find");
            return false;
        }
        this.rate = rate;
        this.lowLimit = lowLimit;
        this.highLimit = highLimit;
        this.freq = freq.floatValue();
        this.phase = (float) (phase.floatValue() / 3.141592653589793 / 2.0);
        this.A = (this.highLimit - this.lowLimit) / 2.0;
        this.B = (this.highLimit + this.lowLimit) / 2.0;
        this.DX = 6.283185307179586 / this.freq * this.rate / 1000.0;
        return true;
    }

    @Override
    public List<UAVal.ValTP> getFitValTps() {
        return SimFuncSine.VAL_TPS;
    }

    @Override
    public int getRunRate() {
        return this.rate;
    }

    @Override
    protected Object calculateNextVal(final UAVal.ValTP vtp) {
        if (this.rate <= 0) {
            return null;
        }
        final double PI2 = 6.283185307179586 / this.freq;
        final Double v = this.A * Math.sin(this.freq * this.curX + this.phase) + this.B;
        this.curX += this.DX;
        if (this.curX > PI2) {
            this.curX %= PI2;
        }
        switch (vtp) {
            case vt_float: {
                return v.floatValue();
            }
            case vt_double: {
                return v;
            }
            default: {
                return null;
            }
        }
    }
}
