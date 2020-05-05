package io.github.houyijun.dynamic.config;


public interface IConfiger {
	
	void start();
	
	String get(String key);
	 
	 void close();
	 
	 void addWatcher(String key,IWatcher watcher);
}
