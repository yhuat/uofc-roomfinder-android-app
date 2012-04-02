package com.uofc.roomfinder.android.util;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;

import static com.uofc.roomfinder.android.util.Constants.*;

public class CoordinateUtil {

	/**
	 * calculates the center Position of an graphicsArray
	 * 
	 * @param graphicsArray
	 * @return center position
	 */
	public static Point getCenterCoordinateOfGraphicsArray(Graphic[] graphicsArray) {
		
		if (graphicsArray.length == 1){
			//only one graphic element in array
			return getCenterCoordinateOfGraphic(graphicsArray[0]);
		}else{
			//multiple graphic elements in array
			//TODO: write logic for more graphic elements
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
		
		return  env.getCenter();
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
		
		return  env.getCenter();
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
		Geometry resultGeometry = GeometryEngine.project(
				geometry, originalSpatialReference, wgs84sr);
		
		return resultGeometry;
	}
	
	

}
