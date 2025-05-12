package cn.doraro.flexedge.core.router.roa;

import cn.doraro.flexedge.core.router.JoinIn;
import cn.doraro.flexedge.core.router.JoinOut;
import cn.doraro.flexedge.core.router.RouterManager;
import cn.doraro.flexedge.core.router.RouterOuterAdp;
import cn.doraro.flexedge.core.util.ILang;

import java.util.List;

public abstract class ROAJdbc extends RouterOuterAdp implements ILang {

    public ROAJdbc(RouterManager rm) {
        super(rm);
    }

    @Override
    public String getTp() {
        return "db_" + this.getDBTp();
    }

    public abstract String getDBTp();


//	@Override
//	public String getTpTitle()
//	{
//		return g("db_"+this.getDBTp());
//	}


    @Override
    public List<JoinIn> getJoinInList() {
        return null;
    }

    @Override
    public List<JoinOut> getJoinOutList() {
        return null;
    }


    @Override
    protected boolean RT_start_ov() {
        return false;
    }

    @Override
    public void RT_stop() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean RT_isRunning() {
        // TODO Auto-generated method stub
        return false;
    }
}
