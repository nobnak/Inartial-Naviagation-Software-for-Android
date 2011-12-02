package nobnak.study.gps;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class LocationController {
	public static final String LOG_TAG = "LocationController";
	
	private LocationManager locManager;
	
	public LocationController(LocationManager locManager) {
		this.locManager = locManager;
	}
	
	// Methods
	public LocationTask request() {
		LocationTask result = new LocationTask();
		result.request(LocationManager.GPS_PROVIDER, 0, 0);
		return result;
	}
	
	// Interfaces
	public interface LocationChangedListener {
		void update(Location location);
	}
	public class LocationTask {
		private List<LocationChangedListener> listeners;
		private LocationListener eventHandler;
		
		public LocationTask() {
			this.listeners = new LinkedList<LocationController.LocationChangedListener>();
		}

		public void close() {
			if (eventHandler != null) {
				locManager.removeUpdates(eventHandler);
				eventHandler = null;
			}
		}
		public void request(String provider, long minTime, float minDistance ) {
			if (eventHandler != null)
				throw new IllegalStateException("Event is under process");
			
			final LocationTask me = this;
			eventHandler = new LocationListener() {
				public void onStatusChanged(String provider, int status, Bundle extras) {
				}
				
				public void onProviderEnabled(String provider) {
					Log.d(LOG_TAG, "Location Enabled");
				}
				
				public void onProviderDisabled(String provider) {
				}				
				public void onLocationChanged(Location location) {
					me.close();
					updateLocation(location);
				}
    		};
	        locManager.requestLocationUpdates(provider, minTime, minDistance, eventHandler);
		}
		public void addListener(LocationChangedListener newOne) {
			listeners.add(newOne);
		}
		public void updateLocation(Location location) {
			for (Iterator<LocationChangedListener> iter = listeners.iterator(); iter.hasNext(); )
				iter.next().update(location);
		}
		@Override
		protected void finalize() throws Throwable {
			super.finalize();
			close();
		}
		
	}
}
