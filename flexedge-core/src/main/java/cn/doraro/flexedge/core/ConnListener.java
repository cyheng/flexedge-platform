package cn.doraro.flexedge.core;

public interface ConnListener
{
	public void onDevConnCreated(ConnPt dc) ;
	
	public void onConnPtConnected(ConnPt cp) ;
	
	public void onConnPtClosed(ConnPt cp) ;
}
