package com.uofc.roomfinder.android.util;

import java.util.List;
import java.util.Vector;

import com.esri.core.geometry.Line;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.Segment;
import com.uofc.roomfinder.entities.routing.Gradient;
import com.uofc.roomfinder.entities.routing.Route;
import com.uofc.roomfinder.entities.routing.RouteFeature;
import com.uofc.roomfinder.entities.routing.RoutePoint;
import com.uofc.roomfinder.entities.routing.RouteSegment;

public class RouteAnalyzerOld {

	/**
	 * generates route segments and analyzes the direction list returned by the NA server
	 * 
	 * @param route
	 * @return
	 */
	public static boolean analyzeRoute(Route route) {
		if (route.getRouteSegments().size() > 0)
			return true;

		// analyze way points (segments from server)
		analyzeWayPoints(route);

		// analyze segments (split route in useful parts)
		// on height change or entering a building, ...
		analyzeSegments(route);

		// calculate length of whole route
		double totalLength = 0;
		for (RouteSegment segment : route.getRouteSegments()) {
			totalLength += segment.getLength();
		}
		route.setLength(totalLength);

		if (route.getRouteSegments().size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * split route in segments 
	 * 
	 * @param route
	 * @param path
	 * @param segment
	 */
	private static void analyzeSegments(Route route) {
		System.out.println("segment splitting in progress");
		List<RoutePoint> path = route.getPath();

		Polyline line;
		Segment segment = new Line();

		Gradient currentGradient = Gradient.NEUTRAL;
		Gradient nextGradient = Gradient.NEUTRAL;
		boolean setNewSegment = false;
		int lastSegmentPathPoint = 0;

		RoutePoint nextWaypoint = null;
		RoutePoint currentWayPoint = null;

		line = new Polyline();
		for (int i = 0; i < path.size() - 1; i++) {

			// get ponts from path
			currentWayPoint = route.getPath().get(i);
			nextWaypoint = route.getPath().get(i + 1);

			// add current wp to segment line (to calculate segment length)
			segment.setStart(new Point(currentWayPoint.getX(), currentWayPoint.getY()));
			segment.setEnd(new Point(nextWaypoint.getX(), nextWaypoint.getY()));
			line.addSegment(segment, true);

			// on height change
			if (nextWaypoint.getZ() != currentWayPoint.getZ()) {

				// now it's going down (was before: neutral or upstairs)
				if (nextWaypoint.getZ() < currentWayPoint.getZ() && currentGradient != Gradient.DOWN) {
					nextGradient = Gradient.DOWN;
					setNewSegment = true;
				}

				// now it's going up (was before: neutral or downstairs)
				if (nextWaypoint.getZ() > currentWayPoint.getZ() && currentGradient != Gradient.UP) {
					nextGradient = Gradient.UP;
					setNewSegment = true;
				}
			}

			// it's flat again (was before: up or down)
			if (nextWaypoint.getZ() == currentWayPoint.getZ() && currentGradient != Gradient.NEUTRAL) {
				nextGradient = Gradient.NEUTRAL;
				setNewSegment = true;
			}

			// on entering a building
			// TODO:
			System.out.println(currentWayPoint.getZ() + " - " + currentGradient);

			if (setNewSegment) {
				System.out.println("->split");

				// calculate length of segment and round it to 2 digits
				double segmentLength = line.calculateLength2D();
				long tmpLength = (int) Math.round(segmentLength * 100); // truncates
				segmentLength = tmpLength / 100.0;

				// new route from last segment point to this point
				route.getRouteSegments().add(new RouteSegment(lastSegmentPathPoint, i, "nothing set yet", currentGradient, segmentLength));
				lastSegmentPathPoint = i; // set new last segment point (to begin the next route with)

				currentGradient = nextGradient;
				setNewSegment = false;
				line = new Polyline();
			}

			// current way point is the lastWP in the next loop
			nextWaypoint = currentWayPoint;
		}

		// set last segment (to destination)
		route.getRouteSegments().add(
				new RouteSegment(lastSegmentPathPoint, route.getPath().size() - 1, "segment to destination", currentGradient, line.calculateLength2D()));

		System.out.println("segment splitting done. splitted in " + route.getRouteSegments().size() + " parts");
	}

	/**
	 * splits route into segments segments are getting displayed in the navbar
	 * 
	 * @param route
	 * @param features
	 * @param path
	 * @param waypoints
	 * @return
	 */
	private static void analyzeWayPoints(Route route) {
		List<RouteFeature> features = route.getRouteFeatures();
		List<RoutePoint> path = route.getPath();
		Vector<Integer> waypoints = new Vector<Integer>();

		// set waypoints to the route (got from the NAServer as directions)
		// ================================================================

		int currentWaypoint = 1;
		// add each segment of route to a poly line
		Point pa = null;
		Point pb = null;
		Segment segment = null;
		Polyline line = null;

		System.out.println("waypoints -> " + features.size());

		// cut route into segments by NAServer segments
		waypoints.add(0);
		for (RouteFeature feature : features) {
			double currentSegmentLength = feature.getLength();
			segment = new Line();
			line = new Polyline();

			for (int i = currentWaypoint; i < path.size(); i++) {
				// get points from path
				pa = new Point(route.getPath().get(i - 1).getX(), route.getPath().get(i - 1).getY());
				pb = new Point(route.getPath().get(i).getX(), route.getPath().get(i).getY());

				// set segment
				segment.setStart(pa);
				segment.setEnd(pb);

				// add segment to polyLine
				line.addSegment(segment, true);

				// System.out.println(i + " - " + line.calculateLength2D());

				// current line = line length of NAServer
				if (line.calculateLength2D() >= currentSegmentLength) {
					currentWaypoint = i;
					if (!waypoints.contains(i))
						waypoints.add(i);
					break;
				}
			}
		}
		waypoints.add(route.getPath().size() - 1);

		// set waypoints to route
		route.setWaypointIndicesOfPath(waypoints);
	}

}
