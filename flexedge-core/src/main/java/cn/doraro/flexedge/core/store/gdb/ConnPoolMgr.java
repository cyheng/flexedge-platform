package cn.doraro.flexedge.core.store.gdb;

import cn.doraro.flexedge.core.store.gdb.connpool.IConnPool;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Set;

public class ConnPoolMgr {
    static IConnPool defaultConnPool = null;

    /**
     * ȱʡ��ElementԪ�أ�����Ϊ�������������µ����ӳ�
     */
    static Element defaultPoolEle = null;


    static String urlDomainPrefix = null;
    static String urlDomainSuffix = null;
    /**
     * �ڶ�������£���id��ȷʵ���ӳص�
     */
    static HashMap<Integer, IConnPool> domain2Pool = new HashMap<Integer, IConnPool>();

    static HashMap<String, IConnPool> dbname2pool = new HashMap<String, IConnPool>();


    public static IConnPool getConnPool(String dbname) {
        IConnPool cp = dbname2pool.get(dbname);
        if (cp != null)
            return cp;

        return defaultConnPool;
    }

    public static Set<String> getConnPoolNames() {
        return dbname2pool.keySet();
    }

}
