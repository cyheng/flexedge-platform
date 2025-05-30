package cn.doraro.flexedge.core.conn;

import cn.doraro.flexedge.core.ConnProvider;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;
import cn.doraro.flexedge.core.util.xmldata.XmlData;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ConnPtTcpClient extends ConnPtStream {
    public static ILogger log = LoggerManager.getLogger(ConnPtTcpClient.class);

    public static String TP = "tcp_client";

    String host = null;

    int port = -1;

    /**
     * conn timeout in millissecond
     */
    int connTimeoutMS = 3000;

    Socket sock = null;

    InputStream inputS = null;

    OutputStream outputS = null;
    private long lastChk = -1;
    private int chkSendUrgentCC = 0;

    public ConnPtTcpClient() {
        readNoDataTimeout = 60000;
    }

    public ConnPtTcpClient(ConnProvider cp, String name, String title, String desc) {
        super(cp, name, title, desc);

        readNoDataTimeout = 60000;
    }

    public String getConnType() {
        return "tcp_client";
    }

    @Override
    public XmlData toXmlData() {
        XmlData xd = super.toXmlData();
        xd.setParamValue("host", host);
        xd.setParamValue("port", port);
        xd.setParamValue("conn_to", connTimeoutMS);
        return xd;
    }

    @Override
    public boolean fromXmlData(XmlData xd, StringBuilder failedr) {
        boolean r = super.fromXmlData(xd, failedr);
        this.host = xd.getParamValueStr("host");
        this.port = xd.getParamValueInt32("port", 8081);
        this.connTimeoutMS = xd.getParamValueInt32("conn_to", 3000);
        this.readNoDataTimeout = xd.getParamValueInt64("read_no_to", 60000);
        return r;
    }

    protected void injectByJson(JSONObject jo) throws Exception {
        super.injectByJson(jo);

        this.host = jo.getString("host");
        this.port = jo.getInt("port");
        this.connTimeoutMS = jo.getInt("conn_to");
        this.readNoDataTimeout = jo.optLong("read_no_to", 60000);
    }

    public String getHost() {
        if (host == null)
            return "";
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPortStr() {
        if (port <= 0)
            return "";
        return "" + port;
    }

    public int getConnTimeout() {
        return connTimeoutMS;
    }

    @Override
    protected InputStream getInputStreamInner() {
        return inputS;
    }

    @Override
    protected OutputStream getOutputStreamInner() {
        return outputS;
    }

    public String getStaticTxt() {
        return this.host + ":" + this.port;
    }

    private synchronized boolean connect() {
        if (sock != null) {
            if (sock.isClosed()) {
                try {
                    disconnect();
                } catch (Exception e) {
                }
            }

//			try
//			{//   ****** this code may cause tcp reset with 90s interval *****
//				sock.sendUrgentData(0xFF);
//			}
//			catch (Exception e)
//			{
//				System.out.println(" ConnPtTcpClient will disconnect by sending err:"+e.getMessage()) ;
//				disconnect();
//			}
            return true;
        }


        if (log.isTraceEnabled())
            log.trace(" ConnPtTcpClient try connect to " + host + ":" + port);

        try {

            sock = new Socket(host, port);

            //set recv timeout,it will make read waiting throw timeout
            //sock.setSoTimeout(connTimeoutMS);

            sock.setTcpNoDelay(true);
            sock.setKeepAlive(true);
            inputS = sock.getInputStream();
            outputS = sock.getOutputStream();

            this.fireConnReady();
            return true;
        } catch (Exception ee) {
            if (log.isDebugEnabled()) {
                log.debug(" ConnPtTcpClient will disconnect by connect err:" + ee.getMessage());
                ee.printStackTrace();
            }
            disconnect();
            return false;
        }
    }

    void disconnect() // throws IOException
    {
        if (sock == null)
            return;

        synchronized (this) {
            //System.out.println("ConnPtTcpClient disconnect [" + this.getName());
            try {
                try {
                    if (inputS != null)
                        inputS.close();
                } catch (Exception e) {
                }

                try {
                    if (outputS != null)
                        outputS.close();
                } catch (Exception e) {
                }

                try {
                    if (sock != null)
                        sock.close();
                } catch (Exception e) {
                }

            } finally {
                inputS = null;
                outputS = null;
                sock = null;
            }
        }
    }

    public synchronized void RT_checkConn() {
        if (System.currentTimeMillis() - lastChk < 5000)
            return;
        try {
            connect();

//			chkSendUrgentCC ++ ;
//			if(chkSendUrgentCC>=10)
//			{
//				if(log.isTraceEnabled())
//					log.trace("sendUrgentData to check socket , sock="+sock +"  "+(this.sock!=null?"conn="+sock.isConnected()+" close="+sock.isClosed():""));
//				
//				if(this.sock!=null)
//				{
//					//this.sock.setOOBInline(on);
//					if(log.isTraceEnabled())
//						log.trace("sendUrgentData to check socket");
//					this.sock.sendUrgentData(0);
//				}
//				chkSendUrgentCC = 0 ;
//			}
        }
//		catch ( IOException e)
//		{
//			this.disconnect();
//			if(log.isDebugEnabled())
//				log.debug("sendUrgentData err - "+e.getMessage(), e);
//		}
        finally {
            lastChk = System.currentTimeMillis();
        }
    }

    public String getDynTxt() {
        return "";
    }

    @Override
    public boolean isClosed() {
        return !isConnReady();
    }

    @Override
    public boolean isConnReady() {
        if (sock == null)
            return false;
        return !sock.isClosed();
    }

    public String getConnErrInfo() {
        if (sock == null)
            return "no connection";
        else
            return null;
    }

    @Override
    public void close() throws IOException {
        disconnect();
    }

}
