package cn.doraro.flexedge.core.msgnet.store;

import cn.doraro.flexedge.core.msgnet.store.influxdb.InfluxDB_M;
import com.influxdb.client.InfluxDBClient;
import org.w3c.dom.Element;

public class StoreSorInfluxDB extends StoreSor {
    public static final String TP = "influxdb";

    String url;

    String token;

    String org;

    String bucket;

    InfluxDB_M dbM = null;
    InfluxDBClient rtClient = null;

    public StoreSorInfluxDB(String name, InfluxDB_M dbm) {
        super(name);
        this.dbM = dbm;
    }

    @Override
    public String getTP() {
        return TP;
    }

    @Override
    public String getTPTitle() {
        return "InfluxDB";
    }

    // RT

    @Override
    protected boolean fromEle(Element ele) {
        if (!super.fromEle(ele))
            return false;

        url = ele.getAttribute("url");
        token = ele.getAttribute("token");
        org = ele.getAttribute("org");
        bucket = ele.getAttribute("bucket");

        return true;
    }

    public synchronized InfluxDBClient RT_getClient() {
//		if(rtClient!=null)
//			return rtClient ;
//		
//		rtClient =  InfluxDBClientFactory.create(this.url,
//				this.token.toCharArray(),org,bucket);
//		return rtClient ;

        return dbM.RT_getClient();
    }

//	synchronized void RT_close()
//	{
//		if(rtClient!=null)
//		{
//			rtClient.close();
//			rtClient=  null ;
//		}
//	}
}
