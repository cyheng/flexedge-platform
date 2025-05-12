// 
// Decompiled by Procyon v0.6.0
// 

package cn.doraro.flexedge.driver.common;

import cn.doraro.flexedge.core.DevAddr;
import cn.doraro.flexedge.core.UADev;
import cn.doraro.flexedge.core.UAVal;
import cn.doraro.flexedge.core.util.Convert;

import java.util.List;

public class MQTTClientAddr extends DevAddr {
    String topic;
    List<String> jsonPath;

    MQTTClientAddr() {
        this.topic = null;
        this.jsonPath = null;
    }

    MQTTClientAddr(final String addr, final UAVal.ValTP vtp, final String topic, final String jsonpath) {
        super(addr, vtp);
        this.topic = null;
        this.jsonPath = null;
        this.topic = topic;
        this.jsonPath = Convert.splitStrWith(jsonpath, "/\\");
    }

    public DevAddr parseAddr(final UADev dev, final String str, final UAVal.ValTP vtp, final StringBuilder failedr) {
        final int idx = str.indexOf(43);
        if (idx <= 0) {
            failedr.append("invalid addr=" + str + ",it must like xxx/xx/xx+temp");
            return null;
        }
        final String topic = str.substring(0, idx);
        final String jsonpath = str.substring(idx + 1);
        if (Convert.isNullOrEmpty(jsonpath)) {
            failedr.append("invalid addr=" + str + ",it must like xxx/xx/xx+temp");
            return null;
        }
        final MQTTClientAddr ret = new MQTTClientAddr(str, vtp, topic, jsonpath);
        return ret;
    }

    public String getMQTTTopic() {
        return this.topic;
    }

    public List<String> getPayloadJSONPath() {
        return this.jsonPath;
    }

    public boolean isSupportGuessAddr() {
        return false;
    }

    public DevAddr guessAddr(final UADev dev, final String str, final UAVal.ValTP vtp) {
        return null;
    }

    public List<String> listAddrHelpers() {
        return null;
    }

    public UAVal.ValTP[] getSupportValTPs() {
        return null;
    }

    public String toCheckAdjStr() {
        return null;
    }

    public boolean canRead() {
        return true;
    }

    public boolean canWrite() {
        return false;
    }
}
