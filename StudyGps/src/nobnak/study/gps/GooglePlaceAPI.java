package nobnak.study.gps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.net.http.AndroidHttpClient;
import android.util.Pair;

public class GooglePlaceAPI {
	public static final String JSON = "json";
	public static final String USER_AGENT = "Android";

	private String placeApiUrl;
	private AndroidHttpClient client;
	
	public GooglePlaceAPI(String placeApiUrl, Context context) {
		this.placeApiUrl = placeApiUrl;
		client = AndroidHttpClient.newInstance(USER_AGENT, context);
	}
	
	public List<Place> retrievePlaces(Location location, int radius) throws PlaceAPIException {
		List<Place> names = new LinkedList<Place>();

		List<Pair<String, String>> params = new LinkedList<Pair<String,String>>();
		params.add(new Pair<String, String>("location", String.format("%3.7f,%3.7f", location.getLatitude(), location.getLongitude())));
		params.add(new Pair<String, String>("radius", String.format("%d", radius)));
		params.add(new Pair<String, String>("sensor", "true"));
		params.add(new Pair<String, String>("types", "cafe"));
		params.add(new Pair<String, String>("key", "AIzaSyCnMlijeElxAv5_EbKziIvVZQoOZ39Q4P8"));
		String url = String.format(placeApiUrl, JSON, UrlParamHelper.constractQuery(params));
		HttpGet request = new HttpGet(url);
		try {
			HttpResponse response = client.execute(request);
			BufferedReader contents = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			try {
				StringBuffer bufJson = new StringBuffer();
				for (CharBuffer bufChar = CharBuffer.allocate(1024); contents.read(bufChar) > 0; ) {
					bufJson.append(bufChar.flip());
					bufChar.clear();
				}
				JSONObject json = new JSONObject(bufJson.toString());
				JSONArray results = json.getJSONArray("results");
				for (int i = 0; results.length() > i; i++) {
					final JSONObject result = results.getJSONObject(i);
					String name = result.getString(PlaceSearchResponse.KEY_NAME);
					JSONObject geoLocation = result.getJSONObject(PlaceSearchResponse.KEY_GEOMETRY)
							.getJSONObject(PlaceSearchResponse.Geometry.KEY_LOCATION);
					float lat = (float) geoLocation.getDouble(PlaceSearchResponse.Geometry.Location.KEY_LAT);
					float lon = (float) geoLocation.getDouble(PlaceSearchResponse.Geometry.Location.KEY_LON);
					names.add(new Place(name, lat, lon));
				}
			} finally {
				contents.close();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new PlaceAPIException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new PlaceAPIException(e);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new PlaceAPIException(e);
		}
		return names;
	}
	
	// Interfaces
	public class Place {
		private String name;
		private Geometry geometry;
		
		public Place(String name, float lon, float lat) {
			this.name = name;
			this.geometry = new Geometry(lon, lat);
		}
		
		// Methods
		public String getName() {
			return name;
		}
		public Geometry getGeometry() {
			return geometry;
		}

		@Override
		public String toString() {
			return name;
		}
		
		// Interfaces
		public class Geometry {
			public final float lat;
			public final float lon;
			
			public Geometry(float lat, float lon) {
				this.lat = lat;
				this.lon = lon;
			}
		}
	}
	public interface PlaceSearchResponse {
		public static final String KEY_NAME = "name";
		public static final String KEY_GEOMETRY = "geometry";
		
		public interface Geometry {
			public static final String KEY_LOCATION = "location";
			
			public interface Location {
				public static final String KEY_LAT = "lat";
				public static final String KEY_LON = "lng";
			}
		}
	}
	public class PlaceAPIException extends Exception {
		public PlaceAPIException() {
			super();
		}
		public PlaceAPIException(String detailMessage, Throwable throwable) {
			super(detailMessage, throwable);
		}
		public PlaceAPIException(String detailMessage) {
			super(detailMessage);
		}
		public PlaceAPIException(Throwable throwable) {
			super(throwable);
		}
	}
}
