package cn.doraro.flexedge.core.msgnet.nodes;

import cn.doraro.flexedge.core.msgnet.MNConn;
import cn.doraro.flexedge.core.msgnet.MNMsg;
import cn.doraro.flexedge.core.msgnet.MNNodeMid;
import cn.doraro.flexedge.core.msgnet.RTOut;
import cn.doraro.flexedge.core.util.jt.JSONTemp;
import org.json.JSONObject;

public class NM_PID extends MNNodeMid // implements ILang
{
	@Override
	public String getColor()
	{
		return "#e6d970";
	}
	
	@Override
	public String getIcon()
	{
		return "PK_pid";
	}

	@Override
	public JSONTemp getInJT()
	{
		return null;
	}

	@Override
	public JSONTemp getOutJT()
	{
		return null;
	}

	@Override
	public int getOutNum()
	{
		return 1;
	}
	
//	@Override
	public String getTP()
	{
		return "pid";
	}

	@Override
	public String getTPTitle()
	{
		return "PID (Todo)";
	}

	@Override
	public boolean isParamReady(StringBuilder failedr)
	{
		failedr.append("TODO") ;
		return false;
	}

	@Override
	public JSONObject getParamJO()
	{
		JSONObject jo = new JSONObject() ;
		
		return jo;
	}

	@Override
	protected void setParamJO(JSONObject jo)
	{
		
	}
	
	// --------------
	
	private transient long lastMsgOutMS = -1 ;

	@Override
	protected RTOut RT_onMsgIn(MNConn in_conn, MNMsg msg)
	{
		StringBuilder failedr = new StringBuilder() ;
		if(!this.isParamReady(failedr))
		{
			return null ;
		}
		
		return null;
	}

}
