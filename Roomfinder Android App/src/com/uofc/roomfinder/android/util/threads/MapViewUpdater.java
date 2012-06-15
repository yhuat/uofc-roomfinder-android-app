package com.uofc.roomfinder.android.util.threads;

import static com.uofc.roomfinder.android.util.Constants.SPARTIAL_REF_NAD83;
import static com.uofc.roomfinder.android.util.Constants.SPARTIAL_REF_WGS84;
import android.graphics.Color;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol.STYLE;
import com.uofc.roomfinder.android.DataModel;
import com.uofc.roomfinder.entities.Point3D;

/**
 * this thread invokes every couple of seconds the WifiScanner which invokes the indoor location processing with RSSI and trilateration
 * 
 * @author benjaminlautenschlaeger
 * 
 */
public class MapViewUpdater implements Runnable {

	private final int COLOR_GPS_MARKER = Color.BLUE;
	private final int COLOR_WIFI_MARKER = Color.GREEN;

	private boolean running = true;

	@Override
	public void run() {

		while (running) {
			try {
				// wait before redraw
				Thread.sleep(1000);
				System.out.println("draw loc");

				// remove everything from location graphics layer
				// DataModel.getInstance().getMapActivity().getMapView().getGraphicsLayerLocations().removeAll();

				// draw points
				drawGpsLocation();
				drawWifiData();

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	/**
	 * draws a circle on the map view with the current GPS location stored in the DataModel
	 * 
	 */
	private void drawGpsLocation() {
		// transform location into spatial reference system of map
		SpatialReference nad83sr = SpatialReference.create(SPARTIAL_REF_NAD83);
		SpatialReference wgs84sr = SpatialReference.create(SPARTIAL_REF_WGS84);

		Point3D gpsLocation = DataModel.getInstance().getGpsPosition();

		// if location is set go on, else quit
		if (gpsLocation == null)
			return;
		if (gpsLocation.getX() == 0)
			return;

		Geometry point = new Point(gpsLocation.getX(), gpsLocation.getY());
		Geometry pointWgs84 = GeometryEngine.project(point, wgs84sr, nad83sr);
		int gpsPointSize = (int) DataModel.getInstance().getGpsAccuracy();

		Graphic graphic = new Graphic(pointWgs84, new SimpleMarkerSymbol(COLOR_GPS_MARKER, gpsPointSize, STYLE.CIRCLE));

		// add point to graphics layer or location
		DataModel.getInstance().getMapActivity().getMapView().getGraphicsLayerLocations().addGraphic(graphic);

		// // query and return the envelope of the line
		// Envelope env = new Envelope();
		// point.queryEnvelope(env);
	}

	/**
	 * draws a circle on the map view with the current Wifi location stored in the DataModel
	 */
	private void drawWifiData() {
		// transform location into spatial reference system of map
		SpatialReference nad83sr = SpatialReference.create(SPARTIAL_REF_NAD83);
		SpatialReference wgs84sr = SpatialReference.create(SPARTIAL_REF_WGS84);

		Point3D wifiLocation = DataModel.getInstance().getWifiPosition();

		// if location is set go on, else quit
		if (wifiLocation == null || wifiLocation.getX() == 0)
			return;

		Geometry point = new Point(wifiLocation.getX(), wifiLocation.getY());
		Geometry pointWgs84 = GeometryEngine.project(point, wgs84sr, nad83sr);
		int gpsPointSize = (int) DataModel.getInstance().getGpsAccuracy();

		Graphic graphic = new Graphic(pointWgs84, new SimpleMarkerSymbol(COLOR_WIFI_MARKER, gpsPointSize, STYLE.CIRCLE));

		// add point to graphics layer or location
		DataModel.getInstance().getMapActivity().getMapView().getGraphicsLayerLocations().addGraphic(graphic);

		// switch floor layer (if gps accuracy is good, device is outside -> ground layer)
		if (DataModel.getInstance().getGpsAccuracy() > 20) {
			DataModel.getInstance().getMapActivity().getMapView().setActiveHeight(wifiLocation);
		} else {
			DataModel.getInstance().getMapActivity().getMapView().setActiveHeight(0);
		}
	}
}
