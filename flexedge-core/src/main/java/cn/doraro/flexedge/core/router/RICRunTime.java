package cn.doraro.flexedge.core.router;

import cn.doraro.flexedge.core.util.ILang;

import java.util.Arrays;
import java.util.List;

public class RICRunTime extends RouterInnCollator implements ILang {
    public static final String TP = "rt";
    private List<JoinOut> jouts = Arrays.asList(
            new JoinOut(this, "rt_tree"),//,false,true),
            new JoinOut(this, "rt_flat") //,false,true)
    );

    public RICRunTime(RouterManager rm) {
        super(rm);
        //this.id = "rt" ;
    }

    @Override
    public String getTp() {
        return TP;
    }

    protected RouterInnCollator newInstance(RouterManager rm) {
        return new RICRunTime(rm);
    }

    public OutStyle getOutStyle() {
        return OutStyle.interval;
    }

    @Override
    public List<JoinIn> getJoinInList() {
        return null;
    }

    @Override
    public List<JoinOut> getJoinOutList() {
        return jouts;
    }

    /**
     * override by sub
     */
    @Override
    protected void RT_runInIntvLoop() {

    }


    public String pullOut(String join_out_name) throws Exception {
        switch (join_out_name) {
            case "rt_tree":
                return this.belongTo.belongTo.JS_get_rt_json();
            case "rt_flat":
                return this.belongTo.belongTo.JS_get_rt_json_flat();
            default:
                return null;
        }
    }

    @Override
    protected void RT_onRecvedFromJoinIn(JoinIn ji, RouterObj recved) {

    }
}
