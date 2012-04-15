/**
 * 
 */
package com.uofc.roomfinder.android;

import static com.uofc.roomfinder.android.util.Constants.SPARTIAL_REF_WGS84;
import static com.uofc.roomfinder.android.util.Constants.SPARTIAL_REF_NAD83;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;

import com.uofc.roomfinder.android.activities.MapActivity;
import com.uofc.roomfinder.android.util.CoordinateUtil;
import com.uofc.roomfinder.entities.routing.Route;

/**
 * @author benjaminlautenschlaeger
 * 
 */
public class DataModel {

	private static DataModel instance = null;

	Point currentPositionWgs84;

	int currentSegmentStart; // the current waypoint of the displayed route to start with
	
	MapActivity map;
	Route route;

	

	// constructor
	protected DataModel() {
		currentSegmentStart = 0;
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
		return currentPositionWgs84;
	}

	public void setCurrentPositionWGS84(Point currentPosition) {
		this.currentPositionWgs84 = currentPosition;
	}

	public Point getCurrentPositionNAD83() {
		Geometry nad83 = CoordinateUtil.transformGeometryToNAD83(currentPositionWgs84, SpatialReference.create(SPARTIAL_REF_WGS84));
		return CoordinateUtil.getCenterCoordinateOfGeometry(nad83);
	}

	public void setCurrentPositionNAD83(Point currentPosition) {
		Geometry nad83 = CoordinateUtil.transformGeometryToNAD83(currentPositionWgs84, SpatialReference.create(SPARTIAL_REF_NAD83));
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

	public MapActivity getMap() {
		return map;
	}

	public void setMap(MapActivity map) {
		this.map = map;
	}

	public Route getRoute() {
		return route;
	}

	public void setRoute(Route route) {
		this.route = route;
	}
	

	
}
