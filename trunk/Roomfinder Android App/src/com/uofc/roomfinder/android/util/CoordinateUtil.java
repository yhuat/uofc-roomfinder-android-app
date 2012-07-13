package com.uofc.roomfinder.android.util;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.uofc.roomfinder.entities.Point3D;

import static com.uofc.roomfinder.android.util.Constants.*;

/**
 * NAD83 -> Spartial Reference:26911 (Default ArcGIS SR)
 * 
 * @author benjaminlautenschlaeger
 * 
 */
public class CoordinateUtil {

	/**
	 * calculates the center Position of an graphicsArray
	 * 
	 * @param graphicsArray
	 * @return center position
	 */
	public static Point getCenterCoordinateOfGraphicsArray(Graphic[] graphicsArray) {

		if (graphicsArray.length == 1) {
			// only one graphic element in array
			return getCenterCoordinateOfGraphic(graphicsArray[0]);
		} else {
			// multiple graphic elements in array
			// TODO: write logic for more graphic elements
			return getCenterCoordinateOfGraphic(graphicsArray[0]);
		}
	}

	/**
	 * gets center position of graphic element
	 * 
	 * @param graphic
	 * @return center position of graphic element
	 */
	public static Point getCenterCoordinateOfGraphic(Graphic graphic) {

		Envelope env = new Envelope();
		graphic.getGeometry().queryEnvelope(env);

		return env.getCenter();
	}

	/**
	 * gets center position of geometry
	 * 
	 * @param graphic
	 * @return center position of geometry
	 */
	public static Point getCenterCoordinateOfGeometry(Geometry geometry) {
		Envelope env = new Envelope();
		geometry.queryEnvelope(env);

		return env.getCenter();
	}

	/**
	 * transform the given geometry (with the given spatial ref) into a a WGS84 projection
	 * 
	 * @param geometry
	 * @param originalSpatialReference
	 * @return
	 */
	public static Geometry transformGeometryToWGS84(Geometry geometry, SpatialReference originalSpatialReference) {
		SpatialReference wgs84sr = SpatialReference.create(SPARTIAL_REF_WGS84);
		Geometry resultGeometry = GeometryEngine.project(geometry, originalSpatialReference, wgs84sr);

		return resultGeometry;
	}

	/**
	 * transform the given geometry (with the given spatial ref) into a a NAD83 projection
	 * 
	 * @param geometry
	 * @param originalSpatialReference
	 * @return
	 */
	public static Geometry transformGeometryToNAD83(Geometry geometry, SpatialReference originalSpatialReference) {
		SpatialReference nad83sr = SpatialReference.create(SPARTIAL_REF_NAD83);
		Geometry resultGeometry = GeometryEngine.project(geometry, originalSpatialReference, nad83sr);

		return resultGeometry;
	}

	/**
	 * determine the z-coordinate with the help of the floor_id assume each floor has the height of 4m (same assumption was made when designing the network data
	 * sets)
	 * 
	 * @param floor
	 * @return z-coordinate
	 */
	public static double getZCoordFromFloor(String floor) {

		int numFloor;

		// if the string is not a number follow the rules to determine z-coordinate
		if (!isNumeric(floor)) {

			if (floor.equals("B1")) {
				numFloor = -1;
			} else if (floor.equals("B2")) {
				numFloor = -2;
			} else if (floor.equals("P1")) {
				numFloor = 14; // probably wrong
			} else if (floor.equals("P2")) {
				numFloor = 14; // probably wrong
			} else if (floor.equals("M1")) {
				numFloor = 1;
			} else if (floor.equals("G1")) {
				numFloor = -1;
			} else if (floor.equals("G2")) {
				numFloor = -2;
			} else {
				numFloor = 1;
			}
		}
		// if it's a number convert it to int
		else {
			numFloor = Integer.parseInt(floor) - 1;
		}
		return numFloor * 4;
	}

	/**
	 * checks whether a string is numeric or not
	 * 
	 * @param str
	 *            potential number
	 * @return
	 */
	public static boolean isNumeric(String str) {
		try {
			Integer.parseInt(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	/**
	 * projects a nad83 point into a wgs84 point
	 * 
	 * @param nad83Point
	 * @return
	 */
	public static Point3D transformToWGS84(Point3D nad83Point) {
		// transform location into spatial reference system of map
		SpatialReference nad83sr = SpatialReference.create(SPARTIAL_REF_NAD83);
		SpatialReference wgs84sr = SpatialReference.create(SPARTIAL_REF_WGS84);

		// if location is set go on, else quit
		if (nad83Point == null || nad83Point.getX() == 0)
			return null;

		Point point = new Point(nad83Point.getX(), nad83Point.getY());
		Point pointWgs84 = (Point) GeometryEngine.project(point, nad83sr, wgs84sr);

		return new Point3D(pointWgs84.getX(), pointWgs84.getY());

	}

}
