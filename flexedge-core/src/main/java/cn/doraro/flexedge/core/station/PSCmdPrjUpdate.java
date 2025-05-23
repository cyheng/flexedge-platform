package cn.doraro.flexedge.core.station;

import cn.doraro.flexedge.core.UAManager;
import cn.doraro.flexedge.core.UAPrj;
import cn.doraro.flexedge.core.util.Convert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class PSCmdPrjUpdate extends PSCmd {
    public static final String CMD = "prj_update";

    @Override
    public String getCmd() {
        return CMD;
    }

    /**
     * @param prj
     * @return
     * @throws IOException
     */
    public PSCmdPrjUpdate asToBePackPrj(UAPrj prj) throws IOException {
        String prjname = prj.getName();
        this.asParams(Arrays.asList(prjname));

        //pack prj to zip bytes
        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        UAManager.getInstance().exportPrj(prj.getId(), boas);

        this.asCmdData(boas.toByteArray());

        return this;
    }

    @Override
    public void RT_onRecvedInPlatform(PlatInsWSServer.SessionItem si, PStation ps) throws Exception {
        String prjname = this.getParamByIdx(0);
        if (Convert.isNullOrEmpty(prjname))
            return;

        byte[] zipbs = this.getCmdData();
        if (zipbs == null || zipbs.length <= 0)
            return;

//		String stationid =ps.getId() ;

        // 不允许直接更新项目，动作太大
        PlatInsManager.getInstance().onRecvedStationPrj(ps, prjname, zipbs);
    }

    @Override
    public void RT_onRecvedInStationLocal(StationLocal sl) throws Exception {
        String prjname = this.getParamByIdx(0);
        if (Convert.isNullOrEmpty(prjname))
            return;
        String stationid = sl.getStationId();
        if (prjname.startsWith(stationid + "_"))
            prjname = prjname.substring(stationid.length() + 1);
        byte[] zipbs = this.getCmdData();
        if (zipbs == null || zipbs.length <= 0)
            return;

        UAManager ua = UAManager.getInstance();
        //UAPrj localprj = ua.getPrjByName(prjname) ;
        //ua.im
        StringBuilder failedr = new StringBuilder();
        if (!ua.updateOrAddPrj(zipbs, prjname, failedr))
            System.err.println(" RT_onRecvedInStationLocal err :" + failedr.toString());
    }
}
