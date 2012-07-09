/**
 * 
 */
package com.uofc.roomfinder.android;

import static com.uofc.roomfinder.android.util.Constants.SPARTIAL_REF_WGS84;
import static com.uofc.roomfinder.android.util.Constants.SPARTIAL_REF_NAD83;

import java.util.Date;

import android.content.Context;
import android.net.wifi.WifiManager;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;

import com.uofc.roomfinder.android.activities.MapActivity;
import com.uofc.roomfinder.android.util.Constants.LocationProvider;
import com.uofc.roomfinder.android.util.CoordinateUtil;
import com.uofc.roomfinder.android.util.LocationHandler;
import com.uofc.roomfinder.entities.Point3D;
import com.uofc.roomfinder.entities.routing.Route;

/**
 * @author benjaminlautenschlaeger
 * 
 */
public class DataModel {

	private static DataModel instance = null;

	// location stuff
	private LocationHandler locationHandler;
	private LocationProvider lastPositionProvider;
	private double currentHeight = 0;

	// gps
	private Point3D gpsPosition;
	private double gpsAccuracy;
	private Date gpsTimestamp;

	// wifi
	private Point3D wifiPosition;
	private Date wifiTimestamp;

	// wifi stuff
	private WifiManager wifi;
	// private BroadcastReceiver receiver;

	// map stuff
	private MapActivity mapActivity;
	private Route route;
	private Point3D destinationPoint;
	private String destinationText;
	private int currentSegmentStart; // the current waypoint of the displayed route to start with
	private boolean updateFloorToCurrentPos = true; // if set, map update thread switches layer according to current position

	// constructor
	protected DataModel() {
		currentSegmentStart = 0;
		wifi = (WifiManager) RoomFinderApplication.getAppContext().getSystemService(Context.WIFI_SERVICE);
	}

	// singleton
	public static DataModel getInstance() {
		if (instance == null) {
			instance = new DataModel();
		}
		return instance;
	}

	// getter&setter

	public MapActivity getMapActivity() {
		return mapActivity;
	}

	public void setMap(MapActivity map) {
		this.mapActivity = map;
	}

	public boolean isUpdateFloorToCurrentPos() {
		return updateFloorToCurrentPos;
	}

	public void setUpdateFloorToCurrentPos(boolean updateFloorToCurrentPos) {
		this.updateFloorToCurrentPos = updateFloorToCurrentPos;
	}

	// route stuff
	// ===========
	public Route getRoute() {
		return route;
	}

	public void setRoute(Route route) throws Exception {
		this.route = route;
	}

	public Point3D getDestinationPoint() {
		return destinationPoint;
	}

	public Point3D getDestinationAsWgs84() {
		Geometry wgs84 = CoordinateUtil.transformGeometryToWGS84(new Point(destinationPoint.getX(), destinationPoint.getY(), destinationPoint.getZ()),
				SpatialReference.create(SPARTIAL_REF_NAD83));
		Point destPoint = CoordinateUtil.getCenterCoordinateOfGeometry(wgs84);
		return new Point3D(destPoint.getX(), destPoint.getY(), destinationPoint.getZ());
	}

	public void setDestinationPoint(Point3D destinationPoint) {
		this.destinationPoint = destinationPoint;
	}

	public String getDestinationText() {
		return destinationText;
	}

	public void setDestinationText(String destinationText) {
		this.destinationText = destinationText;
	}

	public int getCurrentSegmentStart() {
		return currentSegmentStart;
	}

	public void setCurrentSegmentStart(int currentSegmentStart) {
		this.currentSegmentStart = currentSegmentStart;
	}

	public void incrementCurrentSegmentStart() {
		currentSegmentStart++;
	}

	// location stuff
	// ==============
	public LocationHandler getLocationHandler() {
		if (locationHandler == null) {
			locationHandler = new LocationHandler();
		}
		return locationHandler;
	}

	public void setLocationHandler(LocationHandler locationHandler) {
		this.locationHandler = locationHandler;
	}

	public WifiManager getWifiManager() {
		return wifi;
	}

	public void setWifiManager(WifiManager wifi) {
		this.wifi = wifi;
	}

	public void setGpsPosition(Point3D gpsPosition) {
		this.gpsPosition = gpsPosition;
		this.gpsTimestamp = new Date();
	}

	public void setGpsAccuracy(double gpsAccuracy) {
		this.gpsAccuracy = gpsAccuracy;
	}

	public void setWifiPosition(Point3D wifiPosition) {
		this.wifiPosition = wifiPosition;
		wifiTimestamp = new Date();
	}

	/**
	 * is a position available
	 * 
	 * @return
	 */
	public boolean isCurrentPositionAvailable() {
		System.out.println("pos available: " + wifiTimestamp == null && gpsTimestamp == null);
		
		if (wifiTimestamp == null && gpsTimestamp == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * location handling method <br/>
	 * decides whether the GPS or WiFI location is returned based on data
	 * 
	 * @return wgs84 point (google maps spatial reference)
	 */
	public Point getCurrentPositionWGS84() {

		// if there is no location fix -> return null
		if (wifiTimestamp == null && gpsTimestamp == null) {
			return null;
		}

		System.out.println("wifi: " + wifiTimestamp + "gpsTS: " + gpsTimestamp);

		// determine best provider

		// if only GPS -> GPS
		if (wifiTimestamp == null && gpsTimestamp != null) {
			lastPositionProvider = LocationProvider.GPS;

			// if only WIFI -> WIFI
		} else if (wifiTimestamp != null && gpsTimestamp == null) {
			lastPositionProvider = LocationProvider.WIFI;

			// if WIFI newer than GPS -> WIFI
		} else if (wifiTimestamp.after(gpsTimestamp)) {
			lastPositionProvider = LocationProvider.WIFI;

			// in any other case -> GPS
		} else {
			lastPositionProvider = LocationProvider.GPS;
		}

		// determine best position
		Point bestPos = null;
		if (lastPositionProvider == LocationProvider.WIFI) {
			bestPos = new Point(this.wifiPosition.getX(), this.wifiPosition.getY());
			currentHeight = this.wifiPosition.getZ();
		} else {
			bestPos = new Point(this.gpsPosition.getX(), this.gpsPosition.getY());

			// if GPS, then z = 0 -> ground layer
			// GPS signal would return the altitude above sea level (which would be over 1000m for Calgary)
			currentHeight = 0;
		}
		return bestPos;
	}

	/**
	 * location handling method decides wether the GPS or WiFI location is returned based on data
	 * 
	 * @return nad83 point (arcgis server spatial reference)
	 */
	public Point getCurrentPositionNAD83() {
		Point currentPositionWgs84 = getCurrentPositionWGS84();

		// no location fix yet
		if (currentPositionWgs84 == null)
			return null;

		Geometry nad83 = CoordinateUtil.transformGeometryToNAD83(currentPositionWgs84, SpatialReference.create(SPARTIAL_REF_WGS84));
		return CoordinateUtil.getCenterCoordinateOfGeometry(nad83);
	}

	/**
	 * returns the current height <br/>
	 * each floor counts as 4 meter (main floor is 0m)
	 * 
	 * @return
	 */
	public double getCurrentHeight() {
		return currentHeight;
	}

	public LocationProvider getCurrentLocationProvider() {
		return lastPositionProvider;
	}

}
