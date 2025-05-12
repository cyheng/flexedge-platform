package cn.doraro.flexedge.core.msgnet.store;

import cn.doraro.flexedge.core.util.Convert;
import org.w3c.dom.Element;

public abstract class StoreSor
{
	String name = null ;
	
	public StoreSor(String name)
	{
		this.name =name ;
	}
	
	public String getName()
	{
		return name ;
	}

	public abstract String getTP() ;
	
	public abstract String getTPTitle() ;
	
	protected boolean fromEle(Element ele)
	{
		this.name = ele.getAttribute("name") ;
		return Convert.isNotNullEmpty(this.name) ;
	}
}
