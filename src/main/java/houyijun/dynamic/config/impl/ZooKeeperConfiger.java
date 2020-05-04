package houyijun.dynamic.config.impl;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import houyijun.dynamic.config.ConfigUtils;
import houyijun.dynamic.config.IConfiger;
import houyijun.dynamic.config.IWatcher;

public class ZooKeeperConfiger implements IConfiger {
	private static final Logger LOG = LoggerFactory.getLogger(ZooKeeperConfiger.class);

	/**
	 * Listener node path
	 */
	private String path = "";

	private String hosts = ""; // etc,"localhost:2181"

	Proxy proxy = new Proxy();

	RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
	CuratorFramework client = null;
	PathChildrenCache cache = null;

	Thread thread = null;

	public ZooKeeperConfiger(String hosts, String path) {
		this.hosts = hosts;
		this.path = path;
	}

	@Override
	public String get(String key) {
		return proxy.getKeyValue(key);
	}

	@Override
	public void start() {
		try {
			client = CuratorFrameworkFactory.builder().connectString(hosts).sessionTimeoutMs(5000)
					.connectionTimeoutMs(5000).retryPolicy(retryPolicy).build();
			client.start();
		} catch (Exception e) {
			LOG.error("build curator client exception:{}", e);
		}
		try {
			// load path data
			String initValue = query();
			LOG.info("#init query:{}", initValue);
			HashMap<String, String> map = ConfigUtils.decode(initValue);
			proxy.updateProps(map);

			// watch path changes
			// start watcher listener
			thread = new WatcherThread();
			thread.start();
		} catch (Exception e) {
			LOG.error("load path data error:{}", e);
		}
	}

	@Override
	public void close() {
		try {
			if (thread != null) {
				thread.join();
			}
		} catch (Exception e) {
			LOG.error("shutdown watcher thread:{}", e);
		}
		try {
			if (cache != null) {
				cache.close();
			}
			if (client != null) {
				client.close();
			}
		} catch (Exception e) {
			LOG.error("close curator client error:{}", e);
		}
	}

	@Override
	public void addWatcher(String key, IWatcher watcher) {
		// TODO 自动生成的方法存根
		proxy.addWatcher(key, watcher);
	}

	private String query() throws Exception {
		if (client != null) {
			String value = new String(client.getData().forPath(path));
			return value;
		} else {
			return null;
		}
	}

	private void startListen() throws Exception {
		if (client == null) {
			LOG.error("###can't water zookeeper:no curator cient");
			return;
		}
		CountDownLatch latch = new CountDownLatch(Integer.MAX_VALUE);

		NodeCache nodeCache = new NodeCache(client, path, false);
		NodeCacheListener nodeCacheListener = new NodeCacheListener() {
			@Override
			public void nodeChanged() throws Exception {
				String newValue = new String(nodeCache.getCurrentData().getData());
				LOG.info("node updated:{},newData:{}", nodeCache.getPath(), newValue);
				HashMap<String, String> newMap = ConfigUtils.decode(newValue);
				eventHappened(newMap);
			}
		};
		nodeCache.getListenable().addListener(nodeCacheListener);
		nodeCache.start();
		latch.await();

	}

	/**
	 * value changed events
	 * 
	 * @param newMap
	 */
	private void eventHappened(HashMap<String, String> newMap) {
		try {
			proxy.handler(newMap);
			proxy.updateProps(newMap);
		} catch (Exception e) {
			LOG.error("eventHappened:{}", e);
		}
	}

	public class WatcherThread extends Thread {
		// 重写run方法，run方法的方法体就是现场执行体
		public void run() {
			try {
				startListen();
			} catch (Exception e) {
				LOG.error("WatcherThread:{}", e);
			}
		}
	}

}
