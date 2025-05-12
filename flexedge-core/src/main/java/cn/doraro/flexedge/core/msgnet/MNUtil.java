package cn.doraro.flexedge.core.msgnet;

import cn.doraro.flexedge.core.Config;
import cn.doraro.flexedge.core.util.Convert;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class MNUtil
{
	private static HashMap<String,String> nodetp2pmui = new HashMap<>() ;
	
	public static String UI_getOrLoadNodePM(String node_tp) throws IOException
	{
		if(Config.isDebug())
			return UI_loadNodePM(node_tp) ;
		
		String tmps = nodetp2pmui.get(node_tp) ;
		if(tmps!=null)
			return tmps ;
		
		tmps = UI_loadNodePM(node_tp) ;
		if(tmps!=null)
			nodetp2pmui.put(node_tp,tmps) ;
		return tmps ;
	}
	
	private static String UI_loadNodePM(String node_tp) throws IOException
	{
		File f = new File(Config.getWebappBase()+"/admin/mn/nodes/"+node_tp+"_pm.html") ;
		return Convert.readFileTxt(f) ; 
	}
}
