// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.server;

import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;
import javax.servlet.ServletContainerInitializer;
import org.apache.jasper.servlet.JasperInitializer;
import cn.doraro.flexedge.core.util.Convert;
import org.apache.catalina.Context;
import java.util.HashMap;
import java.io.File;
import org.apache.catalina.connector.Connector;
import cn.doraro.flexedge.core.UAServer;
import java.util.List;
import cn.doraro.flexedge.core.Config;
import org.apache.catalina.startup.Tomcat;
import cn.doraro.flexedge.core.util.IServerBootComp;

public class ServerTomcat implements IServerBootComp
{
    Tomcat tomcat;
    private transient boolean bRunning;
    
    static {
        System.setProperty("java.util.logging.manager", "org.apache.juli.ClassLoaderLogManager");
        if (Config.isDebug()) {
            System.setProperty("java.util.logging.config.file", "./log/logging_debug.properties");
        }
        else {
            System.setProperty("java.util.logging.config.file", "./log/logging.properties");
        }
        System.setProperty("java.endorsed.dirs", "./tomcat/common/endorsed");
        System.setProperty("catalina.base", "./tomcat");
        System.setProperty("catalina.home", "./tomcat");
        System.setProperty("tomcat.util.scan.StandardJarScanFilter.jarsToSkip", "*");
        System.setProperty("java.io.tmpdir", "./tomcat/temp");
    }
    
    public ServerTomcat() {
        this.tomcat = null;
        this.bRunning = false;
    }
    
    public List<UAServer.WebItem> startTomcat(final ClassLoader cl) throws Exception {
        final Config.Webapps w = Config.getWebapps();
        if (w == null) {
            throw new RuntimeException("no Webapps found!");
        }
        final String CATALINA_HOME = String.valueOf(Config.getConfigFileBase()) + "/tomcat/";
        this.tomcat = new Tomcat();
        if (w.getAjpPort() > 0) {
            final Connector ajpc = new Connector("AJP/1.3");
            ajpc.setPort(w.getAjpPort());
            this.tomcat.getService().addConnector(ajpc);
        }
        if (w.getSslPort() > 0) {
            final Connector sslc = getSslConnector(w.getSslPort());
            this.tomcat.getService().addConnector(sslc);
        }
        if (w.getPort() > 0) {
            final Connector norc = getNorConnector(w.getPort());
            this.tomcat.getService().addConnector(norc);
        }
        this.tomcat.setBaseDir(CATALINA_HOME);
        final String wbase = Config.getWebappBase();
        final File wbf = new File(wbase);
        final HashMap<String, Context> app2cxt = new HashMap<String, Context>();
        final HashMap<String, File> app2ff = new HashMap<String, File>();
        for (final Config.Webapp app : w.getAppList()) {
            final String appn = app.getAppName();
            final String path = app.getPath();
            String fp = String.valueOf(wbase) + "/" + appn;
            if (Convert.isNotNullEmpty(path)) {
                fp = String.valueOf(wbase) + "/" + path;
            }
            final File ff = new File(fp);
            if (!ff.exists()) {
                continue;
            }
            Context cxt = null;
            if ("ROOT".equals(appn)) {
                cxt = this.tomcat.addWebapp("", fp);
            }
            else {
                cxt = this.tomcat.addWebapp("/" + appn, fp);
            }
            app2cxt.put(appn, cxt);
            app2ff.put(appn, ff);
            cxt.addServletContainerInitializer((ServletContainerInitializer)new JasperInitializer(), (Set)null);
        }
        this.tomcat.getConnector();
        System.out.println("web port " + ((w.getPort() > 0) ? (" http:" + w.getPort()) : "") + ((w.getSslPort() > 0) ? ("  https:" + w.getSslPort()) : "") + " tomcat starting ...");
        final long st = System.currentTimeMillis();
        this.tomcat.start();
        System.out.println(" tomcat started . cost [" + (System.currentTimeMillis() - st) + "] ms");
        final ArrayList<UAServer.WebItem> wis = new ArrayList<UAServer.WebItem>();
        for (final Map.Entry<String, Context> n2cxt : app2cxt.entrySet()) {
            final String appn2 = n2cxt.getKey();
            final ClassLoader tmpcl = n2cxt.getValue().getLoader().getClassLoader();
            File webf = app2ff.get(appn2);
            if (webf.isDirectory()) {
                webf = new File(webf, "WEB-INF/");
            }
            final UAServer.WebItem wi = new UAServer.WebItem(appn2, tmpcl, webf);
            wis.add(wi);
        }
        return wis;
    }
    
    private static Connector getNorConnector(final int port) {
        final Connector connector = new Connector();
        connector.setPort(port);
        connector.setScheme("http");
        connector.setAttribute("maxThreads", (Object)"200");
        return connector;
    }
    
    private static Connector getSslConnector(final int port) {
        final Connector connector = new Connector();
        connector.setPort(port);
        connector.setSecure(true);
        connector.setScheme("https");
        connector.setAttribute("keyAlias", (Object)"tomcat");
        connector.setAttribute("keystorePass", (Object)"123456");
        connector.setAttribute("keystoreType", (Object)"JKS");
        connector.setAttribute("keystoreFile", (Object)"../keystore.jks");
        connector.setAttribute("clientAuth", (Object)"false");
        connector.setAttribute("protocol", (Object)"HTTP/1.1");
        connector.setAttribute("sslProtocol", (Object)"TLS");
        connector.setAttribute("maxThreads", (Object)"200");
        connector.setAttribute("protocol", (Object)"org.apache.coyote.http11.Http11AprProtocol");
        connector.setAttribute("SSLEnabled", (Object)true);
        return connector;
    }
    
    public String getBootCompName() {
        return "tomcat";
    }
    
    public void startComp() throws Exception {
        this.startTomcat(Thread.currentThread().getContextClassLoader());
        this.bRunning = true;
    }
    
    public void stopComp() throws Exception {
        if (this.tomcat == null) {
            this.bRunning = false;
            return;
        }
        this.tomcat.stop();
        this.tomcat.destroy();
        this.bRunning = false;
    }
    
    public boolean isRunning() throws Exception {
        return this.bRunning;
    }
}
