package cn.doraro.flexedge.core;

import cn.doraro.flexedge.core.cxt.JSObMap;
import cn.doraro.flexedge.core.util.CompressUUID;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.xmldata.IXmlDataValidator;
import cn.doraro.flexedge.core.util.xmldata.XmlData;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * connection point
 *
 * @author jason.zhu
 */
public abstract class ConnPt extends JSObMap implements IXmlDataValidator {
    public static final int MAX_MON_BLOCKLEN = 1024;
    private static int cc = 0;
    private static long last_id_dt = -1;
    transient ConnProvider belongTo = null;
    private transient int monListMaxLen = 50;
    /**
     * Monitor item list
     */
    private transient LinkedList<MonItem> monItemList = new LinkedList<>();

//	public static class MonItemBS extends MonItem
//	{
//
//		byte[] monBuf = null;//new byte[MAX_MON_BLOCKLEN] ;
//
//		int monLen = 0 ;
//
//		public MonItemBS(boolean binput,String name,byte[] bs)
//		{
//			super(binput,name) ;
//			monBuf = bs ;
//			monLen = bs.length ;
//		}
//		
//		public MonItemBS(boolean binput,byte[] bs)
//		{
//			super(binput,null) ;
//			monBuf = bs ;
//			monLen = bs.length ;
//		}
//		
//		/**
//		 * 
//		 * @return
//		 */
//		public int getMonDataLen()
//		{
//			return monLen ;
//		}
//		
//		public byte[] getMonData()
//		{
//			return monBuf ;
//		}
//		
//		public String getMonTxt()
//		{
//			return Convert.byteArray2HexStr(monBuf,0,monLen," ") ;
//		}
//	}
//	
//	
//	public static class MonItemStr extends MonItem
//	{
//		//String monName = null ;
//		String monStr = null;//new byte[MAX_MON_BLOCKLEN] ;
//
//		public MonItemStr(boolean binput,String name,String str)
//		{
//			super(binput,name) ;
//			//this.monName = name ;
//			this.monStr = str ;
//		}
//		
//
//		public int getMonDataLen()
//		{
//			return monStr.length() ;
//		}
//		
//		
//		public String getMonTxt()
//		{
//			return monStr ;
//		}
//	}
    /**
     * autoid
     */
    private String id = null;
    /**
     * name to fit connection like socket or others
     */
    private String name = null;
    private String title = null;
    private String desc = null;
    private boolean bEnable = true;
    private transient long createDT = -1;
    private transient long lastUsedDT = 0;
    transient private DevDriver bindedDrv = null;

    //private String staticTxt = "" ;

    /**
     * create a empty connpt,it will be injected to old data
     */
    public ConnPt() {
        this.id = CompressUUID.createNewId();
    }

    /**
     * create a new ConnPt
     *
     * @param cp
     * @param name
     * @param title
     */
    public ConnPt(ConnProvider cp, String name, String title, String desc) {
        this.belongTo = cp;
        this.createDT = System.currentTimeMillis();
        this.id = CompressUUID.createNewId();
        this.name = name;
        this.title = title;
        this.desc = desc;
    }

    private synchronized static String createNewId() {
        long t = System.currentTimeMillis();
        if (last_id_dt == t) {
            cc++;
            return t + "_" + cc;
        }

        last_id_dt = t;
        cc = 0;
        return t + "_0";
    }

    public abstract String getConnType();

    @Override
    public XmlData toXmlData() {
        XmlData xd = new XmlData();
        xd.setParamValue("_cpt_tp", this.getConnType());
        xd.setParamValue("id", id);
        xd.setParamValue("name", name);
        xd.setParamValue("title", title);
        if (desc != null)
            xd.setParamValue("desc", desc);
        xd.setParamValue("enable", bEnable);
        return xd;
    }

    public boolean fromXmlData(XmlData xd, StringBuilder failedr) {
        this.id = xd.getParamValueStr("id");
        this.name = xd.getParamValueStr("name");
        this.title = xd.getParamValueStr("title");
        this.desc = xd.getParamValueStr("desc");
        this.bEnable = xd.getParamValueBool("enable", true);
        if (Convert.isNullOrEmpty(this.id) || Convert.isNullOrEmpty(this.name)) {
            failedr.append("no id name");
            return false;
        }
        return true;
    }

