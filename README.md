# 欢迎使用 zk-etcd-config

**zk-etcd-config是一款轻量级的动态属性配置接口，同时支持etcd v3和zookeeper两种中间件**

[github地址](https://github.com/houyijun/zk-etcd-config)

用zk-etcd-config可以灵活切换使用etcd或者zookeeper作为动态属性配置中间件，用简便的接口来监听属性值的动态变化。

## 示例代码
(1)使用：
假设我们的系统有两个参数需要动态配置，当该系统的微服务集群启动后，需要动态捕捉到这两个参数值的变化，如果我们使用zookeeper作为中间件存储，讲所有的参数组装成一个类似properties.application的文件内容，格式如下：
```
param1=value1
param2=value2
```
那么我们可以将上面的配置内容写到zookeeper的/config节点的data中。当/config中param1或者param2的值变化时，通过IWatcher接口实时接收变动通知。
示例代码

```
String kind="zookeeper";
String hosts="localhost:2181,localhost:2183";
String path="/config";

IConfiger config = ConfigerFactory.createFactory(kind, hosts, path);
config.start();
String value = config.get("param1");
LOG.error("###key={},value={}", "param1", value);
IWatcher w = new TestWatcher();
config.addWatcher("param1", w);
```
TestWatcher是自定义的类，实现IWatcher接口：
```
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import houyijun.dynamic.config.IWatcher;
public class TestWatcher implements IWatcher{
	private static final Logger LOG = LoggerFactory.getLogger(TestWatcher.class);	

	public void onChanged(String key, String oldValue, String newValue) {
		LOG.error("###changed,old={},new={}",oldValue,newValue);
	}
}

```

kind表示用哪种中间件，目前支持的值为：
"etcd-v3" 表示用etcd的V3版本接口。
"zookeeper"表示用zookeeper的接口。
hosts表示etcd或zookeeper的地址，用","隔开，ip:port格式。
path表示动态属性在etcd或zookeeper上存储的节点路径。
IWatcher是监听事件接口，其定义为：
```
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
```

(2)最后退出时关闭config：
```
config.close();
```

## maven pom依赖包
依赖包除了zk-etcd-config之外，还必须加上etcd和zookeeper的依赖包。
```
 	<dependency>
		<groupId>houyijun.dynamic</groupId>
		<artifactId>zk-etcd-config</artifactId>
		<version>0.1.0</version>
	</dependency>
	<!-- etcd V3  -->
	<dependency>
		<groupId>io.etcd</groupId>
		<artifactId>jetcd-core</artifactId>
		<version>0.4.1</version>
	</dependency>
	<!-- zookeeper client -->
	<dependency>
		<groupId>org.apache.curator</groupId>
		<artifactId>curator-recipes</artifactId>
		<version>4.0.1</version>
	</dependency>
	<dependency>
		<groupId>org.apache.zookeeper</groupId>
		<artifactId>zookeeper</artifactId>
		<version>3.4.8</version>
	</dependency>
```
		


