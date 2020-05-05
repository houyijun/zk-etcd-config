package io.github.houyijun.config.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.houyijun.dynamic.config.IConfiger;
import io.github.houyijun.dynamic.config.IWatcher;
import io.github.houyijun.dynamic.config.factory.ConfigerFactory;

public class ZookeeperConfigTest {
	private static final Logger LOG = LoggerFactory.getLogger(ZookeeperConfigTest.class);

	public static void main(String[] args) throws Exception {
		testData();
	}

	/**
	 * s * @throws Exception
	 */
	private static void testData() throws Exception {
		LOG.error("### Test zookeeper  #####");

		IConfiger config = ConfigerFactory.createFactory("zookeeper", "localhost:2181", "/config");
		config.start();
		String value = config.get("hello");
		LOG.error("test###key={},value={}", "hello", value);
		IWatcher w = new TestWatcher();
		config.addWatcher("hello", w);
		LOG.error("###query key:hello,value={}", config.get("hello"));
		LOG.error("###ending.....");
		config.close();
	}
}
