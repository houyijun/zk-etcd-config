package io.github.houyijun.dynamic.config;


public interface IWatcher {
	/**
	 * watcher event happened
	 * @param key
	 * node path
	 * @param oldValue
	 * old value
	 * @param newValue
	 * new value
	 */
	void onChanged(String key,String oldValue,String newValue);
}
