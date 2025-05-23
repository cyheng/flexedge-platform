package cn.doraro.flexedge.core.sim;

import cn.doraro.flexedge.core.UATag;
import cn.doraro.flexedge.core.cxt.JsProp;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;
import cn.doraro.flexedge.core.util.xmldata.data_class;
import cn.doraro.flexedge.core.util.xmldata.data_obj;
import cn.doraro.flexedge.core.util.xmldata.data_val;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * a slave channel will match a bus which will has one or more devices
 *
 * @author jason.zhu
 */
@data_class
public abstract class SimChannel extends SimNode implements Runnable {
    private static final String[] CH_CNS = new String[]{"cn.doraro.flexedge.driver.common.modbus.sim.SlaveChannel"};
    static private LinkedHashMap<String, SimChannel> TP2CH = null;
    @data_val(param_name = "en")
    protected boolean bEnable = true;
    @data_obj(param_name = "devs")
    ArrayList<SimDev> devs = new ArrayList<>();
    transient SimInstance belongTo = null;
    @data_obj(param_name = "cp")
    transient SimCP cp = null;
    Thread thread = null;
    private ILogger log = LoggerManager.getLogger(SimChannel.class);

    public SimChannel() {
        //this.id = CompressUUID.createNewId();
    }

    static private LinkedHashMap<String, SimChannel> getTp2Ch() {
        if (TP2CH != null)
            return TP2CH;

        LinkedHashMap<String, SimChannel> t2c = new LinkedHashMap<>();
        for (String cn : CH_CNS) {
            try {
                Class c = Class.forName(cn);
                SimChannel sc = (SimChannel) c.newInstance();
                t2c.put(sc.getTp(), sc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        TP2CH = t2c;
        return t2c;
    }

    public static Collection<SimChannel> listAllChannelTps() {
        return getTp2Ch().values();
    }

    public static SimChannel createNewInstance(String chtp) throws Exception {
        SimChannel sc = getTp2Ch().get(chtp);
        if (sc == null)
            return null;
        return (SimChannel) sc.getClass().newInstance();
    }

    public abstract String getTp();

    public abstract String getTpTitle();

    public abstract SimDev createNewDev();

    public SimChannel withBasic(SimChannel osch) {
        this.name = osch.name;
        this.title = osch.title;
        this.bEnable = osch.bEnable;
        return this;
    }


    public boolean isEnable() {
        return this.bEnable;
    }

    public SimChannel withEnable(boolean b) {
        this.bEnable = b;
        return this;
    }

    public SimCP getConn() {
        return this.cp;
    }

    public void setConn(SimCP c) throws Exception {
        this.cp = c;
        this.cp.asChannel(this);
        this.save();
    }

    public List<SimDev> listDevItems() {
        return devs;
    }

    public SimDev getDev(String id) {
        for (SimDev d : this.devs) {
            if (d.getId().equals(id))
                return d;
        }
        return null;
    }

    public SimDev getDevByName(String n) {
        for (SimDev d : this.devs) {
            if (n.equals(d.getName()))
                return d;
        }
        return null;
    }

    public void setDevItem(int devid, List<UATag> tags) {
        // SimDev sdi = new SimDev();
        // //sdi.asDevTags(devid, tags) ;
        // this.devs.add(sdi) ;
    }

    public SimDev setDevBasic(SimDev sdi) throws Exception {
        String n = sdi.getName();
        if (Convert.isNullOrEmpty(n))
            throw new Exception("name cannot be null or empty");
        StringBuilder sb = new StringBuilder();
        if (!Convert.checkVarName(n, true, sb))
            throw new Exception(sb.toString());

        String devid = sdi.getId();
        SimDev olddev = this.getDev(devid);
        if (olddev != null) {
            SimDev tmpdev = this.getDevByName(n);
            if (tmpdev != null && tmpdev != olddev)
                throw new Exception("Device name [" + n + "] is already existed!");
        } else {
            SimDev tmpch = this.getDevByName(n);
            if (tmpch != null)
                throw new Exception("Device name [" + n + "] is already existed!");
        }

        int s = this.devs.size();
        for (int i = 0; i < s; i++) {
            SimDev dev = this.devs.get(i);
            if (dev.getId().equals(sdi.getId())) {
                dev.name = sdi.name;
                dev.title = sdi.title;
                dev.bEnable = sdi.bEnable;
                dev.init();
                this.save();
                return dev;
            }
        }

        sdi.init();
        this.devs.add(sdi);
        this.save();
        // refreshActions();
        return sdi;
    }

    public SimDev setDevExt(SimDev sdi) throws Exception {
        String id = sdi.getId();

        int s = this.devs.size();
        for (int i = 0; i < s; i++) {
            SimDev dev = this.devs.get(i);
            if (dev.getId().equals(id)) {
                sdi.name = dev.name;
                sdi.title = dev.title;
                sdi.bEnable = dev.bEnable;
                this.devs.set(i, sdi);
                sdi.init();
                this.save();
                return sdi;
            }
        }

        throw new Exception("no device found");
    }

    public void save() throws Exception {
        belongTo.saveChannel(this);
    }

    public boolean init() {
        for (SimDev dev : devs) {
            dev.init();
        }

        if (cp != null) {
            this.cp.asChannel(this);
        }

        if (cp == null)
            return false;

        return true;
    }

    public boolean RT_init(StringBuilder failedr) {
        if (cp == null) {
            failedr.append("no conn found in channel");
            return false;
        }

        if (!cp.RT_init(failedr))
            return false;


        return true;
    }

    protected abstract void RT_runConnInLoop(SimConn conn) throws Exception;

    public void run() {
        try {
            while (thread != null) {
                try {
                    //RT_runInThread();
                    cp.RT_runInLoop();
                } catch (SocketException e) {
                    if (log.isDebugEnabled())
                        log.debug(" warn:" + e.getMessage());
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            // log.error("SlaveConn Broken:" + e.getMessage());

            // System.out.println("MSlaveTcpConn Broken:" + e.getMessage());
            // close() ;
        } finally {
            thread = null;

            // onRunnerStopped();
        }
    }

    public boolean RT_isRunning() {
        return thread != null;
    }

    synchronized public boolean RT_start(StringBuilder failedr) {
        if (thread != null)
            return true;

        if (!RT_init(failedr))
            return false;

        synchronized (this) {
            if (thread != null)
                return true;

            thread = new Thread(this);
            thread.start();
            return true;
        }
    }

    synchronized public void RT_stop() {
        Thread t = thread;
        if (t != null) {
            try {
                t.interrupt();
                thread = null;
            } finally {
                thread = null;
            }
        }

        this.cp.RT_stop();
    }

    protected abstract void onConnOk(SimConn sc);

    protected abstract void onConnBroken(SimConn sc);


    public Object JS_get(String key) {
        Object r = super.JS_get(key);
        if (r != null)
            return r;
        return this.getDevByName(key);
    }

    public List<JsProp> JS_props() {
        List<JsProp> rets = super.JS_props();

        for (SimDev dev : this.listDevItems()) {
            rets.add(new JsProp(dev.getName(), dev, SimDev.class, true, dev.getTitle(), ""));
        }
        return rets;
    }

}