    protected void injectByJson(JSONObject jo) throws Exception {
        if (this.isConnReady())
            throw new Exception("Connection is ready");
        String id = jo.getString("id");
        if (Convert.isNotNullEmpty(id))
            this.id = id;
        this.name = jo.getString("name");
        this.title = jo.optString("title");
        if (Convert.isNullOrEmpty(this.title))
            this.title = this.name;
        this.desc = jo.optString("desc");
        this.bEnable = jo.optBoolean("enable");
        if (Convert.isNullOrEmpty(name))
            throw new Exception("input json must has name param");
        StringBuilder sb = new StringBuilder();
        if (!Convert.checkVarName(name, true, sb))
            throw new Exception(sb.toString());
    }

    /**
     * unique id
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * unique conn id
     *
     * @return
     */
    public String getName() {
        if (name == null)
            return "";
        return name;
    }

    public String getTitle() {
        if (title == null)
            return "";
        return title;
    }

//	/**
//	 * ConnPt implements or extender ,may has multi types name
//	 */
//	private transient HashMap<Class<?>,List<String>> connc2tps = new HashMap<>() ;
//	/**
//	 * get conn types that this connpt can have.
//	 * extends implements may make mulit types
//	 * @return
//	 * @throws SecurityException 
//	 * @throws NoSuchFieldException 
//	 * @throws IllegalAccessException 
//	 * @throws IllegalArgumentException 
//	 */
//	private static List<String> getConnTPs(Class<?> c) throws Exception
//	{
//		if(connTPS!=null)
//			return connTPS ;
//		
//		ArrayList<String> rets = new ArrayList<>() ;
//		
//		ArrayList<Class<?>> dcs = new ArrayList<>() ;
//		Class<?> curc = this.getClass() ;
//		do
//		{
//			dcs.add(curc) ;
//			curc = curc.getSuperclass() ;
//		}while(curc!=java.lang.Object.class) ;
//		
//		//Class<?> dcs[] = this.getClass().getDeclaredClasses();
//		for(Class<?> dc:dcs)
//		{
//			try
//			{
//				Field f = dc.getDeclaredField("TP") ;
//				if(f==null)
//					continue ;
//				String v = (String)f.get(dc) ;
//				rets.add(v) ;
//			}
//			catch(NoSuchFieldException ee)
//			{}
//		}
//		connTPS = rets ;
//		return rets ;
//	}

    public String getDesc() {
        if (desc == null)
            return "";
        return desc;
    }

    public boolean isEnable() {
        return bEnable;
    }

    public boolean isValid() {
        return Convert.isNotNullEmpty(this.name);
    }

    /**
     * show static config or param brief information in ui
     *
     * @return
     */
    public abstract String getStaticTxt();

    /**
     * dynamic info
     *
     * @return
     */
    public String getDynTxt() {
        return "";
    }

    public ConnProvider getConnProvider() {
        return belongTo;
    }

    public long getLastUsedDT() {
        return lastUsedDT;
    }

    protected void setUsed() {
        this.lastUsedDT = System.currentTimeMillis();
    }
    //public abstract String getConnTp() ;

    public long getCreatedDT() {
        return createDT;
    }

    public IJoinedNode getJoinedNode() throws Exception {
        UAPrj rep = this.belongTo.getBelongTo();
        ConnJoin cj = ConnManager.getInstance().getConnJoinByConnId(rep.getId(), this.id);
        if (cj == null)
            return null;
        String chid = cj.getRelatedChId();
        UACh ch = rep.getChById(chid);
        String devid = cj.getRelatedDevId();
        if (Convert.isNullOrEmpty(devid))
            return ch;
        return ch.getDevById(devid);
    }

    public UACh getJoinedCh() throws Exception {
        IJoinedNode jn = getJoinedNode();
        if (jn == null)
            return null;

        if (!(jn instanceof UACh))
            return null;

        return (UACh) jn;
    }

    public UADev getJoinedDev() throws Exception {
        IJoinedNode jn = getJoinedNode();
        if (jn == null)
            return null;

        if (!(jn instanceof UADev))
            return null;

        return (UADev) jn;
    }

//	/**
//	 * 
//	 * @return
//	 * @throws Exception 
//	 */
//	public UACh getJoinedCh() throws Exception
//	{
//		UAPrj rep = this.belongTo.getBelongTo();
//		ConnJoin cj = ConnManager.getInstance().getConnJoinByConnId(rep.getId(),this.id) ;
//		if(cj==null)
//			return null ;
//		String chid = cj.getRelatedChId() ;
//		return rep.getChById(chid) ;
//	}

