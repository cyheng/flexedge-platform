

package cn.doraro.flexedge.driver.common.modbus.sniffer;

import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.basic.ByteOrder;
import cn.doraro.flexedge.core.util.xmldata.DataUtil;
import cn.doraro.flexedge.driver.common.ModbusAddr;
import cn.doraro.flexedge.driver.common.modbus.ModbusCmd;
import cn.doraro.flexedge.driver.common.modbus.ModbusCmdRead;

public class SnifferCmd {
    int devId;
    int fc;
    int regPos;
    int regNum;
    ModbusCmdRead findedCmd;
    byte[] findedData;
    long findedDT;

    public SnifferCmd(final ModbusCmdRead mc) {
        this.devId = -1;
        this.fc = -1;
        this.regPos = -1;
        this.regNum = -1;
        this.findedCmd = null;
        this.findedData = null;
        this.findedDT = -1L;
        this.devId = mc.getDevAddr();
        this.fc = mc.getFC();
        this.regPos = mc.getRegAddr();
        this.regNum = mc.getRegNum();
        this.findedCmd = mc;
    }

    public static String createUniqueId(final int devid, final int fc, final int regpos, final int regnum) {
        return devid + "_" + fc + "_" + regpos + "_" + regnum;
    }

    public String getUniqueId() {
        return createUniqueId(this.devId, this.fc, this.regPos, this.regNum);
    }

    public int getDevId() {
        return this.devId;
    }

    public int getFC() {
        return this.fc;
    }

    public ModbusCmdRead getFindedCmd() {
        return this.findedCmd;
    }

    public int getRespLen() {
        if (this.findedCmd == null) {
            return -1;
        }
        return this.findedCmd.calRespLenRTU();
    }

    public boolean parseResp(final byte[] respbs) {
        final int devid = respbs[0] & 0xFF;
        if (devid != this.findedCmd.getDevAddr()) {
            return false;
        }
        if (this.findedCmd.getFC() != (respbs[1] & 0xFF)) {
            return false;
        }
        final int len = respbs.length;
        final int crc = ModbusCmd.modbus_crc16_check(respbs, len - 2);
        if (respbs[len - 2] != (byte) (crc >> 8 & 0xFF) || respbs[len - 1] != (byte) (crc & 0xFF)) {
            return false;
        }
        System.arraycopy(respbs, 3, this.findedData = new byte[len - 5], 0, len - 5);
        this.findedDT = System.currentTimeMillis();
        return true;
    }

    public byte[] getFindedData() {
        return this.findedData;
    }

    public long getFindedDT() {
        return this.findedDT;
    }

    public Object getValByAddr(final ModbusAddr ma) {
        final UAVal.ValTP vt = ma.getValTP();
        if (vt == null) {
            return null;
        }
        if (vt == UAVal.ValTP.vt_bool) {
            final int regp = ma.getRegPos();
            final int byte_ps = regp / 8;
            if (byte_ps < this.regPos) {
                return null;
            }
            if (byte_ps >= this.regPos + this.regNum) {
                return null;
            }
            final int idx = byte_ps - this.regPos;
            final byte b = this.findedData[idx];
            final boolean r = (b & 2 << byte_ps % 8) > 0;
            return r;
        } else {
            final int regp = ma.getRegPos();
            if (regp < this.regPos) {
                return null;
            }
            final int idx2 = (regp - this.regPos) * 2;
            switch (vt) {
                case vt_int16:
                case vt_uint16: {
                    if (regp + 1 > this.regPos + this.regNum) {
                        return null;
                    }
                    final short shortv = DataUtil.bytesToShort(this.findedData, idx2);
                    if (vt == UAVal.ValTP.vt_uint16) {
                        return shortv & 0xFFFF;
                    }
                    return shortv;
                }
                case vt_int32:
                case vt_uint32: {
                    if (regp + 2 > this.regPos + this.regNum) {
                        return null;
                    }
                    final int intv = DataUtil.bytesToInt(this.findedData, idx2, ByteOrder.ModbusWord);
                    if (vt == UAVal.ValTP.vt_uint32) {
                        return (long) intv & -1L;
                    }
                    return intv;
                }
                case vt_int64: {
                    if (regp + 4 > this.regPos + this.regNum) {
                        return null;
                    }
                    return DataUtil.bytesToLong(this.findedData, idx2, ByteOrder.ModbusWord);
                }
                case vt_float: {
                    if (regp + 2 > this.regPos + this.regNum) {
                        return null;
                    }
                    return DataUtil.bytesToFloat(this.findedData, idx2);
                }
                case vt_double: {
                    if (regp + 4 > this.regPos + this.regNum) {
                        return null;
                    }
                    return DataUtil.bytesToDouble(this.findedData, idx2);
                }
                default: {
                    return null;
                }
            }
        }
    }

    @Override
    public String toString() {
        return this.devId + " fc=" + this.fc + " regnum=" + this.regNum;
    }
}
