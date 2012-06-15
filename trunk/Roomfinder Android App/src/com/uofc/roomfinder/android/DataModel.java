/**
 * 
 */
package com.uofc.roomfinder.android;

import static com.uofc.roomfinder.android.util.Constants.SPARTIAL_REF_WGS84;
import static com.uofc.roomfinder.android.util.Constants.SPARTIAL_REF_NAD83;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.net.wifi.WifiManager;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;

import com.uofc.roomfinder.android.activities.MapActivity;
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
	LocationHandler locationHandler;
	Point currentPositionWgs84;

	// gps
	Point3D gpsPosition;
	double gpsAccuracy;
	Date gpsTimestamp;

	// wifi
	Point3D wifiPosition;
	Date wifiTimestamp;

	// wifi stuff
	private WifiManager wifi;
	private BroadcastReceiver receiver;

	// map stuff
	MapActivity mapActivity;
	Route route;
	Point3D destinationPoint;
	String destinationText;
	int currentSegmentStart; // the current waypoint of the displayed route to start with

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
	public Point getCurrentPositionWGS84() {
		currentPositionWgs84 = new Point(this.locationHandler.getCurrentLocation().getLongitude(), this.locationHandler.getCurrentLocation().getLatitude(),
				this.locationHandler.getCurrentLocation().getAltitude());

		return currentPositionWgs84;
	}

	public void setCurrentPositionWGS84(Point currentPosition) {
		this.currentPositionWgs84 = currentPosition;
	}

	public Point getCurrentPositionNAD83() {
		currentPositionWgs84 = getCurrentPositionWGS84();

		Geometry nad83 = CoordinateUtil.transformGeometryToNAD83(currentPositionWgs84, SpatialReference.create(SPARTIAL_REF_WGS84));
		return CoordinateUtil.getCenterCoordinateOfGeometry(nad83);
	}

	public void setCurrentPositionNAD83(Point currentPosition) {
		Geometry nad83 = CoordinateUtil.transformGeometryToNAD83(currentPositionWgs84, SpatialReference.create(SPARTIAL_REF_WGS84));
		this.currentPositionWgs84 = CoordinateUtil.getCenterCoordinateOfGeometry(nad83);
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

	public MapActivity getMapActivity() {
		return mapActivity;
	}

	public void setMap(MapActivity map) {
		this.mapActivity = map;
	}

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

	public Point3D getGpsPosition() {
		return gpsPosition;
	}

	public void setGpsPosition(Point3D gpsPosition) {
		this.gpsPosition = gpsPosition;
		this.gpsTimestamp = new Date();
	}

	public double getGpsAccuracy() {
		return gpsAccuracy;
	}

	public void setGpsAccuracy(double gpsAccuracy) {
		this.gpsAccuracy = gpsAccuracy;
	}

	public Date getGpsTimestamp() {
		return gpsTimestamp;
	}

	public Point3D getWifiPosition() {
		return wifiPosition;
	}

	public void setWifiPosition(Point3D wifiPosition) {
		this.wifiPosition = wifiPosition;
		wifiTimestamp = new Date();
	}

	public Date getWifiTimestamp() {
		return wifiTimestamp;
	}

	

}