    public List<ConnMsg> getConnMsgs() {
        return null;
    }

    public final ConnMsg getConnMsgById(String msgid) {
        List<ConnMsg> ms = getConnMsgs();
        if (ms == null)
            return null;
        for (ConnMsg cm : ms) {
            if (cm.getId().equalsIgnoreCase(msgid))
                return cm;
        }
        return null;
    }

    protected void RT_connInit() throws Exception {
    }

    public void onJoinedChanged(ConnJoin cj) {
    }

    public abstract void RT_checkConn();//throws Exception;

    public abstract boolean isConnReady();

    public abstract String getConnErrInfo();

    public String RT_getConnRunInfo() {
        return "";
    }

    public boolean hasJoinedNode() throws Exception {
        return getJoinedNode() != null;
    }

    public boolean hasJoinedCh() throws Exception {
        IJoinedNode jn = this.getJoinedNode();
        return jn != null && (jn instanceof UACh);
    }

    public boolean hasJoinedDev() throws Exception {
        IJoinedNode jn = this.getJoinedNode();
        return jn != null && (jn instanceof UADev);
    }

    /**
     * ConnPt is binded by Driver
     *
     * @param drv
     */
    protected void onDriverBinded(DevDriver drv) {
        this.bindedDrv = drv;
        if (this.isConnReady())
            this.fireConnReady();
    }

    /**
     * ConnPt is unbinded by Driver
     *
     * @param drv
     */
    protected void onDriverUnbinded() {
        this.bindedDrv = null;
    }

    public DevDriver getBindedDriver() {
        return this.bindedDrv;
    }

