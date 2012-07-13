package com.uofc.roomfinder.android.util.threads;

import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol.STYLE;
import com.uofc.roomfinder.android.DataModel;
import com.uofc.roomfinder.android.util.Constants;
import com.uofc.roomfinder.android.util.Constants.LocationProvider;

/**
 * this thread handles map updates
 * 
 * drawing points according to current position on the map <br/>
 * + switching floor layers
 * 
 * @author benjaminlautenschlaeger
 * 
 */
public class MapViewUpdater implements Runnable {

	private boolean running = true;

	LocationProvider currentProvider;

	@Override
	public void run() {

		while (running) {
			try {
				// wait before redraw
				Thread.sleep(Constants.MAP_UPDATE_INTERVAL);

				// remove everything from location graphics layer
				DataModel.getInstance().getMapActivity().getMapView().getGraphicsLayerLocations().removeAll();

				// draw points
				drawCurrentPosition();
				switchFloorToCurrentPos();

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
	private void drawCurrentPosition() {
		Point currentPos = DataModel.getInstance().getCurrentPositionNAD83();
		currentProvider = DataModel.getInstance().getCurrentLocationProvider();

		System.out.println(currentProvider);
		System.out.println(currentPos);

		// gps accuracy based

		// if location is set go on, else quit
		if (currentPos == null)
			return;

		// is there a value for x?
		try {
			if (currentPos.getX() == 0)
				return;
		} catch (Exception e) {
			return;
		}

		Graphic graphic;

		// according to location provider change color of marker
		if (currentProvider == LocationProvider.GPS) {
			int gpsPointSize = 15;
			graphic = new Graphic(currentPos, new SimpleMarkerSymbol(Constants.COLOR_GPS_MARKER, gpsPointSize, STYLE.CIRCLE));

		} else {
			int wifiPointSize = 15;
			graphic = new Graphic(currentPos, new SimpleMarkerSymbol(Constants.COLOR_WIFI_MARKER, wifiPointSize, STYLE.CIRCLE));

		}

		// show wifi logo
		DataModel.getInstance().getMapActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (MapViewUpdater.this.currentProvider == LocationProvider.GPS) {
					DataModel.getInstance().getMapActivity().showWifiLogo(false);
				} else {
					DataModel.getInstance().getMapActivity().showWifiLogo(true);
				}

			}
		});

		// add point to graphics layer or location
		DataModel.getInstance().getMapActivity().getMapView().getGraphicsLayerLocations().addGraphic(graphic);
	}

	/**
	 * switches floor layer according to current height
	 */
	private void switchFloorToCurrentPos() {

		if (DataModel.getInstance().isUpdateFloorToCurrentPos()) {
			double currentHeight = DataModel.getInstance().getCurrentHeight();
			DataModel.getInstance().getMapActivity().getMapView().setActiveHeight(currentHeight);
		}

	}
}
