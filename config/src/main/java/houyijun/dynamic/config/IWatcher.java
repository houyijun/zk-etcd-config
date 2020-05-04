package houyijun.dynamic.config;


public interface IWatcher {
	void onChanged(String key,String oldValue,String newValue);
}
