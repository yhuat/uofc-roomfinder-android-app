package com.uofc.roomfinder.android.map;

import android.graphics.Color;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Line;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.Segment;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol.STYLE;
import com.esri.core.symbol.Symbol;
import com.uofc.roomfinder.android.DataModel;
import com.uofc.roomfinder.android.util.RouteUtil;
import com.uofc.roomfinder.entities.routing.Route;
import com.uofc.roomfinder.entities.routing.RoutePoint;

public class MapDrawer {

	/**
	 * 
	 */
	public static void displayRouteSegmentOfWaypoint(Route route, int segmentCount) {
		
		//is segment count available?
		if (segmentCount > route.getWaypointIndicesOfPath().size()){
			
		}
		
		// get start and end points of the segment
		int startPointOfPath = route.getWaypointIndicesOfPath().get(segmentCount);
		int endPointOfPath = route.getWaypointIndicesOfPath().get(segmentCount+1);
		
		//display it
		displayRoute(route, startPointOfPath, endPointOfPath);
	}

	/**
	 * displays whole route
	 * @param route
	 */
	public static void displayRoute(Route route) {
		int startPointOfPath = 0;
		int endPointOfPath = route.getPath().size();
		displayRoute(route, startPointOfPath, endPointOfPath);
	}
	
	/**
	 * displays a route on graphical layer of map view from start point to end point
	 * 
	 * @param route
	 * @param startPointOfPath
	 * @param endPointOfPath
	 */
	public static void displayRoute(Route route, int startPointOfPath, int endPointOfPath) {

		// remove everything from graphics layer
		DataModel.getInstance().getMap().getGraphicsLayer().removeAll();

		// get segments of path if not available
		if (route.getWaypointIndicesOfPath().size() < 1) {
			RouteUtil.getSegmentsOfRoute(route);
		}

		// display circle at each waypoint
		for (Integer waypoint : route.getWaypointIndicesOfPath()) {
			RoutePoint e = route.getPath().get(waypoint);
			Graphic graphic = new Graphic(new Point(e.getX(), e.getY()), new SimpleMarkerSymbol(Color.YELLOW, 10, STYLE.CIRCLE));
			DataModel.getInstance().getMap().getGraphicsLayer().addGraphic(graphic);
		}

		// create poly line
		Polyline pLine = new Polyline();
		Symbol lineSymbol = new SimpleLineSymbol(Color.BLUE, 4);

		// add each segment of route to a poly line
		Point pa = null;
		Point pb = null;
		Segment segment = new Line();
		try {
			for (int i = startPointOfPath; i < endPointOfPath; i++) {
				// get points from path
				pa = new Point(route.getPath().get(i).getX(), route.getPath().get(i).getY());
				pb = new Point(route.getPath().get(i+1).getX(), route.getPath().get(i+1).getY());

				// set segment
				segment.setStart(pa);
				segment.setEnd(pb);

				// add segment to polyLine
				pLine.addSegment(segment, true);
			}
			// add created line to graphics layer
			DataModel.getInstance().getMap().getGraphicsLayer().addGraphic(new Graphic(pLine, lineSymbol));
			
			//center route and zoom a bit out
			Envelope env = new Envelope();
			pLine.queryEnvelope(env);
			DataModel.getInstance().getMap().getMapView().setExtent(env);
			DataModel.getInstance().getMap().getMapView().zoomout();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
