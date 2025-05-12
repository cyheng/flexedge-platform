// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.pro.modbuss;

import cn.doraro.flexedge.core.util.xmldata.data_class;
import com.fazecast.jSerialComm.SerialPort;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@data_class
public class SlaveCPCom extends SlaveCP {
    public static int[] BAUDS;
    public static int[] DATABITS;
    public static int[] STOPBITS;
    public static int[] PARITY;
    public static String[] PARITY_TITLE;
    public static String[] PARITY_T;
    public static String[] PARITY_NAME;
    public static int[] FLOWCTL;
    public static String[] FLOWCTL_TITLE;

    static {
        SlaveCPCom.BAUDS = new int[]{110, 300, 600, 1200, 2400, 4800, 9600, 19200, 28800, 38400, 56000, 57600, 115200, 128000, 230400, 256000, 460800, 500000, 512000, 600000, 750000, 921600, 1000000, 1500000, 2000000};
        SlaveCPCom.DATABITS = new int[]{5, 6, 7, 8};
        SlaveCPCom.STOPBITS = new int[]{1, 2};
        SlaveCPCom.PARITY = new int[]{0, 1, 2};
        SlaveCPCom.PARITY_TITLE = new String[]{"None", "Odd", "Even"};
        SlaveCPCom.PARITY_T = new String[]{"N", "O", "E"};
        SlaveCPCom.PARITY_NAME = new String[]{"none", "odd", "even"};
        SlaveCPCom.FLOWCTL = new int[]{0, 1, 2, 3, 4};
        SlaveCPCom.FLOWCTL_TITLE = new String[]{"None", "DTR", "RTS", "RTS DTR", "RTS Always"};
    }

    String comId;
    int baud;
    int dataBits;
    int parity;
    int stopBits;
    int flowCtl;
    COMConn comConn;

    public SlaveCPCom(final MSBus_M bus) {
        super(bus);
        this.comId = null;
        this.baud = 9600;
        this.dataBits = 8;
        this.parity = 0;
        this.stopBits = 1;
        this.flowCtl = 0;
        this.comConn = null;
    }

    public static List<String> listSysComs() {
        final SerialPort[] serialPorts = SerialPort.getCommPorts();
        List<String> portNameList = new ArrayList<String>();
        for (final SerialPort serialPort : serialPorts) {
            portNameList.add(serialPort.getSystemPortName());
        }
        portNameList = portNameList.stream().distinct().collect((Collector<? super Object, ?, List<String>>) Collectors.toList());
        return portNameList;
    }

    @Override
    public String getConnTitle() {
        return this.comId + "(" + this.baud + " " + SlaveCPCom.PARITY_T[this.parity] + " " + this.dataBits + " " + this.stopBits + ")";
    }

    @Override
    public String getCPTp() {
        return "com";
    }

    @Override
    public String getCPTpT() {
        return "COM";
    }

    @Override
    public boolean isValid(final StringBuilder failedr) {
        return true;
    }

    @Override
    public JSONObject toJO() {
        final JSONObject jo = super.toJO();
        jo.putOpt("comid", (Object) this.comId);
        jo.putOpt("baud", (Object) this.baud);
        jo.putOpt("databits", (Object) this.dataBits);
        jo.putOpt("parity", (Object) this.parity);
        jo.putOpt("stopbits", (Object) this.stopBits);
        jo.putOpt("flowctl", (Object) this.flowCtl);
        return jo;
    }

    @Override
    public boolean fromJO(final JSONObject jo) {
        super.fromJO(jo);
        this.comId = jo.optString("comid", "");
        this.baud = jo.optInt("baud", 9600);
        this.dataBits = jo.optInt("databits", 8);
        this.parity = jo.optInt("parity", 0);
        this.stopBits = jo.optInt("stopbits", 1);
        this.flowCtl = jo.optInt("flowctl", 0);
        return true;
    }

    @Override
    public int getConnsNum() {
        return 1;
    }

    @Override
    public List<SlaveConn> getConns() {
        if (this.comConn == null) {
            return null;
        }
        return Arrays.asList(this.comConn);
    }

    private synchronized boolean connectNor() {
        if (this.comConn != null && this.comConn.isOpened()) {
            return true;
        }
        SerialPort serialPort = null;
        try {
            serialPort = SerialPort.getCommPort(this.comId);
            serialPort.setBaudRate(9600);
            int stopb = 1;
            if (this.stopBits == 2) {
                stopb = 3;
            }
            final int pari = this.parity;
            serialPort.setComPortParameters(this.baud, this.dataBits, stopb, pari);
            int flow_ctl = 0;
            switch (this.flowCtl) {
                case 0: {
                    flow_ctl = 0;
                    break;
                }
                case 1: {
                    flow_ctl = 4096;
                    break;
                }
                case 2: {
                    flow_ctl = 1;
                    break;
                }
                case 3: {
                    flow_ctl = 1;
                    break;
                }
            }
            serialPort.setFlowControl(flow_ctl);
            serialPort.setComPortTimeouts(272, -1, -1);
            if (serialPort.openPort()) {
                (this.comConn = new COMConn(this.bus, serialPort)).RT_start();
            }
            return true;
        } catch (final Exception ee) {
            ee.printStackTrace();
            if (this.comConn != null) {
                this.comConn.close();
                this.comConn = null;
            }
            if (serialPort != null) {
                serialPort.closePort();
            }
            return false;
        }
    }

    @Override
    public void RT_init() {
    }

    @Override
    public void RT_runInLoop() {
        this.connectNor();
    }

    @Override
    public String RT_getRunInf() {
        final StringBuilder sb = new StringBuilder();
        final COMConn cc = this.comConn;
        if (cc == null || !cc.isOpened()) {
            sb.append("<span style='color:red'>Not Opened</span>");
        } else {
            sb.append("<span style='color:green'>Opened</span>");
        }
        return sb.toString();
    }

    @Override
    public void RT_stop() {
        final COMConn cc = this.comConn;
        if (cc != null) {
            cc.close();
            this.comConn = null;
        }
    }

    public class COMConn extends SlaveConn {
        SerialPort serialPort;
        InputStream inputs;
        OutputStream outputs;

        public COMConn(final MSBus_M bus, final SerialPort sp) throws IOException {
            super(bus, SlaveCPCom.this);
            this.serialPort = null;
            this.inputs = null;
            this.outputs = null;
            this.serialPort = sp;
            this.inputs = sp.getInputStream();
            this.outputs = sp.getOutputStream();
        }

        public InputStream getConnInputStream() {
            return this.inputs;
        }

        public OutputStream getConnOutputStream() {
            return this.outputs;
        }

        @Override
        public void pulseConn() throws Exception {
        }

        public boolean isOpened() {
            return this.serialPort.isOpen();
        }

        @Override
        public String getConnTitle() {
            return SlaveCPCom.this.getConnTitle();
        }

        @Override
        public void close() {
            try {
                super.close();
            } catch (final Exception ex) {
            }
            try {
                this.inputs.close();
            } catch (final Exception ex2) {
            }
            try {
                this.outputs.close();
            } catch (final Exception ex3) {
            }
            try {
                this.serialPort.closePort();
            } catch (final Exception ex4) {
            }
        }
    }
}
