package cn.doraro.flexedge.core.store;

import kotlin.NotImplementedError;
import org.json.JSONObject;

public class SourceIoTDB extends Source {

    @Override
    public String getSorTp() {
        return "iotdb";
    }

    @Override
    public String getSorTpTitle() {
        return "IoTDB";
    }

    public boolean checkValid(StringBuilder failedr) {
        return true;
    }

    public boolean checkConn(StringBuilder failedr) {
        throw new NotImplementedError();
    }

    @Override
    public String getExchgTP() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected JSONObject toExchgPmJO() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected boolean fromExchgPmJO(JSONObject pmjo) {
        // TODO Auto-generated method stub
        return false;
    }
}
