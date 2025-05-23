package cn.doraro.flexedge.core.router;

import cn.doraro.flexedge.core.util.ILang;

import java.util.Arrays;
import java.util.List;

public class RICDef extends RouterInnCollator implements ILang {
    public static final String TP = "def";
    private List<JoinOut> jouts = Arrays.asList(
            new JoinOut(this, "def_tree"),//,false,true),
            new JoinOut(this, "def_flat")//,false,true)
    );


    public RICDef(RouterManager rm) {
        super(rm);
        //this.id = "def" ;
    }

    @Override
    public String getTp() {
        return TP;
    }

    protected RouterInnCollator newInstance(RouterManager rm) {
        return new RICDef(rm);
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
        List<JoinOut> jos = getConnectedJoinOuts();
        if (jos == null || jos.size() <= 0)
            return;

        for (JoinOut jo : jos) {
            try {
                String txt = readOutTxt(jo);
                if (txt == null)
                    continue;
                RouterObj ro = new RouterObj(txt);
                RT_sendToJoinOut(jo, ro);
            } catch (Exception e) {
                e.printStackTrace();
                //return null ;
            }
        }
    }


    private String readOutTxt(JoinOut jo) throws Exception {
        switch (jo.name) {
            case "def_tree":
                return this.belongTo.belongTo.JS_get_def_json();
            case "def_flat":
                return this.belongTo.belongTo.JS_get_def_json_flat();
            default:
                return null;
        }

    }

    @Override
    protected void RT_onRecvedFromJoinIn(JoinIn ji, RouterObj recved) {

    }
}
