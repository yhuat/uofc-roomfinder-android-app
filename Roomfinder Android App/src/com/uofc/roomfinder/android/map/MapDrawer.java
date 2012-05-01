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
import com.uofc.roomfinder.entities.routing.RouteSegment;

public class MapDrawer {

	private static final double SEGMENT_ZOOM_FACTOR = 2.2;

	/**
	 * displays the specified route segment on the route in the data model
	 * 
	 * @param segmentIndex index of the route segment
	 * @throws Exception 
	 */
	public static void displayRouteSegmentOfWaypoint(int segmentIndex) throws Exception {
		RouteSegment segment = DataModel.getInstance().getRoute().getRouteSegments().get(segmentIndex);
		displayRouteSegmentOfWaypoint(segment);
	}
	
	/**
	 * displays the specified route segment on the route in the data model
	 * 
	 * @param segment segment to display
	 * @throws Exception
	 */
	public static void displayRouteSegmentOfWaypoint(RouteSegment segment) throws Exception {
		displayRouteSegmentOfWaypoint(DataModel.getInstance().getRoute(), segment);
	}
	
	/**
	 * displays the specified route segment
	 *
	 * @param segment segment index to display
	 * @throws Exception
	 * 
	 */
	public static void displayRouteSegmentOfWaypoint(Route route, RouteSegment segment) throws Exception {

		System.out.println("segment in display method: " + segment);
		
		// get start and end points of the segment
		int startPointOfPath = segment.getStartPathPoint();
		int endPointOfPath = segment.getEndPathPoint();

		// display it
		displayRoute(route, startPointOfPath, endPointOfPath);

	}

	/**
	 * displays whole route
	 * 
	 * @param route
	 * @throws Exception
	 */
	public static void displayRoute(Route route) throws Exception {
		int startPointOfPath = 0;
		int endPointOfPath = route.getPath().size() - 1;
		displayRoute(route, startPointOfPath, endPointOfPath);
	}

	/**
	 * displays a route on graphical layer of map view from start point to end point
	 * 
	 * @param route
	 * @param startPointOfPath
	 * @param endPointOfPath
	 * @throws Exception
	 */
	public static void displayRoute(Route route, int startPointOfPath, int endPointOfPath) throws Exception {

		// remove everything from graphics layer
		DataModel.getInstance().getMap().getGraphicsLayer().removeAll();

		// get segments of path if not available
		if (route.getRouteSegments().size() < 1) {
			RouteUtil.analyzeRoute(route);

			// no segments could be found quit
			if (route.getRouteSegments().size() < 1) {
				throw new Exception("no segments to display");
			}
		}

		// display circle at each waypoint
		/*
		for (RouteSegment segment : route.getRouteSegments()) {
			RoutePoint e = route.getPath().get(segment.getStartPathPoint());
			Graphic graphic = new Graphic(new Point(e.getX(), e.getY()), new SimpleMarkerSymbol(Color.YELLOW, 10, STYLE.CIRCLE));
			DataModel.getInstance().getMap().getGraphicsLayer().addGraphic(graphic);
			
			//endpoints
			e = route.getPath().get(segment.getStartPathPoint());
			graphic = new Graphic(new Point(e.getX(), e.getY()), new SimpleMarkerSymbol(Color.GREEN, 10, STYLE.CIRCLE));
			DataModel.getInstance().getMap().getGraphicsLayer().addGraphic(graphic);
		}*/
		

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
				pb = new Point(route.getPath().get(i + 1).getX(), route.getPath().get(i + 1).getY());

				// set segment
				segment.setStart(pa);
				segment.setEnd(pb);

				// add segment to polyLine
				pLine.addSegment(segment, true);
			}
			// add created line to graphics layer
			DataModel.getInstance().getMap().getGraphicsLayer().addGraphic(new Graphic(pLine, lineSymbol));

			// center route and zoom a bit out
			Envelope env = new Envelope();
			pLine.queryEnvelope(env);

			// create envelope which is a bit bigger than the route segment
			Envelope newEnv = new Envelope(env.getCenter(), env.getWidth() * SEGMENT_ZOOM_FACTOR, env.getHeight() * SEGMENT_ZOOM_FACTOR);
			DataModel.getInstance().getMap().getMapView().setExtent(newEnv);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	

}
