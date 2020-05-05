package io.github.houyijun.dynamic.config.factory;

import io.github.houyijun.dynamic.config.IConfiger;
import io.github.houyijun.dynamic.config.impl.EtcdV3Configer;
import io.github.houyijun.dynamic.config.impl.ZooKeeperConfiger;

public class ConfigerFactory {
	private static final String ETCD_V3="etcd-v3";
	private static final String ZK="zookeeper";
	
	
	public static IConfiger createFactory(String kind,String hosts,String path)  throws IllegalArgumentException{
		if (kind==null) {
			throw new IllegalArgumentException ("null kind for ConfigFactory");
		}
		if (kind.equals(ETCD_V3)) {
			return new EtcdV3Configer(hosts,path);			
		}else if (kind.equals(ZK)) {
			return new ZooKeeperConfiger(hosts,path);			
		}
		throw new IllegalArgumentException ("invalid kind for ConfigFactory: "+kind);
	}
}
