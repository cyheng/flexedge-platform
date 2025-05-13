

package cn.doraro.flexedge.driver.common.modbus;

public abstract class ModbusCmdRead extends ModbusCmd {
    protected ModbusCmdRead() {
    }

    public ModbusCmdRead(final long scan_inter_ms, final int dev_addr) {
        super(scan_inter_ms, dev_addr);
    }

    public abstract int getRegAddr();

    public abstract int getRegNum();
}
