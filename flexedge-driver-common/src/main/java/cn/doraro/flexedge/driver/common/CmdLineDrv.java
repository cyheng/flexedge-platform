// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common;

import cn.doraro.flexedge.core.*;
import cn.doraro.flexedge.core.basic.PropGroup;
import cn.doraro.flexedge.core.basic.PropItem;
import cn.doraro.flexedge.core.conn.ConnPtStream;
import cn.doraro.flexedge.core.util.Convert;
import cn.doraro.flexedge.core.util.Lan;
import cn.doraro.flexedge.driver.common.clh.ApcSmartUPS;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CmdLineDrv extends DevDriver {
    static List<DevDriver> SUB_DRVS;

    static {
        CmdLineDrv.SUB_DRVS = null;
    }

    CmdLineHandler handler;
    long recvTO;
    private Thread recvTh;
    private ConnPtStream connPtS;
    private InputStream connInputS;
    private Runnable recvRunner;

    public CmdLineDrv() {
        this.handler = null;
        this.recvTO = 3000L;
        this.recvTh = null;
        this.connPtS = null;
        this.connInputS = null;
        this.recvRunner = new Runnable() {
            @Override
            public void run() {
                CmdLineDrv.this.doRecv();
            }
        };
    }

    static List<CmdLineDrv> loadJsDrvs() {
        final ArrayList<CmdLineDrv> rets = new ArrayList<CmdLineDrv>();
        final File f = new File(String.valueOf(Config.getDataDirBase()) + "/dev_drv/cmd_line_js/list.json");
        try {
            final String txt = Convert.readFileTxt(f, "UTF-8");
            if (Convert.isNullOrEmpty(txt)) {
                return rets;
            }
            final JSONArray jarr = new JSONArray(txt);
            for (int len = jarr.length(), i = 0; i < len; ++i) {
                final JSONObject jo = jarr.getJSONObject(i);
                final CmdLineHandlerJS cmh = new CmdLineHandlerJS(jo);
                final CmdLineDrv cld = new CmdLineDrv().asHandler(cmh);
                rets.add(cld);
            }
            return rets;
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static List<DevDriver> listDrivers() {
        if (CmdLineDrv.SUB_DRVS != null) {
            return CmdLineDrv.SUB_DRVS;
        }
        final ArrayList<DevDriver> rets = new ArrayList<DevDriver>();
        rets.add(new CmdLineDrv().asHandler(new ApcSmartUPS()));
        final List<CmdLineDrv> ss = loadJsDrvs();
        rets.addAll(ss);
        return CmdLineDrv.SUB_DRVS = rets;
    }

    public long getRecvTimeOut() {
        return this.recvTO;
    }

    public CmdLineDrv asHandler(final CmdLineHandler h) {
        this.handler = h;
        return h.belongTo = this;
    }

    protected boolean initDriver(final StringBuilder failedr) throws Exception {
        if (!super.initDriver(failedr)) {
            return false;
        }
        final Object pv = this.getBelongToCh().getPropValue("cmd_line", "recv_to");
        if (pv != null && pv instanceof Number) {
            this.recvTO = ((Number) pv).longValue();
            if (this.recvTO <= 0L) {
                this.recvTO = 3000L;
            }
        }
        return this.handler.init(this, failedr);
    }

    public boolean checkPropValue(final String groupn, final String itemn, final String strv, final StringBuilder failedr) {
        return true;
    }

    public DevDriver copyMe() {
        final CmdLineHandler h = this.handler.copyMe();
        final CmdLineDrv drv = new CmdLineDrv();
        drv.asHandler(h);
        return drv;
    }

    public String getName() {
        return this.handler.getName();
    }

    public String getTitle() {
        return this.handler.getTitle();
    }

    protected List<DevDriver> supportMultiDrivers() {
        return listDrivers();
    }

    public boolean isConnPtToDev() {
        return false;
    }

    public Class<? extends ConnPt> supportConnPtClass() {
        return (Class<? extends ConnPt>) ConnPtStream.class;
    }

    public boolean supportDevFinder() {
        return false;
    }

    public List<PropGroup> getPropGroupsForDevDef() {
        return null;
    }

    public List<PropGroup> getPropGroupsForCh(final UACh ch) {
        final ArrayList<PropGroup> pgs = new ArrayList<PropGroup>();
        PropGroup gp = null;
        final Lan lan = Lan.getPropLangInPk((Class) this.getClass());
        gp = new PropGroup("cmd_line", lan);
        gp.addPropItem(new PropItem("recv_to", lan, PropItem.PValTP.vt_int, false, (String[]) null, (Object[]) null, (Object) 3000));
        pgs.add(gp);
        return pgs;
    }

    public List<PropGroup> getPropGroupsForDevInCh(final UADev d) {
        return null;
    }

    public DevAddr getSupportAddr() {
        return null;
    }

    private synchronized void startRecv() {
        if (this.recvTh != null) {
            return;
        }
        (this.recvTh = new Thread(this.recvRunner)).start();
    }

    private synchronized void stopRecv() {
        if (this.recvTh == null) {
            return;
        }
        this.recvTh.interrupt();
        this.recvTh = null;
    }

    protected void afterDriverRun() throws Exception {
        super.afterDriverRun();
        this.stopRecv();
    }

    private void checkReadStart(final InputStream inputs, final byte[] starts) throws IOException {
        if (starts == null || starts.length <= 0) {
            return;
        }
        int idx = 0;
        do {
            final int c = inputs.read();
            final int sv = starts[idx] & 0xFF;
            if (sv == c) {
                ++idx;
            } else {
                idx = 0;
            }
        } while (idx < starts.length);
    }

    private byte[] checkReadEnd(final InputStream inputs, final byte[] ends, final int max_len) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int idx = 0;
        do {
            final int c = inputs.read();
            final int sv = ends[idx] & 0xFF;
            if (sv == c) {
                ++idx;
            } else {
                idx = 0;
            }
            baos.write(c);
            if (baos.size() >= max_len) {
                throw new IOException("recv length is reach " + max_len + " before end");
            }
        } while (idx < ends.length);
        return baos.toByteArray();
    }

    private void doRecv() {
        try {
            final InputStream inputs = this.connInputS;
            final String startstr = this.handler.getBeginStr();
            byte[] starts = null;
            if (startstr != null) {
                starts = startstr.getBytes();
            }
            final String endstr = this.handler.getEndStr();
            final byte[] ends = endstr.getBytes();
            final int max_len = this.handler.getRecvMaxLen();
            while (this.recvTh != null) {
                if (inputs.available() <= 0) {
                    try {
                        Thread.sleep(1L);
                    } catch (final Exception ex) {
                    }
                } else {
                    this.checkReadStart(inputs, starts);
                    if (inputs.available() <= 0) {
                        Thread.sleep(1L);
                    } else {
                        final byte[] ret = this.checkReadEnd(inputs, ends, max_len);
                        this.handler.RT_onRecved(ret);
                    }
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
            if (CmdLineDrv.log.isDebugEnabled()) {
                CmdLineDrv.log.error("CmdLineDrv " + this.getName() + " doRecv Err", (Throwable) e);
            }
            return;
        } finally {
            this.recvTh = null;
        }
        this.recvTh = null;
    }

    protected void RT_onConnReady(final ConnPt cp, final UACh ch, final UADev dev) throws Exception {
        this.connPtS = (ConnPtStream) cp;
        this.connInputS = this.connPtS.getInputStream();
        this.startRecv();
        this.handler.RT_onConned((ConnPtStream) cp);
    }

    protected void RT_onConnInvalid(final ConnPt cp, final UACh ch, final UADev dev) throws Exception {
        this.stopRecv();
        this.handler.RT_onDisconn((ConnPtStream) cp);
        if (this.connPtS == cp) {
            this.connPtS = null;
        }
    }

    protected boolean RT_useLoopNoWait() {
        return this.handler.RT_useNoWait();
    }

    protected boolean RT_runInLoop(final UACh ch, final UADev dev, final StringBuilder failedr) throws Exception {
        return true;
    }

    protected boolean RT_runInLoopNoWait(final UACh ch, final UADev dev, final StringBuilder failedr) throws Exception {
        this.handler.RT_runInLoop(this.connPtS);
        return true;
    }

    public boolean RT_writeVal(final UACh ch, final UADev dev, final UATag tag, final DevAddr da, final Object v) {
        return false;
    }

    public boolean RT_writeVals(final UACh ch, final UADev dev, final UATag[] tags, final DevAddr[] da, final Object[] v) {
        return false;
    }

    public void RT_fireDrvWarn(final String msg) {
        super.RT_fireDrvWarn(msg);
        System.out.println("Warn: " + msg);
    }
}
