

package cn.doraro.flexedge.driver.common;

import cn.doraro.flexedge.core.DevAddr;
import cn.doraro.flexedge.core.DevDef;
import cn.doraro.flexedge.core.UADev;
import cn.doraro.flexedge.core.basic.IConnEndPoint;
import cn.doraro.flexedge.core.conn.ConnPtStream;
import cn.doraro.flexedge.driver.common.modbus.*;
import cn.doraro.flexedge.driver.common.modbus.sniffer.SnifferCmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModbusDevItem {
    ModbusBlock mbCoilIn;
    ModbusBlock mbCoilOut;
    ModbusBlock mbRegIn;
    ModbusBlock mbRegHold;
    private transient List<ModbusAddr> maddrs;
    private transient UADev uaDev;
    private transient DevDef devDef;
    private int failAfterSuccessive;
    private transient int errCount;

    public ModbusDevItem(final UADev dev) {
        this.maddrs = new ArrayList<ModbusAddr>();
        this.mbCoilIn = null;
        this.mbCoilOut = null;
        this.mbRegIn = null;
        this.mbRegHold = null;
        this.uaDev = null;
        this.devDef = null;
        this.failAfterSuccessive = 3;
        this.errCount = 0;
        this.uaDev = dev;
        this.devDef = dev.getDevDef();
    }

    public UADev getUADev() {
        return this.uaDev;
    }

    boolean init(final StringBuilder failedr) {
        final List<DevAddr> addrs = this.uaDev.listTagsAddrAll();
        if (addrs == null || addrs.size() <= 0) {
            failedr.append("no access addresses found");
            return false;
        }
        final List<ModbusAddr> tmpads = new ArrayList<ModbusAddr>();
        for (final DevAddr d : addrs) {
            tmpads.add((ModbusAddr) d);
        }
        this.maddrs = tmpads;
        final int devid = (int) this.uaDev.getOrDefaultPropValueLong("modbus_spk", "mdev_addr", 1L);
        this.failAfterSuccessive = this.uaDev.getOrDefaultPropValueInt("timing", "failed_tryn", 3);
        int blocksize_out_coils = this.uaDev.getOrDefaultPropValueInt("block_size", "out_coils", -1);
        if (blocksize_out_coils < 0 && this.devDef != null) {
            blocksize_out_coils = this.devDef.getOrDefaultPropValueInt("block_size", "out_coils", 32);
        }
        if (blocksize_out_coils <= 0) {
            blocksize_out_coils = 32;
        }
        int blocksize_in_coils = this.uaDev.getOrDefaultPropValueInt("block_size", "in_coils", -1);
        if (blocksize_in_coils < 0 && this.devDef != null) {
            blocksize_in_coils = this.devDef.getOrDefaultPropValueInt("block_size", "in_coils", 32);
        }
        if (blocksize_in_coils <= 0) {
            blocksize_in_coils = 32;
        }
        int blocksize_internal_reg = this.uaDev.getOrDefaultPropValueInt("block_size", "internal_reg", -1);
        if (blocksize_internal_reg < 0 && this.devDef != null) {
            blocksize_internal_reg = this.devDef.getOrDefaultPropValueInt("block_size", "internal_reg", 32);
        }
        if (blocksize_internal_reg <= 0) {
            blocksize_internal_reg = 32;
        }
        int blocksize_holding = this.uaDev.getOrDefaultPropValueInt("block_size", "holding", -1);
        if (blocksize_holding < 0 && this.devDef != null) {
            blocksize_holding = this.devDef.getOrDefaultPropValueInt("block_size", "holding", 32);
        }
        if (blocksize_holding <= 0) {
            blocksize_holding = 32;
        }
        final long reqto = this.uaDev.getOrDefaultPropValueLong("timing", "req_to", 100L);
        final long recvto = this.uaDev.getOrDefaultPropValueLong("timing", "recv_to", 200L);
        final long inter_ms = this.uaDev.getOrDefaultPropValueLong("timing", "inter_req", 100L);
        final long scan_intv = this.uaDev.getOrDefaultPropValueLong("timing", "scan_intv", 100L);
        final List<ModbusAddr> coil_in_addrs = this.filterAndSortAddrs((short) 49);
        final List<ModbusAddr> coil_out_addrs = this.filterAndSortAddrs((short) 48);
        final List<ModbusAddr> reg_input_addrs = this.filterAndSortAddrs((short) 51);
        final List<ModbusAddr> reg_hold_addrs = this.filterAndSortAddrs((short) 52);
        if (coil_in_addrs.size() > 0) {
            final ModbusBlock mb = new ModbusBlock(devid, (short) 49, coil_in_addrs, blocksize_in_coils, scan_intv, this.failAfterSuccessive);
            mb.setTimingParam(reqto, recvto, inter_ms);
            if (mb.initReadCmds()) {
                this.mbCoilIn = mb;
            }
        }
        if (coil_out_addrs.size() > 0) {
            final ModbusBlock mb = new ModbusBlock(devid, (short) 48, coil_out_addrs, blocksize_out_coils, scan_intv, this.failAfterSuccessive);
            mb.setTimingParam(reqto, recvto, inter_ms);
            if (mb.initReadCmds()) {
                this.mbCoilOut = mb;
            }
        }
        if (reg_input_addrs.size() > 0) {
            final boolean fwlow32 = this.uaDev.getOrDefaultPropValueBool("data_encod", "fw_low32", true);
            final ModbusBlock mb2 = new ModbusBlock(devid, (short) 51, reg_input_addrs, blocksize_internal_reg, scan_intv, this.failAfterSuccessive).asFirstWordLowIn32Bit(fwlow32);
            mb2.setTimingParam(reqto, recvto, inter_ms);
            if (mb2.initReadCmds()) {
                this.mbRegIn = mb2;
            }
        }
        if (reg_hold_addrs.size() > 0) {
            final boolean fwlow32 = this.uaDev.getOrDefaultPropValueBool("data_encod", "fw_low32", true);
            final ModbusBlock mb2 = new ModbusBlock(devid, (short) 52, reg_hold_addrs, blocksize_holding, scan_intv, this.failAfterSuccessive).asFirstWordLowIn32Bit(fwlow32);
            mb2.setTimingParam(reqto, recvto, inter_ms);
            if (mb2.initReadCmds()) {
                this.mbRegHold = mb2;
            }
        }
        return true;
    }

    public void setModbusProtocal(final ModbusCmd.Protocol p) {
        if (this.mbCoilIn != null) {
            this.mbCoilIn.setModbusProtocal(p);
        }
        if (this.mbCoilOut != null) {
            this.mbCoilOut.setModbusProtocal(p);
        }
        if (this.mbRegIn != null) {
            this.mbRegIn.setModbusProtocal(p);
        }
        if (this.mbRegHold != null) {
            this.mbRegHold.setModbusProtocal(p);
        }
    }

    private List<ModbusAddr> filterAndSortAddrs(final short addrtp) {
        final ArrayList<ModbusAddr> r = new ArrayList<ModbusAddr>();
        for (final ModbusAddr ma : this.maddrs) {
            if (ma.addrTp == addrtp) {
                r.add(ma);
            }
        }
        Collections.sort(r);
        return r;
    }

    boolean doModbusCmd(final ConnPtStream ep) throws Exception {
        boolean ret = true;
        if (this.mbCoilIn != null) {
            if (!this.mbCoilIn.checkDemotionCanRun()) {
                return false;
            }
            if (!this.mbCoilIn.runCmds((IConnEndPoint) ep)) {
                ret = false;
            }
        }
        if (this.mbCoilOut != null) {
            if (!this.mbCoilOut.checkDemotionCanRun()) {
                return false;
            }
            if (!this.mbCoilOut.runCmds((IConnEndPoint) ep)) {
                ret = false;
            }
        }
        if (this.mbRegIn != null) {
            if (!this.mbRegIn.checkDemotionCanRun()) {
                return false;
            }
            if (!this.mbRegIn.runCmds((IConnEndPoint) ep)) {
                ret = false;
            }
        }
        if (this.mbRegHold != null) {
            if (!this.mbRegHold.checkDemotionCanRun()) {
                return false;
            }
            if (!this.mbRegHold.runCmds((IConnEndPoint) ep)) {
                ret = false;
            }
        }
        if (!ret) {
            ++this.errCount;
            if (this.errCount >= this.failAfterSuccessive) {
                this.errCount = this.failAfterSuccessive;
                return false;
            }
        }
        return ret;
    }

    long getLastReadOkDT() {
        long ret = -1L;
        if (this.mbCoilIn != null) {
            final long tmpdt = this.mbCoilIn.getLastReadOkDT();
            if (tmpdt > ret) {
                ret = tmpdt;
            }
        }
        if (this.mbCoilOut != null) {
            final long tmpdt = this.mbCoilOut.getLastReadOkDT();
            if (tmpdt > ret) {
                ret = tmpdt;
            }
        }
        if (this.mbRegIn != null) {
            final long tmpdt = this.mbRegIn.getLastReadOkDT();
            if (tmpdt > ret) {
                ret = tmpdt;
            }
        }
        if (this.mbRegHold != null) {
            final long tmpdt = this.mbRegHold.getLastReadOkDT();
            if (tmpdt > ret) {
                ret = tmpdt;
            }
        }
        return ret;
    }

    void onSnifferCmd(final SnifferCmd sc) {
        final ModbusCmdRead mcr = sc.getFindedCmd();
        final byte[] fdd = sc.getFindedData();
        if (mcr instanceof ModbusCmdReadBits) {
            if (this.mbCoilIn != null) {
                this.mbCoilIn.onSnifferCmd(sc);
            }
            if (this.mbCoilOut != null) {
                this.mbCoilOut.onSnifferCmd(sc);
            }
        } else if (mcr instanceof ModbusCmdReadWords) {
            if (this.mbRegIn != null) {
                this.mbRegIn.onSnifferCmd(sc);
            }
            if (this.mbRegHold != null) {
                this.mbRegHold.onSnifferCmd(sc);
            }
        }
    }

    void doModbusCmdErr() {
        if (this.mbCoilIn != null) {
            this.mbCoilIn.runCmdsErr();
        }
        if (this.mbCoilOut != null) {
            this.mbCoilOut.runCmdsErr();
        }
        if (this.mbRegIn != null) {
            this.mbRegIn.runCmdsErr();
        }
        if (this.mbRegHold != null) {
            this.mbRegHold.runCmdsErr();
        }
    }

    public boolean RT_writeVal(final DevAddr da, final Object v) {
        final ModbusAddr ma = (ModbusAddr) da;
        ModbusBlock mb = null;
        switch (ma.addrTp) {
            case 48: {
                mb = this.mbCoilOut;
                break;
            }
            case 52: {
                mb = this.mbRegHold;
                break;
            }
            default: {
                return false;
            }
        }
        return mb != null && mb.setWriteCmdAsyn(ma, v);
    }
}