    protected void fireConnReady() {
        onConnReadyOrNot(true);

        if (this.bindedDrv == null)
            return;


        IJoinedNode jn = null;
        try {
            jn = getJoinedNode();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (jn == null)
            return;

        try {
            if (jn instanceof UADev) {
                UADev d = (UADev) jn;
                this.bindedDrv.RT_onConnReady(this, d.getBelongTo(), d);
            } else if (jn instanceof UACh) {
                this.bindedDrv.RT_onConnReady(this, (UACh) jn, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void fireConnInvalid() {
        onConnReadyOrNot(false);
        if (this.bindedDrv == null)
            return;

        try {
            this.bindedDrv.RT_onConnInvalid(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * override it will do something
     *
     * @param r
     */
    protected void onConnReadyOrNot(boolean r) {

    }

    protected void onMonDataRecv(MonItem mi) {
        //MonItemBS curmi = new MonItemBS(binputs,bs) ; ;
        synchronized (this) {
            monItemList.addFirst(mi);
            if (monItemList.size() >= monListMaxLen)
                monItemList.removeLast();
        }
    }

    public boolean setMonListMaxLen(int len) {
        if (len <= 0)
            return false;

        if (len < monListMaxLen) {
            int s = monItemList.size() - len;
            if (s > 0) {
                for (int i = 0; i < s; i++)
                    monItemList.removeFirst();
            }
        }
        monListMaxLen = len;
        return true;
    }

    /**
     * 获得所有的监视列表
     *
     * @return
     */
    public synchronized List<MonItem> getMonitorList() {
        int s = monItemList.size();
        ArrayList<MonItem> rets = new ArrayList<>(s);
        rets.addAll(monItemList);
        return rets;
    }

    public List<MonItem> getMonitorList(long ldt) {
        ArrayList<MonItem> rets = new ArrayList<>();
        for (MonItem mi : monItemList) {
            if (mi.getStartDT() <= ldt)
                break;
            rets.add(mi);
        }
        return rets;
    }

//	protected  void onMonDataRecv(boolean binputs,byte[] bs)
//	{
//		MonItemBS curmi = new MonItemBS(binputs,bs) ; ;
//		synchronized(this)
//		{
//			monItemList.add(curmi) ;
//			if(monItemList.size()>=monListMaxLen)
//				monItemList.removeFirst() ;
//		}
//	}

    public MonItem getMonItemFirst() {
        return monItemList.peekLast();
    }

    public MonItem getMonItemLast() {
        return monItemList.peekFirst();
    }

    public MonItem getMonItemById(String id) {
        for (MonItem mi : monItemList) {
            if (mi.getMonId().equals(id))
                return mi;
        }
        return null;
    }

    public String toString() {
        return this.id + " " + Convert.toFullYMDHMS(new Date(createDT)) + "-" + Convert.toFullYMDHMS(new Date(lastUsedDT));
    }

    public static enum DataTp {
        json(1), str(2), xml(3), bytes(4), html(5);

        private final int val;

        DataTp(int v) {
            val = v;
        }

        public int getInt() {
            return val;
        }

        public String getTitle() {
            switch (val) {
                case 1:
                    return "json";
                case 2:
                    return "string";
                case 3:
                    return "xml";
                case 4:
                    return "bytes";
                case 5:
                    return "html";
                default:
                    return null;
            }
        }
    }

    public static class MonData {
        String name = null;

        int dlen = -1;

        String txt = null;

        DataTp dataTp = DataTp.json;

        long dt = -1;

        private transient byte[] dataBS = null;

        private transient String dataEncd = "UTF-8";

        public MonData(String name, DataTp dtp, String txt) {
            this.name = name;
            if (txt == null)
                txt = "";
            this.dlen = txt.length();
            this.txt = txt;
            this.dataTp = dtp;
            this.dt = System.currentTimeMillis();
        }

        public MonData(String name, byte[] bs) {
            this.name = name;
            this.dlen = bs.length;
            this.dataBS = bs;
            this.dataTp = DataTp.bytes;
            this.dt = System.currentTimeMillis();
        }

        public MonData(String name, DataTp dtp, byte[] bs, String encd) {
            this.name = name;
            this.dlen = bs.length;
            this.dataBS = bs;
            this.dataTp = dtp;
            this.dataEncd = encd;
            if (Convert.isNullOrEmpty(this.dataEncd))
                this.dataEncd = "UTF-8";
            this.dt = System.currentTimeMillis();
        }

        public String getName() {
            return name;
        }

        public int getLen() {
            return dlen;
        }

        public long getDT() {
            return this.dt;
        }

        public String getTxt() throws UnsupportedEncodingException {
            if (txt == null) {
                if (this.dataBS != null) {
                    if (this.dataTp == DataTp.bytes)
                        txt = Convert.byteArray2HexStr(this.dataBS);
                    else// if(this.dataTp==DataTp.xml)
                        txt = new String(this.dataBS, this.dataEncd);
                }

                if (txt == null)
                    txt = "";
            }
            return txt;
        }

        public DataTp getDataTp() {
            return dataTp;
        }

        public void writeToJSON(Writer w, boolean bdetail) throws IOException {
            w.write("{\"n\":\"" + name + "\",\"len\":" + dlen + ",\"tp\":\"" + dataTp.toString() + "\"");
            if (bdetail || this.getLen() < 50) {
                w.write(",\"txt\":\"");
                String mtxt = this.getTxt();
                w.write(Convert.plainToJsStr(mtxt));
                w.write("\"");
            }
            w.write("}");
        }
    }

    /**
     * 存放上下行数据
     */
    public static class MonItem {
        String id = null;
        /**
         * true-input  false=output
         */
        boolean bInput = true;
        /**
         * first data dt
         */
        long stDT = -1;

        /**
         * last data dt
         */
        long endDT = -1;

        String monName = null;

        MonData[] monDatas = null;

        public MonItem(boolean binput, String name, MonData[] mds) {
            this.id = createNewId();
            bInput = binput;
            endDT = stDT = System.currentTimeMillis();
            this.monName = name;
            this.monDatas = mds;
        }

        public String getMonId() {
            return this.id;
        }

        /**
         * mon name like topic
         *
         * @return
         */
        public String getMonName() {
            if (monName == null)
                return "";
            return monName;
        }

        public MonData[] getMonDatas() {
            return monDatas;
        }

        public MonData getMonDataSource() {
            if (monDatas == null || monDatas.length <= 0)
                return null;
            return monDatas[0];
        }

        public boolean isInput() {
            return bInput;
        }

        public boolean isOutput() {
            return !bInput;
        }

        public long getStartDT() {
            return stDT;
        }

        public long getEndDT() {
            return endDT;
        }


        public void writeJsonOut(Writer w, boolean bdetail) throws IOException {
            w.write("{\"id\":\"" + this.id + "\",\"dt\":" + stDT + ",\"bin\":" + bInput + ",\"n\":\"" + this.monName + "\"");
            w.write(",\"datas\":[");
            boolean bfirst = true;
            for (MonData md : this.monDatas) {
                if (bfirst) bfirst = false;
                else w.write(",");

                md.writeToJSON(w, bdetail);
            }
            w.write("]} ");
        }
    }

}
