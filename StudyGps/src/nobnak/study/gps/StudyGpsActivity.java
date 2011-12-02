package nobnak.study.gps;

import java.util.List;

import nobnak.study.gps.GooglePlaceAPI.Place;
import nobnak.study.gps.GooglePlaceAPI.PlaceAPIException;
import nobnak.study.gps.LocationController.LocationChangedListener;
import nobnak.study.gps.LocationController.LocationTask;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class StudyGpsActivity extends Activity {
	public static final String LOG_TAG = "StudyGps";
	
	private TextView displayLocation;
	private ListView nameList;

	private LocationController locCtrl;

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final StudyGpsActivity me = this;
        LocationManager locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        this.displayLocation = (TextView) findViewById(R.id.displayLocation);
        this.nameList = (ListView) findViewById(R.id.nameListView);
        this.locCtrl = new LocationController(locManager);
        
        final GooglePlaceAPI placeApi = new GooglePlaceAPI(getString(R.string.placeApiUrl), this);
        final int radius = 1000;
        Button buttonRequestLocation = (Button) findViewById(R.id.buttonRequestLocation);
        buttonRequestLocation.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				final LocationTask task = locCtrl.request();
				final ProgressDialog dialog = ProgressDialog.show(me, 
						me.getText(R.string.title_under_reuqest_location), 
						me.getText(R.string.message_under_request_location), 
						false, true, new OnCancelListener() {
							public void onCancel(DialogInterface dialog) {
								task.close();
							}
						});
				task.addListener(new LocationChangedListener() {
					public void update(final Location location) {
						me.displayLocation(location);

						new AsyncTask<Void, Void, List<Place>>() {
							private PlaceAPIException error = null;
							
							@Override
							protected List<Place> doInBackground(Void... params) {
								try {
									return placeApi.retrievePlaces(location, radius);
								} catch (PlaceAPIException e) {
									error = e;
									return null;
								}
							}
							@Override
							protected void onCancelled() {
								close();
							}
							@Override
							protected void onPostExecute(List<Place> places) {
								if (error != null) {
									Toast.makeText(me, error.getMessage(), Toast.LENGTH_SHORT).show();
									close();
									return;
								}
								
								me.displayPlaces(places);
								close();
							}
							private void close() {
								dialog.dismiss();
							}
						}.execute();
					}
				});
			}
		});
    }
	public void displayLocation(Location location) {
		displayLocation.setText(String.format(
				"Lat:%3.3f, Lon:%3.3f", location.getLatitude(), location.getLongitude()));
	}
	public void displayPlaces(List<Place> places) {
		final ArrayAdapter<Place> adapter = new ArrayAdapter<Place>(this, android.R.layout.simple_list_item_1, places);
		nameList.setAdapter(adapter);
		nameList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Place place = adapter.getItem(position);
				Intent map = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(
						String.format("geo:%f,%f", place.getGeometry().lat, place.getGeometry().lon)));
				startActivity(map);
			}
		});
	}

}