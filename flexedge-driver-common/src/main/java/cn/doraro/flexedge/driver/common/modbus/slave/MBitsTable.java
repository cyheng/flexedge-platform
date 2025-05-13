

package cn.doraro.flexedge.driver.common.modbus.slave;

public class MBitsTable {
    int regAddr;
    int regNum;
    boolean[] regData;

    public MBitsTable(final int regaddr, final int regnum) {
        this.regAddr = -1;
        this.regNum = -1;
        this.regData = null;
        this.regAddr = regaddr;
        this.regNum = regnum;
        this.regData = new boolean[regnum];
    }

    public int getRegAddr() {
        return this.regAddr;
    }

    public int getRegNum() {
        return this.regNum;
    }

    public boolean[] getRegData() {
        return this.regData;
    }
}
