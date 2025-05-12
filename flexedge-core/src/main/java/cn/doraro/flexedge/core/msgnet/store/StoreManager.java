package cn.doraro.flexedge.core.msgnet.store;

import cn.doraro.flexedge.core.msgnet.store.influxdb.InfluxDB_M;

import java.util.HashMap;


public class StoreManager {
    static final String IDB_FULL_TP = "_storage.influxdb";
    private static StoreManager instance = null;
    HashMap<String, StoreSorInfluxDB> name2idb = new HashMap<>();

    private StoreManager() {
        //loadConf() ;
    }

    public static StoreManager getInstance() {
        if (instance != null)
            return instance;

        synchronized (StoreManager.class) {
            if (instance != null)
                return instance;

            instance = new StoreManager();
            return instance;
        }
    }

    public StoreSorInfluxDB getStoreSorInfluxDB(String resname) {
        StoreSorInfluxDB idb = name2idb.get(resname);
        if (idb != null)
            return idb;

        InfluxDB_M dbm = null;// (InfluxDB_M)IOTPlatformManager.getInstance().getMNManager().findNodeByResName(IDB_FULL_TP, resname) ;
        if (dbm == null) {
            return null;
        }

        idb = new StoreSorInfluxDB(resname, dbm);
        name2idb.put(resname, idb);
        return idb;
    }

//	public List<StoreTb> listStoreTbs()
//	{
//		ArrayList<StoreTb> rets = new ArrayList<>() ;
//		MNManager mnm = null; // IOTPlatformManager.getInstance().getMNManager();
//		
//		if(mnm==null)
//			return null ;
//		
//		for(MNNet net:mnm.listNets())
//		{
//			for(MNNode n:net.getNodeMapAll().values())
//			{
//				if(n instanceof StoreTbWriter_NE)
//				{
//					StoreTbWriter_NE ne = (StoreTbWriter_NE)n ;
//					StoreTb st = ne.getStoreTb() ;
//					if(st==null)
//						continue ;
//					rets.add(st) ;
//				}
//			}
//		}
//		return rets ;
//	}
//	
//	public StoreTb getStoreByName(String name)
//	{
//		MNManager mnm = null; //IOTPlatformManager.getInstance().getMNManager();
//		for(MNNet net:mnm.listNets())
//		{
//			for(MNNode n:net.getNodeMapAll().values())
//			{
//				if(n instanceof StoreTbWriter_NE)
//				{
//					StoreTbWriter_NE ne = (StoreTbWriter_NE)n ;
//					StoreTb st = ne.getStoreTb() ;
//					if(st!=null && name.equals(st.getTbName()))
//						return st;
//				}
//			}
//		}
//		return null ;
//	}


}
