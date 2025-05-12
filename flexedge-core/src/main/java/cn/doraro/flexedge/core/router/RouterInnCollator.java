package cn.doraro.flexedge.core.router;

import cn.doraro.flexedge.core.UATag;
import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.cxt.UAContext;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.ILang;
import cn.doraro.flexedge.core.util.Lan;
import cn.doraro.flexedge.core.util.queue.HandleResult;
import cn.doraro.flexedge.core.util.queue.IObjHandler;
import cn.doraro.flexedge.core.util.queue.QueueThread;
import org.json.JSONObject;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * inner collator which organize tags data
 * or receive outer data to update tags
 *
 * @author jason.zhu
 */
public abstract class RouterInnCollator extends RouterNode implements ILang {
    static Lan lan = Lan.getLangInPk(RouterInnCollator.class);
    static List<RouterInnCollator> RICS = Arrays.asList(new RICSelTags(null), new RICFilterTags(null), new RICDef(null), new RICRunTime(null));
    long outIntervalMS = 30000;
    Thread thread = null;
    QueueThread<TagVal> queTh = null;
    private IObjHandler<TagVal> objH = new IObjHandler<TagVal>() {

        @Override
        public void initHandler() {

        }

        @Override
        public int processFailedRetryTimes() {
            return 0;
        }

        @Override
        public long processRetryDelay(int retrytime) {
            return 0;
        }

        @Override
        public HandleResult processObj(TagVal o, int retrytime) throws Exception {
            RT_runOnChgTagVal(o);
            return HandleResult.Succ;
        }

        @Override
        public long handlerInvalidWait() {
            return 0;
        }

        @Override
        public void processObjDiscard(TagVal o) throws Exception {

        }

    };
    private transient UAContext cxt = null;

    public RouterInnCollator(RouterManager rm) {
        super(rm);
    }

    public static RouterInnCollator transFromJO(RouterManager rm, JSONObject jo, StringBuilder failedr) {
        String tp = jo.getString("_tp");
        if (Convert.isNullOrEmpty(tp))
            return null;

        RouterInnCollator dp = newInstanceByTp(rm, tp);
//		switch(tp)
//		{
//		case RICSelTags.TP:
//			dp = new RICSelTags(rm) ;
//			break ;
//		case RICDef.TP:
//			dp = new RICDef(rm) ;
//			break ;
//		case RICRunTime.TP:
//			dp = new RICRunTime(rm) ;
//			break ;
//		default:
//			break ;
//		}

        if (dp == null)
            return null;

        if (!dp.fromJO(jo, failedr))
            return null;
        return dp;
    }

    public static List<RouterInnCollator> listRICAll() {
        return RICS;
    }

    public static RouterInnCollator newInstanceByTp(RouterManager rm, String tp) {
        for (RouterInnCollator ric : RICS) {
            if (ric.getTp().equals(tp)) {
                return ric.newInstance(rm);
            }
        }
        return null;
    }

    public String getTpTitle() {
        return g("ric_" + this.getTp());
    }

    public long getOutIntervalMS() {
        return this.outIntervalMS;
    }

    protected abstract RouterInnCollator newInstance(RouterManager rm);

    public abstract OutStyle getOutStyle();
    //public abstract String pullOut(String join_out_name) throws Exception;

    public final List<JoinOut> getConnectedJoinOuts() {
        List<JoinOut> jos = this.getJoinOutList();
        if (jos == null || jos.size() <= 0)
            return null;

        HashSet<String> fidset = new HashSet<>();
        for (JoinConn jc : this.belongTo.CONN_getROA2RICMap().values()) {
            String fid = jc.getFromId();
            fidset.add(fid);
        }

        ArrayList<JoinOut> rets = new ArrayList<>();
        for (JoinOut jo : jos) {
            String fid = jo.getFromId();
            if (fidset.contains(fid))
                rets.add(jo);
        }
        return rets;
    }

    /**
     * called by overrider
     *
     * @param jo
     * @param data
     * @throws Exception
     */
    protected final void RT_sendToJoinOut(JoinOut jo, RouterObj data)// throws Exception
    {
        jo.RT_setLastData(data);

        for (JoinConn jc : this.belongTo.CONN_getRIC2ROAMap().values()) {
            String fid = jc.getFromId();
            if (fid.equals(jo.getFromId())) {
                JoinIn ji = jc.getToJI();
                sendOutToConn(jo, jc, ji, data);
            }
        }
    }

