package config.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import houyijun.dynamic.config.IWatcher;

public class TestWatcher implements IWatcher{
	private static final Logger LOG = LoggerFactory.getLogger(TestWatcher.class);
	

	@Override
	public void onChanged(String key, String oldValue, String newValue) {
		// TODO 自动生成的方法存根
		LOG.error("###changed,old={},new={}",oldValue,newValue);
	}

}
