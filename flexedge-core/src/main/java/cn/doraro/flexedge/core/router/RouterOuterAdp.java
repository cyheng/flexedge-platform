package cn.doraro.flexedge.core.router;

import cn.doraro.flexedge.core.util.Lan;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;
import org.json.JSONObject;

/**
 * Router Outer Adapter
 * which will be extends by Outer Plugin
 *
 * @author jason.zhu
 */
public abstract class RouterOuterAdp extends RouterNode {
    static Lan lan = Lan.getLangInPk(RouterOuterAdp.class);

    static ILogger log = LoggerManager.getLogger(RouterOuterAdp.class);

    public RouterOuterAdp(RouterManager rm) {
        super(rm);
    }

    public static RouterOuterAdp transFromJO(RouterManager rm, JSONObject jo, StringBuilder failedr) {
        String tp = jo.getString("_tp");
        RouterOuterAdp ro = RouterOuterAdpCat.getAdpByTP(tp);
        if (ro == null) {
            failedr.append("no RouterOuter found with tp=" + tp);
            return null;
        }

        RouterOuterAdp ret = ro.newInstance(rm);
        if (!ret.fromJO(jo, failedr))
            return null;
        return ret;
    }

    public String getId() {
        return id;
    }

    public abstract String getTp();

    public final String getTpTitle() {
        return lan.g("adp_" + this.getTp());
    }

    public abstract RouterOuterAdp newInstance(RouterManager rm);

    final void RT_recvedFromJoinIn(JoinIn ji, RouterObj recved_ob) {
        ji.RT_setLastData(recved_ob);

        try {
            RT_onRecvedFromJoinIn(ji, recved_ob);
        } catch (Throwable ee) {
            //log.error(ee.getMessage(), ee);
            this.RT_fireErr("RT_recvedFromJoinIn [" + ji.name + "] err", ee);
            if (log.isDebugEnabled())
                log.debug("RT_recvedFromJoinIn [" + ji.name + "] err", ee);
        }
    }

//	final void sendTxtOutToJoinOut(JoinOut jo,String txt)
//	{
//		for(JoinConn jc : this.belongTo.CONN_getROA2RICMap().values())
//		{
//			String fid = jc.getFromId() ;
//			if(fid.equals(jo.getFromId()))
//			{
//				JoinIn ji = jc.getToJI() ;
//				sendTxtOutToConn(jo,jc,ji,txt) ;
//			}
//		}
//	}

//	final void sendTxtOutToConn(JoinOut jo,JoinConn jc,JoinIn ji,String txt)
//	{
//		String ret = jc.RT_doTrans(txt) ;
//		if(ret==null)
//			return ;//error
//		
//		RouterInnCollator roa = (RouterInnCollator)ji.getBelongNode() ;
//		roa.RT_onRecvedFromJoinIn(ji,ret) ;
//	}

    protected abstract void RT_onRecvedFromJoinIn(JoinIn ji, RouterObj recved_ob) throws Exception;

    protected final void RT_sendToJoinOut(JoinOut jo, RouterObj data)// throws Exception
    {
        jo.RT_setLastData(data);

        for (JoinConn jc : this.belongTo.CONN_getROA2RICMap().values()) {
            String fid = jc.getFromId();
            if (fid.equals(jo.getFromId())) {
                JoinIn ji = jc.getToJI();
                sendOutToConn(jo, jc, ji, data);
            }
        }
    }

    final void sendOutToConn(JoinOut jo, JoinConn jc, JoinIn ji, RouterObj data)// throws Exception
    {
        RouterObj ret = jc.RT_doTrans(data);
        if (ret == null)
            return;//error

        RouterInnCollator ric = (RouterInnCollator) ji.getBelongNode();
        ji.RT_setLastData(ret);
        if (!ric.isEnable()) {
//			if(log.isDebugEnabled())
//				log.debug("");
            return;
        }
        ric.RT_onRecvedFromJoinIn(ji, ret);
    }

    public final boolean RT_start() {
        if (!this.bEnable)
            return false;

        return RT_start_ov();
    }

    //protected abstract void RT_onRecvedFromJoinIn(JoinIn ji,String recved_txt) ;

    protected abstract boolean RT_start_ov();

    public abstract void RT_stop();

    public abstract boolean RT_isRunning();

    /**
     * @author jason.zhu
     */
    public static class LeftIn {

    }


//	public JSONObject toJO()
//	{
//		JSONObject jo = super.toJO() ;
//		jo.put("id", id) ;
//		
//		//jo.put("_tp", this.getTp()) ;
//		jo.put("en", this.bEnable) ;
//		return jo ;
//	}
//	
//	
//	
//	protected boolean fromJO(JSONObject jo,StringBuilder failedr)
//	{
//		super.fromJO(jo, failedr) ;
//		this.id = jo.getString("id") ;
//		
//		this.bEnable = jo.optBoolean("en",true) ;
//		return true ;
//	}

    public static class LeftOut {

    }
}
