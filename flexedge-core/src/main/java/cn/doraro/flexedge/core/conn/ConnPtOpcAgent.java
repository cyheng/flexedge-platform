package cn.doraro.flexedge.core.conn;

import cn.doraro.flexedge.core.conn.masyn.MCmdAsyn;
import cn.doraro.flexedge.core.conn.masyn.MCmdAsynEndPoint;
import cn.doraro.flexedge.core.conn.masyn.MCmdAsynStateM;

import java.util.List;

public class ConnPtOpcAgent extends ConnPtTcpAccepted {
    private Thread recvTh = null;

//	@Override
//	public List<String> transBindIdToPath(String bindid)
//	{
//		return null;
//	}
//
//	@Override
//	public void writeBindBeSelectedTreeJson(Writer w,boolean list_tags_only) throws Exception
//	{
//		w.write("{\"id\":\""+UUID.randomUUID().toString()+"\"");
//    	w.write(",\"nc\":0");
//    	w.write(",\"icon\": \"fa fa-sitemap fa-lg\"");
//    	
//    	w.write(",\"text\":\""+this.getTitle()+"\"");
//    	w.write(",\"state\": {\"opened\": true}");
//		w.write("}");
//	}
    private MCmdAsynEndPoint cmdEP = null;
    private MCmdAsynStateM asynStatM = new MCmdAsynStateM() {

        @Override
        public void onMCmdAsynRecved(MCmdAsyn mca) {

        }

        @Override
        public void onMCmdAsynBroken() {

        }

        @Override
        public boolean checkStateMOk() {

            return false;
        }

        @Override
        public StateRes onPulseStateMachine() {

            return null;
        }

    };

    @Override
    public String getConnType() {
        return "opc_agent";
    }

    @Override
    public String getStaticTxt() {
        return null;
    }

    public List<String> listOpcProgIds() {
        return null;
    }

    /**
     * override it will do something
     * when conn ready,it will start recv msg thread
     *
     * @param r
     */
    protected void onConnReadyOrNot(boolean r) {
        if (r) {//
            startRecv();
        } else {
            stopRecv();
        }
    }

    private synchronized void startRecv() {
        stopRecv();
        //
        cmdEP = new MCmdAsynEndPoint(this.getId(), this, asynStatM);
        cmdEP.setConnPtStream(this);
        cmdEP.start();
    }

    private synchronized void stopRecv() {
        if (cmdEP == null) {
            return;
        }

        cmdEP.dispose();
        cmdEP = null;
    }


}
