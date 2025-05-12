package cn.doraro.flexedge.core.station;

import cn.doraro.flexedge.core.UAManager;
import cn.doraro.flexedge.core.UAPrj;
import cn.doraro.flexedge.core.util.Convert;

import java.util.Arrays;

/**
 * platform 触发 station上载本地项目到platform的指令
 * plaform - > station
 *
 * @author jason.zhu
 */
public class PSCmdPrjUpTrigger extends PSCmd {
    public final static String CMD = "up_prj_trigger";

    @Override
    public String getCmd() {
        return CMD;
    }

    public PSCmdPrjUpTrigger asUpPrjname(String prjname) {
        this.asParams(Arrays.asList(prjname));
        return this;
    }

    @Override
    public void RT_onRecvedInStationLocal(StationLocal sl) throws Exception {
        String prjname = this.getParamByIdx(0);
        if (Convert.isNullOrEmpty(prjname))
            return;
        UAPrj locprj = UAManager.getInstance().getPrjByName(prjname);
        if (locprj == null)
            return;

        PSCmdPrjUpdate cmd = new PSCmdPrjUpdate();
        cmd.asToBePackPrj(locprj);
        StringBuilder failedr = new StringBuilder();
        if (!sl.RT_sendCmd(cmd, failedr)) {
            if (StationLocal.log.isDebugEnabled()) {
                StationLocal.log.debug(failedr.toString());
            }
        }
    }

}
