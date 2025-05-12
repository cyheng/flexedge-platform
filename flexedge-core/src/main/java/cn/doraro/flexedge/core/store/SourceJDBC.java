package cn.doraro.flexedge.core.store;

import cn.doraro.flexedge.core.plugin.PlugDir;
import cn.doraro.flexedge.core.plugin.PlugManager;
import cn.doraro.flexedge.core.store.gdb.connpool.DBConnPool;
import cn.doraro.flexedge.core.store.gdb.connpool.DBType;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.logger.ILogger;
import cn.doraro.flexedge.core.util.logger.LoggerManager;
import cn.doraro.flexedge.core.util.xmldata.data_class;
import cn.doraro.flexedge.core.util.xmldata.data_val;
import org.json.JSONObject;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@data_class
public class SourceJDBC extends Source {
    public static final String EXCHG_TP = "source_jdbc";
    static ILogger log = LoggerManager.getLogger(SourceJDBC.class);
    static LinkedHashMap<String, Drv> name2driver = null;//new LinkedHashMap<>() ;
    @data_val(param_name = "drv_name")
    String drvName = null;
//	
//	private static void regDrv(Drv d)
//	{
//		name2driver.put(d.getName(), d) ;
//	}
//	
//	public static final String DRV_MYSQL = "mysql" ;
//	
//	public static final String DRV_SQLSERVER = "sqlserver" ;
//	
//	static
//	{
//		regDrv(new Drv(DRV_MYSQL,"MySql").asDriverClassName("com.mysql.jdbc.Driver")
//				.asJdbcUrl("jdbc:mysql://{$host}:{$port}/{$db_name}?useUnicode=true&characterEncoding=UTF-8")
//				);
//		
//		regDrv(new Drv(DRV_SQLSERVER,"SQL Server").asDriverClassName("com.microsoft.jdbc.sqlserver.SQLServerDriver")
//				.asJdbcUrl("jdbc:microsoft:sqlserver://{$host}:{$port};DatabaseName={$db_name};SelectMethod=cursor")
//				);
//	}
//	
//	public static Collection<Drv> listDrvs()
//	{
//		return name2driver.values();
//	}
    @data_val(param_name = "db_host")
    String dbHost = null;
    @data_val(param_name = "db_port")
    int dbPort = -1;
    @data_val(param_name = "db_name")
    String dbName = null;
    @data_val(param_name = "db_user")
    String dbUser = null;
    @data_val(param_name = "db_psw")
    String dbPsw = null;
    private transient DBConnPool connPool = null;

    public SourceJDBC() {
        super();
    }

    private static Drv parseDrv(PlugDir pd) {
        Drv d = new Drv();
        d.name = pd.getName();
        d.title = pd.getTitle();
        d.plugDir = pd;
        JSONObject jo = pd.getConfigJO();
        if (jo == null)
            return null;
        d.driverClassName = jo.optString("jdbc_class");
        d.jdbcUrl = jo.optString("jdbc_url");
        if (Convert.isNullOrEmpty(d.driverClassName))
            return null;
        if (Convert.isNullOrEmpty(d.jdbcUrl))
            return null;

        d.defaultPortStr = jo.optString("jdbc_port_default", "");
        String dbt = jo.optString("jdbc_dbtype");
        if (Convert.isNotNullEmpty(dbt))
            d.dbTp = DBType.valueOf(dbt);
        return d;
    }

    public static LinkedHashMap<String, Drv> getName2Driver() {
        if (name2driver != null)
            return name2driver;

        LinkedHashMap<String, Drv> n2d = new LinkedHashMap<>();
        LinkedHashMap<String, PlugDir> n2dir = PlugManager.getInstance().LIB_getPlugs("jdbc");
        if (n2dir == null) {
            name2driver = n2d;
            return n2d;
        }
        for (PlugDir pd : n2dir.values()) {
            Drv d = parseDrv(pd);
            if (d == null)
                continue;
            n2d.put(d.getName(), d);
        }
        name2driver = n2d;
        return n2d;
    }

    public static List<Drv> listJDBCDrivers() {
        LinkedHashMap<String, Drv> n2d = getName2Driver();
        ArrayList<Drv> rets = new ArrayList<Drv>();
        rets.addAll(n2d.values());
        return rets;
    }

//	public StoreJDBC(String n,String t)
//	{
//		super(n,t) ;
//	}

    public static Drv getJDBCDriver(String name) {
        return getName2Driver().get(name);
    }

    public SourceJDBC setJDBCInfo(String drv_name, String db_host, int db_port, String db_name, String db_user, String db_psw) {
        this.drvName = drv_name;
        this.dbHost = db_host;
        this.dbPort = db_port;
        this.dbName = db_name;
        this.dbUser = db_user;
        this.dbPsw = db_psw;
        return this;
    }

    public String getSorTp() {
        return "jdbc";
    }

    public String getSorTpTitle() {
        Drv drv = getJDBCDriver(this.drvName);
        String tt = null;
        if (drv != null)
            tt = drv.getTitle();
        if (Convert.isNullOrEmpty(tt))
            tt = this.drvName;
        return "DB:" + tt;
    }

    public String getDrvName() {
        return this.drvName;
    }

    public String getDBHost() {
        if (this.dbHost == null)
            return "";
        return this.dbHost;
    }

