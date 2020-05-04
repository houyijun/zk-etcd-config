package config.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import houyijun.dynamic.config.IConfiger;
import houyijun.dynamic.config.IWatcher;
import houyijun.dynamic.config.factory.ConfigerFactory;


public class EtcdConfigTest {
	private static final Logger LOG = LoggerFactory.getLogger(EtcdConfigTest.class);
	
	
	public static void main(String[] args) throws  Exception {
		testData();
   }
	
	/**
	 * 设置etcd V3：
	 * 环境变量ETCDCTL_API=3进行设置
	 * @throws Exception
	 */
	private static void testData() throws Exception  {
		LOG.error("### Test Etcd  #####");
		
		IConfiger config = ConfigerFactory.createFactory("etcd-v3", "localhost:2379", "/config");
		config.start();
		String value = config.get("hello");
		LOG.error("test###key={},value={}","hello",value);
		IWatcher w=new TestWatcher();
		config.addWatcher("hello", w);
		LOG.error("###ending.....");
		config.close();
	}

}
