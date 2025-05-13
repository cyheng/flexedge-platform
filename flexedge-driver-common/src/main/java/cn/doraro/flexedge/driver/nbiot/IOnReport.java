

package cn.doraro.flexedge.driver.nbiot;

import cn.doraro.flexedge.driver.nbiot.msg.WMMsg;
import cn.doraro.flexedge.driver.nbiot.msg.WMMsgReport;

import java.util.List;

public interface IOnReport {
    List<WMMsg> onMsgReport(final WMMsgReport p0);
}
