package cn.doraro.flexedge.core.conn.ext;

import cn.doraro.flexedge.core.basic.NameTitleVal;
import cn.doraro.flexedge.core.conn.ConnProTcpServer;
import cn.doraro.flexedge.core.util.InputStreamTimeouter;
import cn.doraro.flexedge.core.util.xmldata.XmlData;

import java.net.Socket;
import java.util.HashMap;


/**
 * using timeout and read first data in accepted socket connection
 * then transfer to string directly
 * @author jason.zhu
 *
 */
public class ASHCommon implements ConnProTcpServer.AcceptedSockHandler
{

	@Override
	public String getName()
	{
		return "common";
	}
	
	public String getTitle()
	{
		return "Common" ;
	}

	@Override
	public String checkSockConnId(Socket sock) throws Exception
	{
		InputStreamTimeouter isto = new InputStreamTimeouter(sock.getInputStream(), 1000, 1024) ;
		int cc = 0 ;
		do
		{
			byte[] bs = isto.readNext() ;
			if(bs!=null)
				return new String(bs).trim() ;
			
			Thread.sleep(5);
			
			cc ++ ;
			if(cc>200*30)
				return null ;
		}
		while(true) ;
	}

	@Override
	public int getRecvTimeout()
	{
		return 0;
	}

	@Override
	public int getRecvEndTimeout()
	{
		// TODO Auto-generated method stub
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
