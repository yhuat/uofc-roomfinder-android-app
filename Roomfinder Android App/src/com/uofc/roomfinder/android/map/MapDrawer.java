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
import com.uofc.roomfinder.android.util.Constants;
import com.uofc.roomfinder.entities.routing.Gradient;
import com.uofc.roomfinder.entities.routing.Route;
import com.uofc.roomfinder.entities.routing.RouteSegment;

public class MapDrawer {

	private static final int ROUTE_LINE_COLOR = Color.BLUE;
	private static final int ROUTE_LINE_SIZE = 4;
	private static final int ROUTE_POINT_SIZE = 6;

	/**
	 * displays the specified route segment on the route in the data model
	 * 
	 * @param segmentIndex
	 *            index of the route segment
	 * @throws Exception
	 */
	public static void displayRouteSegment(int segmentIndex) throws Exception {
		String info_text = DataModel.getInstance().getDestinationText();
		info_text += " (" + (segmentIndex + 1) + "/" + DataModel.getInstance().getRoute().getRouteSegments().size() + ")";
		info_text += "\n " + DataModel.getInstance().getRoute().getRouteSegments().get(segmentIndex).getLength() + "gr: "
				+ DataModel.getInstance().getRoute().getRouteSegments().get(segmentIndex).getGradient();

		// display info box on map view
		DataModel.getInstance().getMapActivity().displayInfoBox(info_text);

		RouteSegment segment = DataModel.getInstance().getRoute().getRouteSegments().get(segmentIndex);

		// display route
		displayRouteSegment(segment);
	}

	/**
	 * displays the specified route segment on the route in the data model
	 * 
	 * @param segment
	 *            segment to display
	 * @throws Exception
	 */
	public static void displayRouteSegment(RouteSegment segment) throws Exception {
		displayRouteSegment(DataModel.getInstance().getRoute(), segment);
	}

	/**
	 * displays the specified route segment
	 * 
	 * @param segment
	 *            segment index to display
	 * @throws Exception
	 * 
	 */
	public static void displayRouteSegment(Route route, RouteSegment segment) throws Exception {

		System.out.println("segment in display method: " + segment);

		// get start and end points of the segment
		int startPointOfPath = segment.getStartPathPoint();
		int endPointOfPath = segment.getEndPathPoint();

		// display it
		displayRoute(route, startPointOfPath, endPointOfPath, segment.getGradient());

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

	// handling default value for gradient
	private static void displayRoute(Route route, int startPointOfPath, int endPointOfPath) throws Exception {
		displayRoute(route, startPointOfPath, endPointOfPath, Gradient.NEUTRAL);
	}

	/**
	 * displays a route on graphical layer of map view from start point to end point
	 * 
	 * @param route
	 * @param startPointOfPath
	 * @param endPointOfPath
	 * @throws Exception
	 */
	public static void displayRoute(Route route, int startPointOfPath, int endPointOfPath, Gradient gradient) throws Exception {

		// no segments could be found quit
		if (route.getRouteSegments().size() < 1) {
			throw new Exception("no segments to display");
		}

		Envelope env;
		double zoomWidth;

		// if the segment is neutral display the route as a line
		if (gradient == Gradient.NEUTRAL) {
			env = drawLine(route, startPointOfPath, endPointOfPath);
			zoomWidth = env.getWidth() * Constants.SEGMENT_ZOOM_FACTOR;
		} else {
			// otherwise just display the starting point
			env = drawPoint(route, startPointOfPath);
			zoomWidth = 50;
		}

		// switch to according room layer
		DataModel.getInstance().getMapActivity().getMapView().setActiveHeight(route.getPath().get(startPointOfPath).getZ());

		// create envelope which is a bit bigger than the route segment
		Envelope newEnv = new Envelope(env.getCenter(), zoomWidth, zoomWidth);
		DataModel.getInstance().getMapActivity().getMapView().setExtent(newEnv);
	}

	/**
	 * draws a point on the graphic layer
	 * 
	 * @param route
	 * @param pointIndex
	 * @return
	 */
	private static Envelope drawPoint(Route route, int pointIndex) {
		// remove everything from graphics layer
		DataModel.getInstance().getMapActivity().getMapView().getGraphicsLayer().removeAll();

		Point point = new Point(route.getPath().get(pointIndex).getX(), route.getPath().get(pointIndex).getY());
		Graphic graphic = new Graphic(point, new SimpleMarkerSymbol(ROUTE_LINE_COLOR, ROUTE_POINT_SIZE, STYLE.CIRCLE));

		// add created line to graphics layer
		DataModel.getInstance().getMapActivity().getMapView().getGraphicsLayer().addGraphic(graphic);

		// query and return the envelope of the line
		Envelope env = new Envelope();
		point.queryEnvelope(env);

		return env;
	}

	/**
	 * draws the segment as a line on the graphics layer
	 * 
	 * @param route
	 * @param startPointOfPath
	 * @param endPointOfPath
	 * @return
	 */
	private static Envelope drawLine(Route route, int startPointOfPath, int endPointOfPath) {
		// remove everything from graphics layer
		DataModel.getInstance().getMapActivity().getMapView().getGraphicsLayer().removeAll();

		// create poly line
		Polyline pLine = new Polyline();
		Symbol lineSymbol = new SimpleLineSymbol(ROUTE_LINE_COLOR, ROUTE_LINE_SIZE);

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

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// add created line to graphics layer
		DataModel.getInstance().getMapActivity().getMapView().getGraphicsLayer().addGraphic(new Graphic(pLine, lineSymbol));

		// query and return the envelope of the line
		Envelope env = new Envelope();
		pLine.queryEnvelope(env);

		return env;
	}

}
