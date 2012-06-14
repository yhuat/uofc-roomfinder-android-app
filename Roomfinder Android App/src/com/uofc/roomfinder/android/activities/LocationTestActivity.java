package com.uofc.roomfinder.android.activities;

import java.util.List;

import com.uofc.roomfinder.R;
import com.uofc.roomfinder.android.RoomFinderApplication;

import android.app.Activity;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class LocationTestActivity extends Activity implements LocationListener {

	private static final String TAG = "LocationDemo";
	private static final String[] S = { "Out of Service", "Temporarily Unavailable", "Available" };

	private LocationManager locationManager;
	private String bestProvider;
	private TextView output;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("test activity created");
		setContentView(R.layout.location_layout);

		// Get the output UI
		output = (TextView) findViewById(R.id.output);

		// Get the location manager
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		// List all providers:
		List<String> providers = locationManager.getAllProviders();
		for (String provider : providers) {
			printProvider(provider);
		}

		Criteria criteria = new Criteria();
		bestProvider = locationManager.getBestProvider(criteria, false);
		output.append("\n\nBEST Provider:\n");
		printProvider(bestProvider);

		output.append("\n\nLocations (starting with last known):");
		locationManager.requestLocationUpdates(bestProvider, 0, 1 , lnormal);
		
	}

	/** Register for the updates when Activity is in foreground */
	@Override
	protected void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(bestProvider, 20000, 1, this);
	}

	/** Stop the updates when Activity is paused */
	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		printLocation(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
		// let okProvider be bestProvider
		// re-register for updates
		output.append("\n\nProvider Disabled: " + provider);
	}

	@Override
	public void onProviderEnabled(String provider) {
		// is provider better than bestProvider?
		// is yes, bestProvider = provider
		output.append("\n\nProvider Enabled: " + provider);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		output.append("\n\nProvider Status Changed: " + provider + ", Status=" + S[status] + ", Extras=" + extras);
	}

	private void printProvider(String provider) {
		LocationProvider info = locationManager.getProvider(provider);
		output.append(info.toString() + " - " + info.getName() + "\n\n");
	}

	private void printLocation(Location location) {
		if (location == null)
			output.append("\nLocation[unknown]\n\n");
		else
			output.append("\n\n" + location.toString());
	}
	
	
	private LocationListener lnormal = new LocationListener() {
		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		@Override
		public void onLocationChanged(Location location) {
			Log.d(TAG, "normal Location Changed: " + location.getProvider() + " lat: " + location.getLatitude() + " lon: " + location.getLongitude() + " alt: "
					+ location.getAltitude() + " acc: " + location.getAccuracy());
			Toast.makeText(RoomFinderApplication.getAppContext(), "NORMAL: Location Changed: "+location.getProvider()+" lat: "+location.getLatitude()+" lon: "+location.getLongitude()+" alt: "+location.getAltitude()+" acc: "+location.getAccuracy(), Toast.LENGTH_LONG).show();
			try {

				Log.v(TAG, "Location Changed: " + location.getProvider() + " lat: " + location.getLatitude() + " lon: " + location.getLongitude() + " alt: "
						+ location.getAltitude() + " acc: " + location.getAccuracy());
				
				// mixView.repaint();
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	};
}
