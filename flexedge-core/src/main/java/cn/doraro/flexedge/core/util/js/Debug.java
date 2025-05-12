package cn.doraro.flexedge.core.util.js;

import cn.doraro.flexedge.core.cxt.JSObMap;
import cn.doraro.flexedge.core.cxt.JsDef;

import java.io.PrintWriter;

@JsDef(name="debug",title="Debug",desc="System Debug",icon="icon_debug")
public class Debug extends JSObMap
{
	PrintWriter pw = null;
	

	public void setOutPipe(PrintWriter pw)
	{
		this.pw = pw ;
	}
	
	@JsDef
	public void print(Object txt)
	{
		if(pw!=null)
		{
			pw.print(txt);
			return ;
		}
		System.out.print(txt);
	}
	
	@JsDef
	public void println(Object txt)
	{
		if(pw!=null)
		{
			pw.println(txt);
			return ;
		}
		System.out.println(txt);
	}
	
}