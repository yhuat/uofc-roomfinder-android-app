package com.uofc.roomfinder.android.util;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.uofc.roomfinder.android.DataModel;
import com.uofc.roomfinder.android.util.tasks.RoomQuery;
import com.uofc.roomfinder.android.util.tasks.RouteDownloader;
import com.uofc.roomfinder.entities.Point3D;

public class GisServerUtil {

	/**
	 * converts the known abbreviation into the correct abbreviation for a query
	 * 
	 * @param buildingAbbreviation
	 * @return
	 */
	public static String getBuildingAbbreviationForQuery(String buildingAbbreviation) {
		String result = buildingAbbreviation;

		if (buildingAbbreviation.equals("KNA")) {
			result = "KN";
		}
		return result;
	}

	/**
	 * starts an async task for querying the route displays a route
	 * 
	 * @param building
	 * @param room
	 * @param impedance
	 */
	public static void startRoomWithRouteQuery(String building, String room, String impedance) {

		Object[] queryParams = { building, room, impedance };
		RoomQuery asyncQuery = new RoomQuery();
		asyncQuery.execute(queryParams);

	}

	/**
	 * starts an async task for querying a building only shows shape of a building
	 * 
	 * @param building
	 * @param room
	 */
	public static void startRoomQuery(String building, String room) {

		Object[] queryParams = { building, room };
		RoomQuery asyncQuery = new RoomQuery();
		asyncQuery.execute(queryParams);
	}

	/**
	 * initiates the route querying
	 * 
	 * @param result
	 * @param building
	 * @param room
	 * @param impedance
	 */
	public static void createRoute(FeatureSet result, String building, String room, String impedance) {
		System.out.println("cr" + impedance);
		// display graphic
		Graphic[] grs = result.getGraphics();

		// get floor out of result feature set
		String floorResult = (String) result.getGraphics()[0].getAttributeValue(com.uofc.roomfinder.android.util.Constants.QUERY_ROOM_COL_FLR_ID);

		// get Route of NA server
		Point startPoint = DataModel.getInstance().getCurrentPositionNAD83();
		Point endPoint = CoordinateUtil.getCenterCoordinateOfGeometry(grs[0].getGeometry());

		// build destination point for route
		Point3D routeStart = new Point3D(startPoint.getX(), startPoint.getY());
		Point3D routeEnd = new Point3D(endPoint.getX(), endPoint.getY(), CoordinateUtil.getZCoordFromFloor(floorResult));

		// start async thread for downloading route
		new RouteDownloader().execute(routeStart, routeEnd, impedance);
		
		// set destination to data model
		DataModel.getInstance().setDestinationPoint(routeEnd);
		DataModel.getInstance().setDestinationText("Route to " + building + " " + room);
	}
	
	/**
	 * @param result
	 */
	public static void createRoute(Point3D endPoint, String destination) {
		String impedance = "Length";

		// get current pos
		Point startPoint = DataModel.getInstance().getCurrentPositionNAD83();

		// build destination point for route
		Point3D routeStart = new Point3D(startPoint.getX(), startPoint.getY());
		Point3D routeEnd = new Point3D(endPoint.getX(), endPoint.getY(), endPoint.getZ());

		// start async thread for downloading route
		new RouteDownloader().execute(routeStart, routeEnd, impedance);
		
		// set destination to data model
		DataModel.getInstance().setDestinationPoint(routeEnd);
		DataModel.getInstance().setDestinationText("Route to " + destination);
	}

	/**
	 * @param result
	 */
	public static void createBuildingShape(FeatureSet result, String building, String room) {
		// remove graphics from graphics layer
		DataModel.getInstance().getMapActivity().getMapView().getGraphicsLayer().removeAll();

		// display graphic
		Graphic[] grs = result.getGraphics();
		DataModel.getInstance().getMapActivity().getMapView().getGraphicsLayer().addGraphics(grs);

		// zoom to room
		// create envelope which is a bit bigger than the route segment
		Envelope env = new Envelope();
		result.getGraphics()[0].getGeometry().queryEnvelope(env);
		Envelope newEnv = new Envelope(env.getCenter(), env.getWidth() * Constants.ROOM_ZOOM_FACTOR, env.getHeight() * Constants.ROOM_ZOOM_FACTOR);
		DataModel.getInstance().getMapActivity().getMapView().setExtent(newEnv);

		// destination point
		Point endPoint = CoordinateUtil.getCenterCoordinateOfGeometry(grs[0].getGeometry());

		// build destination point
		String floorResult = (String) result.getGraphics()[0].getAttributeValue(com.uofc.roomfinder.android.util.Constants.QUERY_ROOM_COL_FLR_ID);
		Point3D routeEnd = new Point3D(endPoint.getX(), endPoint.getY(), CoordinateUtil.getZCoordFromFloor(floorResult));

		// set layer to destination point
		DataModel.getInstance().getMapActivity().getMapView().setActiveHeight(routeEnd);

		// set destination to data model
		DataModel.getInstance().setDestinationPoint(routeEnd);

		// display result as an info box on map view
		DataModel.getInstance().getMapActivity().displayInfoBox(building + " " + room + "\n" + DataModel.getInstance().getDestinationText());
	}

}