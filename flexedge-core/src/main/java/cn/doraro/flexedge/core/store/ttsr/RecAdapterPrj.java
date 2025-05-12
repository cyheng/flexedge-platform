package cn.doraro.flexedge.core.store.ttsr;

import cn.doraro.flexedge.core.UAManager;
import cn.doraro.flexedge.core.UAPrj;
import cn.doraro.flexedge.core.store.SourceJDBC;
import cn.doraro.flexedge.core.store.StoreManager;
import cn.doraro.flexedge.core.store.gdb.connpool.DBConnPool;

import java.util.HashMap;

public class RecAdapterPrj extends RecAdapter
{
	private static final HashMap<String,RecAdapterPrj> name2recm = new HashMap<>() ;
	
	public static RecAdapter getInstance(UAPrj prj)
	{
		String name = prj.getName();
		RecAdapterPrj recm = name2recm.get(name) ;
		if(recm!=null)
			return recm ;
		
		synchronized(RecAdapter.class)
		{
			recm = name2recm.get(name) ;
			if(recm!=null)
				return recm ;
			
			recm = new RecAdapterPrj(name) ;
			name2recm.put(name,recm) ;
			return recm ;
		}
	}
	
	String prjName = null ;
	
	UAPrj prj = null ;
	
	RecIO recIO = null ;
	
	protected RecAdapterPrj(String prjname)
	{
		this.prjName = prjname ;
	}

	@Override
	protected boolean RT_init(StringBuilder failedr)
	{
		this.prj = UAManager.getInstance().getPrjByName(prjName) ;
		getIO() ;
		return this.prj!=null;
	}
	
	@Override
	protected RecIO getIO()
	{
		if(recIO!=null)
			return recIO ;
		
		synchronized(this)
		{
			if(recIO!=null)
				return recIO ;
			
			SourceJDBC innersor = StoreManager.getInnerSource(prjName+".ttsr") ;
			DBConnPool cp= innersor.getConnPool() ;
			
			recIO = new RecIOSQLite() ;
			StringBuilder failedr = new StringBuilder() ;
			if(!recIO.initIO(cp,failedr))
				throw new RuntimeException(failedr.toString()) ;
			
			return recIO;
		}
	}
}
