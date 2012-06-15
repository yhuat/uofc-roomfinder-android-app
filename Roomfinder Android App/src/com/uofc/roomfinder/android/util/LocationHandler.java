package com.uofc.roomfinder.android.util;

import java.util.List;

import com.uofc.roomfinder.android.DataModel;
import com.uofc.roomfinder.android.RoomFinderApplication;
import com.uofc.roomfinder.android.util.threads.WifiLocationProvider;
import com.uofc.roomfinder.entities.Point3D;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/**
 * this class handles everything that has to do with location management
 * 
 * @author benjaminlautenschlaeger
 * 
 */
public class LocationHandler {

	private static final String TAG = "location handler";

	private LocationManager lm;
	private Location curLoc;
	private Location locationAtLastDownload;

	public LocationHandler() {
		System.out.println("init location handler");
		lm = (LocationManager) RoomFinderApplication.getAppContext().getSystemService(Context.LOCATION_SERVICE);

		// List all providers:
		List<String> providers = lm.getAllProviders();
		for (String provider : providers) {
			System.out.println("provider: " + lm.getProvider(provider).getName());
		}

		Criteria criteria = new Criteria();

		// try to use the coarse provider first to get a rough position
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		String coarseProvider = lm.getBestProvider(criteria, true);
		try {
			lm.requestLocationUpdates(coarseProvider, 0, 0, coarseLocationListener);
		} catch (Exception e) {
			Log.d(TAG, "Could not initialize the coarse provider");
		}

		// try to set up fine fine provider
		criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		String bestProvider = lm.getBestProvider(criteria, true);
		lm.requestLocationUpdates(bestProvider, 0, 1, fineLocationListener);
		System.out.println("best provider: " + bestProvider);

		// fallback (without GPS)
		Location hardFix = new Location("reverseGeocoded");

		// center of ICT building, ground floor
		// 51.080259, -114.130308
		hardFix.setLatitude(51.080259);
		hardFix.setLongitude(-114.130308);
		hardFix.setAltitude(4.0);

		// set the current loc to the best estimate
		try {
			Location lastFinePos = lm.getLastKnownLocation(bestProvider);
			Location lastCoarsePos = lm.getLastKnownLocation(coarseProvider);
			if (lastFinePos != null)
				curLoc = lastFinePos;
			else if (lastCoarsePos != null)
				curLoc = lastCoarsePos;
			else
				curLoc = hardFix;
		} catch (Exception ex2) {
			// ex2.printStackTrace();
			curLoc = hardFix;
			// Toast.makeText(RoomFinderApplication.getAppContext(), RoomFinderApplication.getAppContext().getString(DataView.CONNECTION_GPS_DIALOG_TEXT),
			// Toast.LENGTH_LONG).show();
		}
		curLoc = hardFix;

	}

	public Location getLocationAtLastDownload() {
		return locationAtLastDownload;
	}

	public void setLocationAtLastDownload(Location locationAtLastDownload) {
		this.locationAtLastDownload = locationAtLastDownload;
	}

	public Location getCurrentLocation() {
		if (curLoc == null)
			return null;

		/*
		 * Location hardFix = new Location("reverseGeocoded"); hardFix.setLatitude(51.080259); hardFix.setLongitude(-114.130308); hardFix.setAltitude(4.0);
		 * return hardFix;
		 */

		synchronized (curLoc) {
			return curLoc;
		}

	}

	public void unregisterLocationManager() {
		if (lm != null) {
			lm.removeUpdates(coarseLocationListener);
			lm.removeUpdates(fineLocationListener);
			lm = null;
		}
	}

	public LocationListener getLocationListener() {
		return fineLocationListener;
	}

	/**
	 * enables a thread to invoke wifi scanning each couple of seconds
	 */
	private void startWifiLocationScanner() {
		// delete, should be in location bla
		Thread backgroundThread = new Thread(new WifiLocationProvider());
		backgroundThread.start();
	}

	// coarse location listener is only for the first fix
	// after first fix, coarse listener is removed from the manager
	private LocationListener coarseLocationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			try {
				Log.d(TAG, "coarse Location Changed: " + location.getProvider() + " lat: " + location.getLatitude() + " lon: " + location.getLongitude()
						+ " alt: " + location.getAltitude() + " acc: " + location.getAccuracy());
				lm.removeUpdates(coarseLocationListener);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		@Override
		public void onProviderDisabled(String arg0) {
		}

		@Override
		public void onProviderEnabled(String arg0) {
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		}

	};

	// fine location listener uses gps or network service to determine position
	private LocationListener fineLocationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			String status = "normal Location Changed: " + location.getProvider() + " lat: " + location.getLatitude() + " lon: " + location.getLongitude()
					+ " alt: " + location.getAltitude() + " acc: " + location.getAccuracy();

			Log.d(TAG, status);

			DataModel.getInstance().setGpsPosition(new Point3D(location.getLongitude(), location.getLatitude(), location.getAltitude()));
			DataModel.getInstance().setGpsAccuracy(location.getAccuracy());
			
			// if accuracy is lower than X -> start wifi thread! (add wifi logo)
			Thread backgroundThread = new Thread(new WifiLocationProvider());
			backgroundThread.start();

			synchronized (curLoc) {
				curLoc = location;
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

	};

}
