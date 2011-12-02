package nobnak.study.gps;

import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;

import android.util.Pair;

public class UrlParamHelper {
	public static String constractQuery(List<Pair<String, String>> params) {
		StringBuffer buffer = new StringBuffer();
		for (Iterator<Pair<String, String>> i = params.iterator(); i.hasNext(); ) { 
			Pair<String, String> p = i.next();
			buffer.append(URLEncoder.encode(p.first));
			buffer.append("=");
			buffer.append(URLEncoder.encode(p.second));
			if (i.hasNext())
				buffer.append("&");
		}
		return buffer.toString();
	}
}
