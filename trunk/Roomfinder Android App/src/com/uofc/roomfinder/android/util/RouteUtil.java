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

public class RouteUtil {

	/**
	 * generates route segments and analyzes the direction list returned by the NA server
	 * 
	 * @param route
	 * @return
	 */
	public static boolean analyzeRoute(Route route) {
		if (route.getRouteSegments().size() > 0)
			return true;

		List<RouteFeature> features = route.getRouteFeatures();
		List<RoutePoint> path = route.getPath();
		Vector<Integer> waypoints = new Vector<Integer>();

		// calculate length of whole route
		double totalLength = 0;
		for (RouteFeature feature : features) {
			totalLength += feature.getLength();
		}
		route.setLength(totalLength);

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

		// split route in segments
		// =======================
		RoutePoint lastWayPoint = route.getPath().get(0);
		RoutePoint currentWayPoint = null;
		Gradient gradient = Gradient.neutral;
		boolean setNewSegment = false;
		int lastSegmentPathPoint = 0;

		System.out.println("segment splitting in progress");

		for (int i = 1; i < path.size(); i++) {
			// get points from path
			currentWayPoint = route.getPath().get(i);

			// on height change
			if (lastWayPoint.getZ() != currentWayPoint.getZ()) {

				// now it's going down (was before: neutral or upstairs)
				if (lastWayPoint.getZ() > currentWayPoint.getZ() && gradient != Gradient.down) {
					gradient = Gradient.down;
					setNewSegment = true;
				}

				// now it's going up (was before: neutral or downstairs)
				if (lastWayPoint.getZ() < currentWayPoint.getZ() && gradient != Gradient.up) {
					gradient = Gradient.up;
					setNewSegment = true;
				}
			}

			// it's flat again (was before: up or down)
			if (lastWayPoint.getZ() == currentWayPoint.getZ() && gradient != Gradient.neutral) {
				gradient = Gradient.neutral;
				setNewSegment = true;
			}

			// on entering a building
			// TODO:
			// System.out.println(currentWayPoint.getZ() + " - " + gradient);

			if (setNewSegment) {
				// new route from last segment point to this point
				route.getRouteSegments().add(new RouteSegment(lastSegmentPathPoint, i, "nothing set yet", gradient));
				lastSegmentPathPoint = i; // set new last segment point (to begin the next route with)

				setNewSegment = false;
			}

			// current way point is the lastWP in the next loop
			lastWayPoint = currentWayPoint;
		}

		// set last segment (to destination)
		route.getRouteSegments().add(new RouteSegment(lastSegmentPathPoint, route.getPath().size() - 1, "segment to destination", gradient));

		System.out.println("segment splitting done. splitted in " + route.getRouteSegments().size() + " parts");

		if (route.getRouteSegments().size() > 0) {
			return true;
		} else {
			return false;
		}
	}

}