    public int getDBPort() {
        return this.dbPort;
    }

    public String getDBName() {
        if (this.dbName == null)
            return "";
        return this.dbName;
    }

    public String getDBUser() {
        if (this.dbUser == null)
            return "";
        return this.dbUser;
    }

    public String getDBPsw() {
        if (this.dbPsw == null)
            return "";

        return this.dbPsw;
    }

    public String getJDBCUrl() {
        Drv drv = getName2Driver().get(this.drvName);
        return drv.calJdbcUrl(this.dbHost, this.dbPort, this.dbName);
    }

    public String getDBInf() {
        return drvName + ":" + this.dbHost + ":" + this.dbPort + ":" + this.dbName;
    }

    public boolean checkValid(StringBuilder failedr) {
        if (!Convert.checkVarName(this.dbName, "DB Name", false, failedr))
            return false;
        return true;
    }

//	private DBType getDBType()
//	{
//		switch(this.drvName)
//		{
//		case DRV_MYSQL:
//			return DBType.mysql;
//		case DRV_SQLSERVER:
//			return DBType.sqlserver;
//		default:
//			return null ;
//		}
//	}

    public DBConnPool getConnPool() {
        if (connPool != null)
            return connPool;

        Drv drv = getName2Driver().get(this.drvName);
        String url = drv.calJdbcUrl(this.dbHost, this.dbPort, this.dbName);
        connPool = new DBConnPool(drv.dbTp, this.getName(), drv.getDriverClassName(), url, this.dbName, this.dbUser,
                this.dbPsw, "0", "10", drv.plugDir.getOrLoadCL());
        return connPool;
    }

    public boolean checkConn(StringBuilder failedr) {
        DBConnPool dbc = getConnPool();
        if (dbc == null) {
            failedr.append("no connection pool created,may be config error");
            return false;
        }

        Connection conn = null;
        try {
            conn = dbc.getConnection();
            return true;
        } catch (Exception ee) {
            failedr.append(ee.getMessage());
            //ee.printStackTrace();
            if (log.isWarnEnabled())
                log.warn("Data Source [" + this.name + "] check Error.\r\n" + ee.getMessage());

            return false;
        } finally {
            if (conn != null)
                dbc.free(conn);
        }
    }

    @Override
    public String getExchgTP() {
        return EXCHG_TP;
    }

    @Override
    protected JSONObject toExchgPmJO() {
        JSONObject jo = new JSONObject();
        jo.put("drv_name", this.drvName);
        jo.put("db_host", this.dbHost);
        jo.put("db_port", this.dbPort);
        jo.put("db_name", this.dbName);
        jo.put("db_user", this.dbUser);
        jo.put("db_psw", this.dbPsw);
        return jo;
    }

    @Override
    protected boolean fromExchgPmJO(JSONObject pmjo) {
        this.drvName = pmjo.optString("drv_name");
        this.dbHost = pmjo.optString("db_host");
        this.dbPort = pmjo.optInt("db_port", 3306);
        this.dbName = pmjo.optString("db_name");
        this.dbUser = pmjo.optString("db_user");
        this.dbPsw = pmjo.optString("db_psw");
        return true;
    }

    public static class Drv {
        PlugDir plugDir = null;

        String name = null;
        String title = null;

        String driverClassName = null;

        String jdbcUrl = null;

        String defaultPortStr = "";

        DBType dbTp = null;

        public PlugDir getPlugDir() {
            return this.plugDir;
        }

        public String getName() {
            return name;
        }

        public String getTitle() {
            return title;
        }

        public String getDriverClassName() {
            return this.driverClassName;
        }

        public String getDefaultPortStr() {
            if (this.defaultPortStr == null)
                return "";
            return this.defaultPortStr;
        }

        public Drv asDriverClassName(String cn) {
            this.driverClassName = cn;
            return this;
        }

        public String getJdbcUrl() {
            return this.jdbcUrl;
        }

        public Drv asJdbcUrl(String u) {
            this.jdbcUrl = u;
            return this;
        }

        public boolean checkJdbcUrlVar(String var_name) {
            if (this.jdbcUrl == null)
                return false;
            String k = "{$" + var_name + "}";
            return this.jdbcUrl.indexOf(k) >= 0;
        }

        public String calJdbcUrl(String host, int port, String dbname) {
            String tmps = this.jdbcUrl;
            tmps = tmps.replace("{$host}", host);
            tmps = tmps.replace("{$port}", "" + port);
            tmps = tmps.replace("{$db_name}", dbname);

            //String fp =  Config.getDataDirBase()+"db_sqlite/" ;


            String data_dyn_dir = System.getProperty("flexedge.data_dyn_dir");
            if (Convert.isNullOrEmpty(data_dyn_dir))
                throw new RuntimeException("no [flexedge.data_dyn_dir] env property found");

            //String fp =  Config.getDataDynDirBase()+"db_sqlite/" ;
            String fp = data_dyn_dir + "db_sqlite/";
            File f = new File(fp);
            if (!f.exists())
                f.mkdirs();

            tmps = tmps.replace("{$$data_db_sqlite}", fp);
            //tmps = tmps.replace("{$$data_dyn}", Config.) ;

            return tmps;
        }
    }

//	public Connection DB_getConn()
//	{
//		getConnPool().
//	}
}
