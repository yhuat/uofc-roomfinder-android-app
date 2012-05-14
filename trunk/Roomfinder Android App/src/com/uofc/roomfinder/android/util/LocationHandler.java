package com.uofc.roomfinder.android.util;

import org.mixare.DataView;

import com.uofc.roomfinder.android.DataModel;
import com.uofc.roomfinder.android.RoomFinderApplication;

import android.app.Application;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


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

	
	public LocationHandler(){
		lm = (LocationManager) RoomFinderApplication.getAppContext().getSystemService(Context.LOCATION_SERVICE);
		
		
		Criteria c = new Criteria();
		//try to use the coarse provider first to get a rough position
		c.setAccuracy(Criteria.ACCURACY_COARSE);
		String coarseProvider = lm.getBestProvider(c, true);
		try {
			lm.requestLocationUpdates(coarseProvider, 0 , 0, lcoarse);
		} catch (Exception e) {
			Log.d(TAG, "Could not initialize the coarse provider");
		}
		
		//need to be precise
		c.setAccuracy(Criteria.ACCURACY_FINE);				
		//fineProvider will be used for the initial phase (requesting fast updates)
		//as well as during normal program usage
		//NB: using "true" as second parameters means we get the provider only if it's enabled
		String fineProvider = lm.getBestProvider(c, true);
		try {
			lm.requestLocationUpdates(fineProvider, 0 , 0, lbounce);
		} catch (Exception e) {
			Log.d(TAG, "Could not initialize the bounce provider");
		}
		
		//fallback for the case where GPS and network providers are disabled
		Location hardFix = new Location("reverseGeocoded");

		//Frangart, Eppan, Bozen, Italy
		hardFix.setLatitude(46.480302);
		hardFix.setLongitude(11.296005);
		hardFix.setAltitude(300);

		/*New York*/
//		hardFix.setLatitude(40.731510);
//		hardFix.setLongitude(-73.991547);
		
		// TU Wien
//		hardFix.setLatitude(48.196349);
//		hardFix.setLongitude(16.368653);
//		hardFix.setAltitude(180);

		//frequency and minimum distance for update
		//this values will only be used after there's a good GPS fix
		//see back-off pattern discussion 
		//http://stackoverflow.com/questions/3433875/how-to-force-gps-provider-to-get-speed-in-android
		//thanks Reto Meier for his presentation at gddde 2010
		long lFreq = 60000;	//60 seconds
		float lDist = 50;		//20 meters
		try {
			lm.requestLocationUpdates(fineProvider, lFreq , lDist, lnormal);
		} catch (Exception e) {
			Log.d(TAG, "Could not initialize the normal provider");
			Toast.makeText(  RoomFinderApplication.getAppContext(), RoomFinderApplication.getAppContext().getString(DataView.CONNECTION_GPS_DIALOG_TEXT), Toast.LENGTH_LONG ).show();
		}
		
		try {
			Location lastFinePos=lm.getLastKnownLocation(fineProvider);
			Location lastCoarsePos=lm.getLastKnownLocation(coarseProvider);
			if(lastFinePos!=null)
				curLoc = lastFinePos;
			else if (lastCoarsePos!=null)
				curLoc = lastCoarsePos;
			else
				curLoc = hardFix;
			
		} catch (Exception ex2) {
			//ex2.printStackTrace();
			curLoc = hardFix;
			Toast.makeText( RoomFinderApplication.getAppContext(), RoomFinderApplication.getAppContext().getString(DataView.CONNECTION_GPS_DIALOG_TEXT), Toast.LENGTH_LONG ).show();
		}
		
		setLocationAtLastDownload(curLoc);
		
		
	}
	
	public Location getLocationAtLastDownload() {
		return locationAtLastDownload;
	}

	public void setLocationAtLastDownload(Location locationAtLastDownload) {
		this.locationAtLastDownload = locationAtLastDownload;
	}
	
	
	private LocationListener lbounce = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			Log.d(TAG, "bounce Location Changed: "+location.getProvider()+" lat: "+location.getLatitude()+" lon: "+location.getLongitude()+" alt: "+location.getAltitude()+" acc: "+location.getAccuracy());
			//Toast.makeText(ctx, "BOUNCE: Location Changed: "+location.getProvider()+" lat: "+location.getLatitude()+" lon: "+location.getLongitude()+" alt: "+location.getAltitude()+" acc: "+location.getAccuracy(), Toast.LENGTH_LONG).show();

			
			if (location.getAccuracy() < 40) {
				lm.removeUpdates(lcoarse);
				lm.removeUpdates(lbounce);			
			}
		}

		@Override
		public void onProviderDisabled(String arg0) {
			Log.d(TAG, "bounce disabled");
		}

		@Override
		public void onProviderEnabled(String arg0) {
			Log.d(TAG, "bounce enabled");

		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
		
	};
	
	private LocationListener lcoarse = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			try {
				Log.d(TAG, "coarse Location Changed: "+location.getProvider()+" lat: "+location.getLatitude()+" lon: "+location.getLongitude()+" alt: "+location.getAltitude()+" acc: "+location.getAccuracy());
				//Toast.makeText(ctx, "COARSE: Location Changed: "+location.getProvider()+" lat: "+location.getLatitude()+" lon: "+location.getLongitude()+" alt: "+location.getAltitude()+" acc: "+location.getAccuracy(), Toast.LENGTH_LONG).show();
				lm.removeUpdates(lcoarse);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		@Override
		public void onProviderDisabled(String arg0) {}

		@Override
		public void onProviderEnabled(String arg0) {}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
		
	};

	private LocationListener lnormal = new LocationListener() {
		public void onProviderDisabled(String provider) {}

		public void onProviderEnabled(String provider) {}

		public void onStatusChanged(String provider, int status, Bundle extras) {}

		public void onLocationChanged(Location location) {
			Log.d(TAG, "normal Location Changed: "+location.getProvider()+" lat: "+location.getLatitude()+" lon: "+location.getLongitude()+" alt: "+location.getAltitude()+" acc: "+location.getAccuracy());
			//Toast.makeText(ctx, "NORMAL: Location Changed: "+location.getProvider()+" lat: "+location.getLatitude()+" lon: "+location.getLongitude()+" alt: "+location.getAltitude()+" acc: "+location.getAccuracy(), Toast.LENGTH_LONG).show();
			try {

				
				Log.v(TAG,"Location Changed: "+location.getProvider()+" lat: "+location.getLatitude()+" lon: "+location.getLongitude()+" alt: "+location.getAltitude()+" acc: "+location.getAccuracy());
					synchronized (curLoc) {
						curLoc = location;
					}
//					mixView.repaint();
					Location lastLoc=getLocationAtLastDownload();
					if(lastLoc==null)
						setLocationAtLastDownload(location);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	};
	
}
