package cn.doraro.flexedge.core.conn;

import cn.doraro.flexedge.core.Config;
import cn.doraro.flexedge.core.ConnProvider;
import cn.doraro.flexedge.core.ConnPt;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.Env;
import cn.doraro.flexedge.core.util.xmldata.XmlData;
import com.fazecast.jSerialComm.SerialPort;
import gnu.io.CommPortIdentifier;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

public class ConnProCOM extends ConnProvider {
    private static Boolean bRxTx = null;

    public ConnProCOM() {

    }

    public static boolean usingRxTx() {
        if (bRxTx != null)
            return bRxTx;

        Element ele = Config.getConfElement("system");
        if (ele != null && "true".equalsIgnoreCase(ele.getAttribute("rxtx"))) {
            bRxTx = true;
            return true;
        }

        bRxTx = Env.isJVM_Win32();
        return bRxTx;
//		if(bRxTx!=null)
//			return bRxTx ;
//


    }

    public static List<String> listSysComs() {
        if (usingRxTx()) {
            try {
                List<String> systemPorts = new ArrayList<>();
                //获得系统可用的端口
                Enumeration<CommPortIdentifier> portList = (Enumeration<CommPortIdentifier>) CommPortIdentifier.getPortIdentifiers();
                while (portList.hasMoreElements()) {
                    String portName = portList.nextElement().getName();//获得端口的名字
                    systemPorts.add(portName);
                }
                return systemPorts;
            } catch (Throwable e) {
                e.printStackTrace();
                return new ArrayList<String>(0);
            }
        } else {
            SerialPort[] serialPorts = SerialPort.getCommPorts();
            List<String> portNameList = new ArrayList<String>();
            for (SerialPort serialPort : serialPorts) {
                portNameList.add(serialPort.getSystemPortName());
            }
            portNameList = portNameList.stream().distinct().collect(Collectors.toList());
            return portNameList;
        }
    }

    @Override
    public String getProviderType() {
        return "com";
    }

    @Override
    public String getProviderTpt() {
        return "COM";
    }

    public boolean isSingleProvider() {
        return true;
    }

    @Override
    public Class<? extends ConnPt> supportConnPtClass() {
        return ConnPtCOM.class;
    }

    public String getName() {
        String n = super.getName();
        if (Convert.isNotNullEmpty(n))
            return n;
        return "";
    }

    public String getTitle() {
        String n = super.getTitle();
        if (Convert.isNotNullEmpty(n))
            return n;
        return "COM";
    }

    public boolean fromXmlData(XmlData xd, StringBuilder errsb) {
        if (!super.fromXmlData(xd, errsb))
            return false;

        return true;
    }

    public XmlData toXmlData() {
        XmlData xd = super.toXmlData();

        return xd;
    }


    public void disconnAll() //throws IOException
    {
        for (ConnPt ci : this.listConns()) {
            try {
                ConnPtCOM cc = (ConnPtCOM) ci;
                cc.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void stop() {
        super.stop();

        disconnAll();
    }

    @Override
    protected long connpRunInterval() {
        return 3000;
    }


    @Override
    protected void connpRunInLoop() throws Exception {
        for (ConnPt ci : this.listConns()) {
            ConnPtCOM citc = (ConnPtCOM) ci;
            citc.RT_checkConn();
        }
    }

}