

package cn.doraro.flexedge.driver.common;

import cn.doraro.flexedge.core.UAVal;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

class SimFuncRandom extends SimulatorFunc {
    public static final String NAME = "random";
    static List<UAVal.ValTP> VAL_TPS;

    static {
        SimFuncRandom.VAL_TPS = Arrays.asList(UAVal.ValTP.vt_int16, UAVal.ValTP.vt_int32, UAVal.ValTP.vt_float, UAVal.ValTP.vt_double);
    }

    int rate;
    int lowLimit;
    int highLimit;
    Random rand;

    SimFuncRandom() {
        this.rate = -1;
        this.rand = null;
    }

    @Override
    public String getName() {
        return "random";
    }

    @Override
    protected boolean setParams(final List<Object> parms, final StringBuilder failedr) {
        if (parms.size() < 3) {
            failedr.append("random(Rate, Low Limit, High Limit)");
            return false;
        }
        final Object p1 = parms.get(0);
        final Integer rate = this.parseToInt(p1);
        final Integer lowLimit = this.parseToInt(parms.get(1));
        final Integer highLimit = this.parseToInt(parms.get(2));
        if (rate == null) {
            failedr.append("unknown first param: " + p1);
            return false;
        }
        if (lowLimit == null || highLimit == null) {
            failedr.append("lowLimit or highLimit cannot find");
            return false;
        }
        this.rate = rate;
        this.lowLimit = lowLimit;
        this.highLimit = highLimit;
        this.rand = new Random();
        return true;
    }

    @Override
    public List<UAVal.ValTP> getFitValTps() {
        return SimFuncRandom.VAL_TPS;
    }

    @Override
    public int getRunRate() {
        return this.rate;
    }

    @Override
    protected Object calculateNextVal(final UAVal.ValTP vtp) {
        if (this.rand == null) {
            return null;
        }
        switch (vtp) {
            case vt_int16: {
                return (short) this.rand.nextInt(this.highLimit) % (this.highLimit - this.lowLimit + 1) + this.lowLimit;
            }
            case vt_int32: {
                return this.rand.nextInt(this.highLimit) % (this.highLimit - this.lowLimit + 1) + this.lowLimit;
            }
            case vt_float: {
                final float v = this.rand.nextFloat();
                if (this.lowLimit == 0.0) {
                    return this.highLimit * v;
                }
                return (this.highLimit - this.lowLimit) * v + this.lowLimit;
            }
            case vt_double: {
                final double d = this.rand.nextDouble();
                if (this.lowLimit == 0.0) {
                    return this.highLimit * d;
                }
                return (this.highLimit - this.lowLimit) * d + this.lowLimit;
            }
            default: {
                return null;
            }
        }
    }
}
