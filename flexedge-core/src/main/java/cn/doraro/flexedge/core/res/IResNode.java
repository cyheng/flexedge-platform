package cn.doraro.flexedge.core.res;

import java.io.File;

public interface IResNode
{
	public String getResLibId();
	
	public String getResNodeId() ;
	
	public String getResNodeTitle() ;
	
//	public IResNode getResNodeParent() ;
//	
//	public IResNode getResSubNode(String nid) ;
	
//	public IResCxt getResCxt() ;
	
	public File getResNodeDir() ;
	
	default ResDir getResDir()
	{
		File d = getResNodeDir() ;
		if(d==null)
			return null ;
		return new ResDir(new File(d,"_res/")) ;
	}
	
	default ResDir getNodeDir()
	{
		File d = getResNodeDir() ;
		if(d==null)
			return null ;
		return new ResDir(d) ;
	}
	
//	
//	public ResDir getResDir() ;
//	
//	public IResNode getResNodeSub(String subid) ;
//	
//	public IResNode getResNodeParent() ;
//	
//	
//	public default String getResLibId()
//	{
//		IResCxt cxt=this.getResCxt();
//		if(cxt==null)
//			return null ;
//		return cxt.getResLibId() ;
//	}
	
	public default String getResNodeUID()
	{
		return getResLibId()+"-"+this.getResNodeId() ;
	}
}
