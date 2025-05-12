package cn.doraro.flexedge.core.router.roa;

import cn.doraro.flexedge.core.router.JoinIn;
import cn.doraro.flexedge.core.router.RouterManager;
import cn.doraro.flexedge.core.router.RouterObj;
import cn.doraro.flexedge.core.router.RouterOuterAdp;

public class ROAJdbcOracle extends ROAJdbc
{

	public ROAJdbcOracle(RouterManager rm)
	{
		super(rm);
	}

	@Override
	public String getDBTp()
	{
		return "oracle";
	}

	@Override
	public RouterOuterAdp newInstance(RouterManager rm)
	{
		return new ROAJdbcOracle(rm);
	}

	@Override
	protected void RT_onRecvedFromJoinIn(JoinIn ji, RouterObj recved_txt) throws Exception
	{
		
	}

}
