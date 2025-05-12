// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common.modbus;

import cn.doraro.flexedge.core.util.xmldata.IXmlDataable;
import cn.doraro.flexedge.core.util.xmldata.XmlData;

public class ModbusConnInfo implements IXmlDataable {
    String devId;
    String devType;
    private String clientIP;
    private int port;

    public ModbusConnInfo() {
        this.clientIP = null;
        this.port = -1;
        this.devId = null;
        this.devType = null;
    }

    public ModbusConnInfo(final String addrip, final int p, final String devid) {
        this.clientIP = null;
        this.port = -1;
        this.devId = null;
        this.devType = null;
        this.clientIP = addrip;
        this.port = p;
        this.devId = devid;
    }

    public String getClientIPAddr() {
        return this.clientIP;
    }

    public int getClientPort() {
        return this.port;
    }

    public String getDevId() {
        if (this.devId == null) {
            return "";
        }
        return this.devId;
    }

    public String getDevType() {
        if (this.devType == null) {
            return "";
        }
        return this.devType;
    }

    @Override
    public String toString() {
        return "[" + this.devId + "#" + this.devType + "] " + this.clientIP + ":" + this.port;
    }

    public XmlData toXmlData() {
        final XmlData xd = new XmlData();
        xd.setParamValue("client_ip", (Object) this.clientIP);
        xd.setParamValue("client_port", (Object) this.port);
        if (this.devId != null) {
            xd.setParamValue("dev_id", (Object) this.devId);
        }
        if (this.devType != null) {
            xd.setParamValue("dev_type", (Object) this.devType);
        }
        return xd;
    }

    public void fromXmlData(final XmlData xd) {
        this.clientIP = xd.getParamValueStr("client_ip");
        this.port = xd.getParamValueInt32("client_port", -1);
        this.devId = xd.getParamValueStr("dev_id");
        this.devType = xd.getParamValueStr("dev_type");
    }
}
