package com.uofc.roomfinder.android.util;

import java.util.List;
import java.util.Vector;

import com.esri.core.geometry.Line;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.Segment;
import com.esri.core.map.Graphic;
import com.uofc.roomfinder.entities.routing.Route;
import com.uofc.roomfinder.entities.routing.RouteFeature;
import com.uofc.roomfinder.entities.routing.RoutePoint;

public class RouteUtil {

	public static Vector<Integer> getSegmentsOfRoute(Route route) {
		List<RouteFeature> features = route.getRouteFeatures();
		List<RoutePoint> path = route.getPath();
		Vector<Integer> waypoints = new Vector<Integer>();
		
		int currentWaypoint = 1;

		// add each segment of route to a poly line
		Point pa = null;
		Point pb = null;
		Segment segment = null; 
		Polyline line = null;

		// cut route into segments by NAServer segments
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
				
				System.out.println(i + " - " + line.calculateLength2D());
				
				//current line = line length of NAServer
				if (line.calculateLength2D() >= currentSegmentLength){
					System.out.println("breakpoint: " + i);
					currentWaypoint = i;	
					waypoints.add(i);
					break;
				}				
			}
		}
		
		return waypoints;
	}

}