    private final void sendOutToConn(JoinOut jo, JoinConn jc, JoinIn ji, RouterObj data)// throws Exception
    {
        RouterObj ret = jc.RT_doTrans(data);
        if (ret == null)
            return;//error

        RouterOuterAdp roa = (RouterOuterAdp) ji.getBelongNode();

        ji.RT_setLastData(ret);
        if (!roa.isEnable())
            return;
        roa.RT_recvedFromJoinIn(ji, ret);
    }

    protected abstract void RT_onRecvedFromJoinIn(JoinIn ji, RouterObj recved);

    public synchronized boolean RT_start() {
        if (!this.bEnable)
            return false;

        OutStyle os = getOutStyle();
        switch (os) {
            case interval:
                if (thread != null)
                    return true;
                thread = new Thread(this::runInterval);
                thread.start();
                return true;
            case on_change:
                if (queTh != null)
                    return true;
                queTh = new QueueThread<>(objH);
                queTh.start();
                return true;
        }
        return false;
    }

    public synchronized void RT_stop() {
        OutStyle os = getOutStyle();
        switch (os) {
            case interval:
                Thread th = thread;
                if (th == null)
                    return;

                th.interrupt();
                thread = null;
                return;
            case on_change:
                //thread = new Thread(this::runOnChange) ;
                QueueThread<?> qt = queTh;
                if (qt == null)
                    return;
                qt.stop();
                queTh = null;
                return;
        }
    }

    public boolean RT_isRunning() {
        OutStyle os = getOutStyle();
        switch (os) {
            case interval:
                return thread != null;
            case on_change:
                return queTh != null && queTh.isRunning();
        }
        return false;
    }

    private void runInterval() {
        try {
            while (thread != null) {
                try {
                    Thread.sleep(this.outIntervalMS);
                } catch (Exception e) {
                }

                try {
                    RT_runInIntvLoop();
                } catch (Exception ee) {
                    ee.printStackTrace();
                    this.RT_fireErr(ee.getMessage(), ee);
                }
            }
        } finally {
            thread = null;
        }
    }

    /**
     * override by sub
     */
    protected void RT_runInIntvLoop() throws Exception {

    }

//	private String rtErr = null ;
//	private long rtDT = -1 ;

    /**
     * override by sub
     */
    protected void RT_runOnChgTagVal(TagVal tv) {

    }

    public boolean DEBUG_triggerOutData(StringBuilder failedr) {
        failedr.append("not supported");
        return false;
    }

    /**
     * called by inner running when found tag value changed
     *
     * @param tag
     * @throws Exception
     */
    public void fireChangedTagVal(UATag tag) throws Exception {
        if (this.queTh == null)
            throw new Exception("no queue thread start or not on_change style");

        UAVal val = tag.RT_getVal();
        TagVal tv = new TagVal(tag, val);
        this.queTh.enqueue(tv);
    }

    public UAContext RT_getContext() throws ScriptException {
        if (cxt != null)
            return cxt;

        synchronized (this) {
            if (cxt != null)
                return cxt;

            cxt = new UAContext(this.belongPrj);
            return cxt;
        }
    }

    public JSONObject toJO() {
        JSONObject jo = super.toJO();
        jo.put("out_intv", this.outIntervalMS);
        return jo;
    }

    protected boolean fromJO(JSONObject jo, StringBuilder failedr) {
        super.fromJO(jo, failedr);
        this.outIntervalMS = jo.optLong("out_intv", 30000);
        if (this.outIntervalMS < 0)
            this.outIntervalMS = 30000;
        return true;
    }

    public static enum OutStyle {
        interval(0), on_change(1);

        private final int val;

        OutStyle(int v) {
            val = v;
        }

        public static OutStyle valOfInt(int v) {
            switch (v) {
                case 1:
                    return on_change;
                default:
                    return interval;
            }
        }

        public int getInt() {
            return val;
        }

        public String getTitle() {
            return lan.g("os_" + this.name());
        }
    }

    public static class TagVal {
        UATag tag;
        UAVal val;

        public TagVal(UATag tag, UAVal val) {
            this.tag = tag;
            this.val = val;
        }

        public UATag getTag() {
            return this.tag;
        }

        public UAVal getVal() {
            return this.val;
        }
    }
}
