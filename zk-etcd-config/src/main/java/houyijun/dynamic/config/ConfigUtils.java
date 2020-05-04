package houyijun.dynamic.config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import io.etcd.jetcd.ByteSequence;

public class ConfigUtils {

	public static HashMap<String, String> decode(String value) {
		HashMap map = new HashMap<String, String>();
		try {
			Properties p = new Properties();
			InputStream in = new ByteArrayInputStream(value.getBytes());
			p.load(in);
			Set propertySet = p.entrySet();
			for (Object o : propertySet) {
				Map.Entry entry = (Map.Entry) o;
				map.put(entry.getKey(), entry.getValue());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return map;
	}

	public static ByteSequence String2Sequence(String input) {
		try {
			ByteSequence target = ByteSequence.from(new String(input).getBytes("utf-8"));
			return target;
		} catch (Exception e) {
			return null;
		}
	}

	public static String Sequence2String(ByteSequence input) {
		try {
			String target = input.toString(Charset.forName("utf-8"));
			return target;
		} catch (Exception e) {
			return null;
		}
	}
}
