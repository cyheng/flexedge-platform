// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.server;

import cn.doraro.flexedge.core.Config;
import java.util.StringTokenizer;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.InputStream;
import cn.doraro.flexedge.core.util.logger.LoggerManager;
import java.io.BufferedReader;
import cn.doraro.flexedge.core.util.logger.ILogger;

public class ServerCtrlHandler
{
    static ILogger log;
    BufferedReader in;
    
    static {
        ServerCtrlHandler.log = LoggerManager.getLogger((Class)ServerCtrlHandler.class);
    }
    
    public ServerCtrlHandler(final InputStream cmdinput) {
        this.in = null;
        this.in = new BufferedReader(new InputStreamReader(cmdinput));
    }
    
    public void handle() throws Exception {
        System.out.print("iottree->");
        String inputLine;
        while ((inputLine = this.in.readLine()) != null) {
            try {
                final StringTokenizer st = new StringTokenizer(inputLine, " ", false);
                final String[] cmds = new String[st.countTokens()];
                for (int i = 0; i < cmds.length; ++i) {
                    cmds[i] = st.nextToken();
                }
                if (cmds.length == 0) {
                    continue;
                }
                if ("exit".equals(cmds[0]) || "disconnect".equalsIgnoreCase(cmds[0])) {
                    return;
                }
                if ("exit_server".equals(cmds[0])) {
                    System.exit(0);
                }
                else if ("?".equals(cmds[0]) || "help".equalsIgnoreCase(cmds[0])) {
                    System.out.println("exit - stop server and exit!");
                    System.out.println("ver - show ver!");
                }
                else if ("ver".equals(cmds[0])) {
                    System.out.println("IOT-Tree Server,Version:" + Config.getVersion());
                }
                else {
                    System.out.println("unknow cmd , using ? or help !");
                }
            }
            catch (final Exception _e) {
                _e.printStackTrace();
                continue;
            }
            finally {
                System.out.print("iottree->");
            }
            System.out.print("iottree->");
        }
    }
}
