package cn.doraro.flexedge.core.store.record;

import cn.doraro.flexedge.core.UAPrj;
import cn.doraro.flexedge.core.store.SourceJDBC;
import cn.doraro.flexedge.core.store.StoreManager;
import cn.doraro.flexedge.core.store.gdb.connpool.DBConnPool;
import cn.doraro.flexedge.core.store.tssdb.TSSAdapter;
import cn.doraro.flexedge.core.store.tssdb.TSSIO;
import cn.doraro.flexedge.core.store.tssdb.TSSIOSQLite;

public class TSSAdapterPrj extends TSSAdapter {
//	private static final HashMap<String,TSSAdapterPrj> name2recm = new HashMap<>() ;
//	
//	public static TSSAdapter getInstance(UAPrj prj)
//	{
//		String name = prj.getName();
//		TSSAdapterPrj recm = name2recm.get(name) ;
//		if(recm!=null)
//			return recm ;
//		
//		synchronized(TSSAdapter.class)
//		{
//			recm = name2recm.get(name) ;
//			if(recm!=null)
//				return recm ;
//			
//			recm = new TSSAdapterPrj(name) ;
//			name2recm.put(name,recm) ;
//			return recm ;
//		}
//	}

    //String prjName = null ;

    UAPrj prj = null;

    TSSIO recIO = null;

    protected TSSAdapterPrj(UAPrj prj) {
        //this.prjName = prjname ;

        this.prj = prj;//UAManager.getInstance().getPrjByName(prjName) ;

        //this.asTagParams(createTagParams()) ;

        //this.asSavedListener(tssSavedLis);
    }

    @Override
    protected long getSaveIntervalMS() {
        return 1000;
    }

    @Override
    protected boolean RT_init(StringBuilder failedr) {

        getIO();

        if (!super.RT_init(failedr))
            return false;

        return this.prj != null;
    }

    @Override
    protected TSSIO getIO() {
        if (recIO != null)
            return recIO;

        synchronized (this) {
            if (recIO != null)
                return recIO;

            SourceJDBC innersor = StoreManager.getInnerSource(prj.getName() + ".tssdb");
            DBConnPool cp = innersor.getConnPool();

            recIO = new TSSIOSQLite(this);
            StringBuilder failedr = new StringBuilder();
            if (!recIO.initIO(cp, failedr))
                throw new RuntimeException(failedr.toString());

            return recIO;
        }
    }
}
