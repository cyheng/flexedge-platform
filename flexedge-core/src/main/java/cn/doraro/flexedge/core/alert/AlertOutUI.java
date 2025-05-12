package cn.doraro.flexedge.core.alert;

import cn.doraro.flexedge.core.util.xmldata.data_class;

@data_class
public class AlertOutUI extends AlertOut {
    public static final String TP = "ui";

    @Override
    public String getOutTp() {
        return TP;
    }

    @Override
    public String getOutTpTitle() {
        return "UI";
    }

    public void sendAlert(String uid, AlertItem ai) {

    }
}
