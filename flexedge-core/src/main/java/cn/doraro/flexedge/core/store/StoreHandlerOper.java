package cn.doraro.flexedge.core.store;

import cn.doraro.flexedge.core.UATag;

import java.util.List;

/**
 * for operator issue command to system log
 * 
 * @author jason.zhu
 *
 */
public class StoreHandlerOper  extends StoreHandler
{
	public static final String TP = "op" ;
	
	@Override
	public String getTp()
	{
		return TP;
	}

	@Override
	public String getTpTitle()
	{
		return "User Operation logger";
	}

	@Override
	public boolean checkFilterFit(UATag tag)
	{
		return false;
	}

	@Override
	public List<StoreOut> getSupportedOuts()
	{
		return null;
	}
	
}
