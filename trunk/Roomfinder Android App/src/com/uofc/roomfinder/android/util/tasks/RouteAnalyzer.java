package com.uofc.roomfinder.android.util.tasks;

import static com.uofc.roomfinder.android.util.Constants.SPARTIAL_REF_NAD83;

import java.util.List;
import java.util.Vector;

import android.os.AsyncTask;
import android.widget.Toast;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Line;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.Segment;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.ags.query.QueryTask;
import com.uofc.roomfinder.android.DataModel;
import com.uofc.roomfinder.android.map.MapDrawer;
import com.uofc.roomfinder.android.util.Constants;
import com.uofc.roomfinder.entities.Point3D;
import com.uofc.roomfinder.entities.routing.Gradient;
import com.uofc.roomfinder.entities.routing.Route;
import com.uofc.roomfinder.entities.routing.RouteFeature;
import com.uofc.roomfinder.entities.routing.RouteSegment;
import com.uofc.roomfinder.util.Util;

public class RouteAnalyzer extends AsyncTask<Object, Void, FeatureSet> {

	private final static String BUILDING_QUERY_URL = "http://136.159.24.32/ArcGIS/rest/services/Buildings/MapServer/0";

	private Route route;

	@Override
	protected void onPreExecute() {

	}

	/**
	 * analyzes the route (splits route into segments)
	 * 
	 * route splitting is done in two parts: - on height change - on entering or leaving a building
	 * 
	 * for the second part, the traversed buildings have to be queried (thats what this async task is for) in the onPostExecute() method the rout is beeing
	 * analyzed
	 * 
	 * @param queryParams
	 *            first element of array: queryEnvelope <br/>
	 * @return returns every building in the query envelope
	 */
	@Override
	protected FeatureSet doInBackground(Object... params) {
		if (params == null || params.length < 1)
			return null;

		route = (Route) params[0];

		// envelope of the route (the query should return each building in this envelope)
		Envelope queryEnvelope = getRouteEnvelope(route);

		// the building name attached to the result geometries would be good :)
		String[] outputFields = { Constants.QUERY_BUILDING_COL_ID, Constants.QUERY_BUILDING_COL_NAME };

		Query query = new Query();
		query.setGeometry(queryEnvelope);
		query.setOutSpatialReference(SpatialReference.create(SPARTIAL_REF_NAD83));
		query.setOutFields(outputFields);
		query.setReturnIdsOnly(false);
		query.setReturnGeometry(true);
		// query.setWhere(whereClause);

		QueryTask qTask = new QueryTask(BUILDING_QUERY_URL);
		FeatureSet fs = null;

		try {
			fs = qTask.execute(query);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fs;

	}

	@Override
	protected void onPostExecute(FeatureSet result) {

		if (result == null)
			return;

		try {

			// route is already analyzed
			if (route.getRouteSegments().size() > 0)
				return;

			// analyze way points (segments from server)
			analyzeWayPoints(route);

			// analyze segments (split route in useful parts)
			// on height change or entering a building, ...
			analyzeSegments(route, result.getGraphics());

			// calculate length of whole route
			double totalLength = 0;
			for (RouteSegment segment : route.getRouteSegments()) {
				totalLength += segment.getLength();
			}
			route.setLength(totalLength);

			if (route.getRouteSegments().size() < 1) {
				// TODO: display error
				return;
			}

			// set route to data model
			DataModel.getInstance().setRoute(route);

			// paint navigation bar
			DataModel.getInstance().getMapActivity().getMapNavBar().createNavigationBar(route.getRouteSegments());

			// display first segment of route
			MapDrawer.displayRouteSegment(0);

		} catch (Exception e1) {
			Toast toast = Toast.makeText(DataModel.getInstance().getMapActivity(), "error 101: cannot display route", Toast.LENGTH_LONG);
			toast.show();
			e1.printStackTrace();
		}
	}

	/**
	 * creates a graphic of the route and determines the envelope of the route
	 * 
	 * @param route
	 * @return envelope of the route
	 */
	private Envelope getRouteEnvelope(Route route) {
		// add each segment of route to a poly line
		Point pa = null;
		Point pb = null;

		// build line
		Segment segment = new Line();
		Polyline line = new Polyline();

		// add each point as segemnt to line
		for (int i = 1; i < route.getPath().size(); i++) {
			// get points from path
			pa = new Point(route.getPath().get(i - 1).getX(), route.getPath().get(i - 1).getY());
			pb = new Point(route.getPath().get(i).getX(), route.getPath().get(i).getY());

			// set segment
			segment.setStart(pa);
			segment.setEnd(pb);

			// add segment to polyLine
			line.addSegment(segment, true);
		}
		Envelope env = new Envelope();
		line.queryEnvelope(env);
		return env;
	}

	/**
	 * split route in segments
	 * 
	 * @param route
	 * @param path
	 * @param segment
	 */
	private static void analyzeSegments(Route route, Graphic[] buildings) {
		System.out.println("segment splitting in progress");
		List<Point3D> path = route.getPath();

		Polyline line = new Polyline();
		Segment segment = new Line();

		Gradient currentGradient = Gradient.NEUTRAL;
		Gradient nextGradient = Gradient.NEUTRAL;
		boolean setNewSegment = false;
		String newSegmentText = null;
		String newSegmentLocation = null;
		String currentSegmentLocation = null;
		String oldoldSegmentLocation = null; // segment before the last segment, is needed to sort out weird stuff from the naserver...
		int lastSegmentPathPoint = 0;

		Point3D nextWaypoint = null;
		Point3D currentWayPoint = null;

		for (int i = 0; i < path.size() - 1; i++) {

			// get ponts from path
			currentWayPoint = route.getPath().get(i);
			nextWaypoint = route.getPath().get(i + 1);

			// add current wp to segment line (to calculate segment length)
			segment.setStart(new Point(currentWayPoint.getX(), currentWayPoint.getY()));
			segment.setEnd(new Point(nextWaypoint.getX(), nextWaypoint.getY()));
			line.addSegment(segment, true);

			// determine current segment location
			// in which building is the current waypoint
			currentSegmentLocation = determineContainingBuilding(buildings, new Point(currentWayPoint.getX(), currentWayPoint.getY()));
			newSegmentLocation = determineContainingBuilding(buildings, new Point(nextWaypoint.getX(), nextWaypoint.getY()));
			// System.out.println("cur" + currentSegmentLocation + " next " + newSegmentLocation);

			// on height change (if not outside)
			// =================
			if (nextWaypoint.getZ() != currentWayPoint.getZ()
					&& !determineContainingBuilding(buildings, new Point(currentWayPoint.getX(), currentWayPoint.getY())).equals("outside")) {

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

			// on entering or leaving a building
			// =======================
			if (!currentSegmentLocation.equals(newSegmentLocation)) {
				setNewSegment = true;
				if (newSegmentLocation.equals("outside")) {
					newSegmentText = "Leave building";
				} else {
					newSegmentText = "Enter " + newSegmentLocation;
				}
			}

			// System.out.println(currentWayPoint.getZ() + " - " + currentGradient + " - loc " + currentSegmentLocation);

			if (setNewSegment) {
				System.out.println("->split");

				// calculate length of segment and round it to 2 digits
				double segmentLength = line.calculateLength2D();
				long tmpLength = (int) Math.round(segmentLength * 100); // truncates
				segmentLength = tmpLength / 100.0;

				// new route from last segment point to this point
				route.getRouteSegments().add(new RouteSegment(lastSegmentPathPoint, i, newSegmentText, currentGradient, segmentLength));
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
				new RouteSegment(lastSegmentPathPoint, route.getPath().size() - 1, "Destination", currentGradient, line.calculateLength2D()));

		for (int i = 0; i < route.getRouteSegments().size(); i++) {
			RouteSegment start = route.getRouteSegments().get(i);
			// System.out.println(i + ": start: " + start.getStartPathPoint() + " end: " + start.getEndPathPoint());
		}

		System.out.println("segments before cleanup: " + route.getRouteSegments().size());
		cleanupSegments(route);
		System.out.println("segments after cleanup: " + route.getRouteSegments().size());

		updateDescriptions(route);

		System.out.println("segment splitting done. splitted in " + route.getRouteSegments().size() + " parts");
	}

	/**
	 * helper method which cleans up segments
	 * 
	 * @param route2
	 */
	private static void cleanupSegments(Route route) {

		// clean messed up segments

		// case 1: up-neutral-up in short distance
		// sometimes something like this happens in elevators
		// (go up by elevator to 2nd floor-go out and to next elevator-go up by elevator to 5th floor)
		for (int i = 1; i < route.getRouteSegments().size() - 2; i++) {
			// has to start with UP
			if (route.getRouteSegments().get(i).getGradient() == Gradient.UP) {
				System.out.println("first step");
				// followed by NEUTRAL-UP
				if (route.getRouteSegments().get(i + 1).getGradient() == Gradient.NEUTRAL && route.getRouteSegments().get(i + 2).getGradient() == Gradient.UP) {
					System.out.println("merging segments starting at " + i);
					mergeRouteSegments(route, i, 2);
				}
			}
		}
	}

	private static void updateDescriptions(Route route) {

		for (int i = 1; i < route.getRouteSegments().size() - 1; i++) {
			// has to start with UP
			if (route.getRouteSegments().get(i).getGradient() == Gradient.UP || route.getRouteSegments().get(i).getGradient() == Gradient.DOWN) {
				// followed by NEUTRAL
				if (route.getRouteSegments().get(i + 1).getGradient() == Gradient.NEUTRAL) {

					// determine destination floor name
					String floor = "";
					long lngFloor = (long) route.getPath().get(route.getRouteSegments().get(i + 1).getStartPathPoint()).getZ();
					if (lngFloor % 4 == 0) {
						long longFloor = lngFloor / 4 + 1;
						floor = Util.rPad("" + longFloor, 2, '0');
					}

					String newText = "Go to floor " + floor;
					route.getRouteSegments().get(i).setDescription(newText);
				}
			}
		}

		for (int i = 1; i < route.getRouteSegments().size() - 1; i++) {
			// followed by NEUTRAL
			if (route.getRouteSegments().get(i).getGradient() == Gradient.NEUTRAL) {
				// has to start with UP
				if (route.getRouteSegments().get(i + 1).getGradient() == Gradient.UP || route.getRouteSegments().get(i).getGradient() == Gradient.DOWN) {
					String newText = "Go to elevator/staircases";
					route.getRouteSegments().get(i).setDescription(newText);
				}
			}
		}
	}

	/**
	 * merges route segments to one segment has to be done, because na server messes up some routes
	 * 
	 * @param route
	 * @param startSegmentIndex
	 * @param segmentCountsToMerge
	 */
	private static void mergeRouteSegments(Route route, int startSegmentIndex, int segmentCountsToMerge) {
		RouteSegment start = route.getRouteSegments().get(startSegmentIndex);
		RouteSegment additionalSegment;

		// add segments to start segment
		for (int i = startSegmentIndex + 1; i <= startSegmentIndex + segmentCountsToMerge; i++) {
			additionalSegment = route.getRouteSegments().get(i);
			// System.out.println("adding segment " + i + " to segment " + startSegmentIndex + "with endPoint: " + additionalSegment.getEndPathPoint());

			// add values to start segment
			start.addLength(additionalSegment.getLength());
			start.setEndPathPoint(additionalSegment.getEndPathPoint());
			start.setDescription(additionalSegment.getDescription());

		}

		for (int i = 0; i < route.getRouteSegments().size(); i++) {
			RouteSegment tmp = route.getRouteSegments().get(i);
			// System.out.println(i + ": start: " + tmp.getStartPathPoint() + " end: " + tmp.getEndPathPoint());
		}

		// delete segments
		for (int i = 0; i < segmentCountsToMerge; i++) {
			route.getRouteSegments().remove(startSegmentIndex + 1);

		}

	}

	/**
	 * get containing building of segment
	 * 
	 * @param buildings
	 * @param segment
	 * @return string of containing building, if no building is containing the segment "outdoor" will be returned
	 */
	private static String determineContainingBuilding(Graphic[] buildings, Point segment) {
		for (Graphic building : buildings) {
			try {

				if (GeometryEngine.contains(building.getGeometry(), segment, SpatialReference.create(Constants.SPARTIAL_REF_NAD83))) {
					return (String) building.getAttributeValue(com.uofc.roomfinder.android.util.Constants.QUERY_BUILDING_COL_ID);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "outside";
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
		List<Point3D> path = route.getPath();
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
