package cn.doraro.flexedge.core;

import org.json.*;

import java.util.List;

public interface IOCList extends IOCBox
{
	/**
	 * export type ListHead = {n:string,t:string}[]
	 * @return
	 */
	public JSONArray OCList_getListHead() ;
	
	public List<Object> OCList_getItems();
}
