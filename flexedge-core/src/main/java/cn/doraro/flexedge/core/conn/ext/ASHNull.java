package cn.doraro.flexedge.core.conn.ext;

import cn.doraro.flexedge.core.basic.NameTitleVal;
import cn.doraro.flexedge.core.conn.ConnProTcpServer;
import cn.doraro.flexedge.core.util.xmldata.XmlData;

import java.net.Socket;
import java.util.HashMap;

public class ASHNull implements ConnProTcpServer.AcceptedSockHandler
{

	@Override
	public String getName()
	{
		return "null";
	}
	
	public String getTitle()
	{
		return "Null" ;
	}

	@Override
	public String checkSockConnId(Socket sock) throws Exception
	{
		return null ;
	}

	@Override
	public int getRecvTimeout()
	{
		return 0;
	}

	@Override
	public int getRecvEndTimeout()
	{
		return 0;
	}

	@Override
	public NameTitleVal[] getParamDefs()
	{
		return null;//need no param
	}
	

	@Override
	public XmlData chkAndCreateParams(HashMap<String, String> pn2strv, StringBuilder failedr)
	{
		return new XmlData(); //return empty
	}

	@Override
	public void setParams(XmlData xd)
	{
		//do nothing
	}

}
