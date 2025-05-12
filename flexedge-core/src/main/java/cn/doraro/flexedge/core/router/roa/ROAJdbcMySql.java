package cn.doraro.flexedge.core.router.roa;

import cn.doraro.flexedge.core.router.JoinIn;
import cn.doraro.flexedge.core.router.RouterManager;
import cn.doraro.flexedge.core.router.RouterObj;
import cn.doraro.flexedge.core.router.RouterOuterAdp;

public class ROAJdbcMySql extends ROAJdbc
{

	public ROAJdbcMySql(RouterManager rm)
	{
		super(rm);
	}

	@Override
	public String getDBTp()
	{
		return "mysql";
	}

	@Override
	public RouterOuterAdp newInstance(RouterManager rm)
	{
		return new ROAJdbcMySql(rm);
	}

	@Override
	protected void RT_onRecvedFromJoinIn(JoinIn ji, RouterObj recved_txt) throws Exception
	{
		
	}

}
