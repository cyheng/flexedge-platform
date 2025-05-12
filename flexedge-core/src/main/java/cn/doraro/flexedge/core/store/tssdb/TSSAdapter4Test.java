package cn.doraro.flexedge.core.store.tssdb;

import cn.doraro.flexedge.core.UAVal.ValTP;
import cn.doraro.flexedge.core.store.gdb.connpool.DBConnPool;
import cn.doraro.flexedge.core.store.gdb.connpool.DBType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TSSAdapter4Test extends TSSAdapter
{
	TSSIO recIO = null ;
	
	public TSSAdapter4Test()
	{
		
		this.asTagParams(createTagParams()) ;
	}
	
	private List<TSSTagParam> createTagParams()
	{
		ArrayList<TSSTagParam> pms = new ArrayList<>() ;
		pms.add(new TSSTagParam("aa.ss.ii",ValTP.vt_int16,1000,-1)) ;
		pms.add(new TSSTagParam("kk.bb",ValTP.vt_bool,5000,-1)) ;
		pms.add(new TSSTagParam("kk.mm.ff",ValTP.vt_float,500,-1)) ;
		
		pms.add(new TSSTagParam("aa.ss.ii2",ValTP.vt_int16,1000,-1)) ;
		pms.add(new TSSTagParam("kk.bb2",ValTP.vt_bool,5000,-1)) ;
		pms.add(new TSSTagParam("kk.mm.ff2",ValTP.vt_float,500,-1)) ;
		return pms ;
	}
	
	@Override
	protected long getSaveIntervalMS() 
	{
		return 500 ;
	}
	
	@Override
	protected boolean RT_init(StringBuilder failedr)
	{
		getIO() ;
		return super.RT_init(failedr);
	}

	@Override
	protected TSSIO getIO()
	{
		if(recIO!=null)
			return recIO ;
		
		synchronized(this)
		{
			if(recIO!=null)
				return recIO ;
			
			recIO = new TSSIOSQLite(this) ;
			StringBuilder failedr = new StringBuilder() ;
			if(!recIO.initIO(getConnPool(),failedr))
				throw new RuntimeException(failedr.toString()) ;
			
			return recIO;
		}
	}

	private DBConnPool getConnPool()
	{
		String url = "jdbc:sqlite:{$$data_db_sqlite}ttsr._test.db";
		String fp =  "../data/tmp/tssdb_test/" ;
		File f = new File(fp) ;
		if(!f.exists())
			f.mkdirs() ;
		
		url = url.replace("{$$data_db_sqlite}",fp) ;
		System.out.println("db url="+url) ;
		DBConnPool connPool = new DBConnPool(DBType.sqlite, "", "org.sqlite.JDBC", url,null, null,
				null, "0", "10",this.getClass().getClassLoader());
		return connPool ;
	}
	
	public File getDBFile()
	{
		String fp =  "../data/tmp/tssdb_test/ttsr._test.db" ;
		return new File(fp) ;
	}
}
