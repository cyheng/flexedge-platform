

package cn.doraro.flexedge.driver.common;

import cn.doraro.flexedge.core.UAVal;

import java.util.List;

public abstract class SimulatorFunc {
    List<Object> params;
    private transient Object curVal;
    private transient long lastDT;

    public SimulatorFunc() {
        this.params = null;
        this.curVal = null;
        this.lastDT = -1L;
    }

    public static SimulatorFunc createFunc(final String func, final List<Object> params, final StringBuilder failedr) {
        SimulatorFunc ret = null;
        switch (func) {
            case "random": {
                ret = new SimFuncRandom();
                if (!ret.setParams(params, failedr)) {
                    return null;
                }
                return ret;
            }
            case "sine": {
                ret = new SimFuncSine();
                if (!ret.setParams(params, failedr)) {
                    return null;
                }
                return ret;
            }
            default: {
                failedr.append("unknown function name");
                return null;
            }
        }
    }

    public List<Object> getParams() {
        return this.params;
    }

    protected abstract boolean setParams(final List<Object> p0, final StringBuilder p1);

    public abstract String getName();

    public boolean checkValTp(final UAVal.ValTP vtp) {
        final List<UAVal.ValTP> tps = this.getFitValTps();
        return tps != null && tps.contains(vtp);
    }

    public abstract List<UAVal.ValTP> getFitValTps();

    public abstract int getRunRate();

    protected abstract Object calculateNextVal(final UAVal.ValTP p0);

    public Object getValWithRunByRate(final UAVal.ValTP vtp) {
        final long rr = this.getRunRate();
        if (rr <= 0L) {
            return rr;
        }
        final long st = System.currentTimeMillis();
        if (st - this.lastDT > rr) {
            try {
                this.curVal = this.calculateNextVal(vtp);
            } finally {
                this.lastDT = st;
            }
        }
        return this.curVal;
    }

    protected Integer parseToInt(final Object p1) {
        if (p1 instanceof Number) {
            return ((Number) p1).intValue();
        }
        if (p1 instanceof String) {
            try {
                return Integer.parseInt((String) p1);
            } catch (final Exception e) {
                return null;
            }
        }
        return null;
    }

    protected Double parseToDouble(final Object p1) {
        if (p1 instanceof Number) {
            return ((Number) p1).doubleValue();
        }
        if (p1 instanceof String) {
            try {
                return Double.parseDouble((String) p1);
            } catch (final Exception e) {
                return null;
            }
        }
        return null;
    }
}
