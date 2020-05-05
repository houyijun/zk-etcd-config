package io.github.houyijun.dynamic.config.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import io.github.houyijun.dynamic.config.IWatcher;

public class Proxy {
	
	private ConcurrentHashMap<String,List<IWatcher>> map=new ConcurrentHashMap<String,List<IWatcher>>();
	
	private HashMap<String,String> props = new HashMap<String,String>();
	
	private static ReentrantLock lock = new ReentrantLock();
	
	/**
	 *  get key value from props
	 * @param key
	 * @return
	 */
	public String getKeyValue(String key){
		String retVal ="";
		lock.lock();
		if (props.containsKey(key)) {
			retVal=props.get(key);
		}
		lock.unlock();
		return retVal;
	}
	
	/**
	 *  update props values from newMap
	 * @param newMap
	 */
	public void updateProps(HashMap<String, String> newMap) {
		lock.lock();
		Iterator<String> keys = newMap.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			String newValue = newMap.get(key);
			if (!(props.containsKey(key) && props.get(key).equals(newValue))) {
				props.put(key, newValue);
			}
		}
		//delete useless keyValues from props
		for (Iterator<Map.Entry<String, String>> it = props.entrySet().iterator(); it.hasNext();){
		    Map.Entry<String, String> item = it.next();
		    if (!newMap.containsKey(item.getKey())) {
		    	 it.remove();
		    }		   
		}
		lock.unlock();
	}
	
	
	public void addWatcher(String key,IWatcher watcher) {
		if (!map.containsKey(key)) {
			map.put(key,new ArrayList<IWatcher>());
		}
		
		map.get(key).add(watcher);
	}
	
	
	
	/**
	 * event watcher  handler
	 * @param newMap
	 */
	public void handler( HashMap<String, String> newMap) {
		HashMap<String,String> oldMap=props;
		
		Iterator<String> keys = newMap.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			String newValue = newMap.get(key);
			if (map.containsKey(key)) {
				if (!oldMap.containsKey(key)) {
					map.get(key).forEach(w->w.onChanged(key, "", newValue));						
				}else if (!oldMap.get(key).equals(newValue)){
					map.get(key).forEach(w->w.onChanged(key,oldMap.get(key), newValue));
				}
			}
			
		}
		
		// only has old values
		Iterator<String> oldKeys = oldMap.keySet().iterator();
		while (oldKeys.hasNext()) {
			String key = oldKeys.next();
			String oldValue = oldMap.get(key);
			if (map.containsKey(key) && !newMap.containsKey(key)) {
					map.get(key).forEach(w->w.onChanged(key, oldValue, ""));						
			}
		}
	}

}
