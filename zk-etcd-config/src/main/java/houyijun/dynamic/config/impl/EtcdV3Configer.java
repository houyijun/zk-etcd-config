package houyijun.dynamic.config.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import houyijun.dynamic.config.ConfigUtils;
import houyijun.dynamic.config.IConfiger;
import houyijun.dynamic.config.IWatcher;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.Util;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.watch.WatchEvent;

public class EtcdV3Configer implements IConfiger {
	private static final Logger LOG = LoggerFactory.getLogger(IConfiger.class);

	Client client = null;
	KV kvClient = null;

	/**
	 * Listener node path
	 */
	private String path = "";

	Proxy proxy = new Proxy();

	private String hosts = ""; // etc,"localhost:2379"


	Thread thread=null;
	
	public EtcdV3Configer(String hosts, String path) {
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
			try {
				String[] list=hosts.split(",");
				List<String> hostList=new ArrayList<String>();
				for(int i=0;i<list.length;i++) {
					if (list[i].startsWith("http://")) {
						hostList.add(list[i]);
					}else {
						hostList.add("http://"+list[i]);
					}
				}						
				Collection<URI> uris = Util.toURIs(hostList);
				client = Client.builder().endpoints(uris).build();
				kvClient = client.getKVClient();
			} catch (Exception e) {
				LOG.error("init EtcdConfiger error:{}", e);
			}

			// refresh etcd's path data
			String initValue = query();
			LOG.info("#init query:{}",initValue);
			HashMap<String, String> map = ConfigUtils.decode(initValue);
			proxy.updateProps(map);

			// start watcher listener
			thread = new WatcherThread();
			thread.start();
		} catch (Exception e) {
			LOG.error("getValue exception:{},{}", path, e);
		}
	}

	@Override
	public void close() {
		try {
			if (thread!=null) {
		        thread.join(); 
			}
		} catch (Exception e) {
			LOG.error("shutdown watcher thread:{}", e);
		}
		// TODO 自动生成的方法存根
		if (kvClient != null) {
			kvClient.close();
		}
		if (client != null) {
			client.close();
		}

	}

	@Override
	public void addWatcher(String key, IWatcher watcher) {
		// TODO 自动生成的方法存根
		proxy.addWatcher(key, watcher);
	}

	private String query() throws Exception {
		ByteSequence key = ConfigUtils.String2Sequence(path);
		CompletableFuture<GetResponse> getFuture = kvClient.get(key);
		GetResponse response = getFuture.get();
		if (response.getCount() > 0) {
			return ConfigUtils.Sequence2String(response.getKvs().get(0).getValue());
		} else {
			return "";
		}
	}


	/**
	 * listener for etcd's path
	 * 
	 */
	private void startListen() {
		LOG.info("Watching for key={}", path);
		ByteSequence key = ConfigUtils.String2Sequence(path);
		CountDownLatch latch = new CountDownLatch(Integer.MAX_VALUE);

		Watch.Listener listener = Watch.listener(response -> {

			for (WatchEvent event : response.getEvents()) {
				LOG.error("####Etcd path changed:{},value:{}",
						ConfigUtils.Sequence2String(event.getKeyValue().getKey()),
						ConfigUtils.Sequence2String(event.getKeyValue().getValue()));
				if (event.getKeyValue().getKey().equals(key)) {
					String value = "";
					if (event.getKeyValue().getValue() != null) {
						value =ConfigUtils.Sequence2String( event.getKeyValue().getValue());
					}
					HashMap<String, String> newMap = ConfigUtils.decode(value);
					eventHappened( newMap);
				}
			}

//			latch.countDown();
		});

		try {
			Watch.Watcher watcher = client.getWatchClient().watch(key, listener);
			latch.await();
		} catch (Exception e) {
			LOG.error("watcher error:{}", e);
		}

	}

	/**
	 * value changed events
	 * 
	 * @param newMap
	 */
	private void eventHappened( HashMap<String, String> newMap) {
		proxy.handler( newMap);
		proxy.updateProps(newMap);
	}

	
	public class WatcherThread extends Thread {
	    //重写run方法，run方法的方法体就是现场执行体
	    public void run() {
	    	startListen();
	    }
	}
	
	
}
