package com.uofc.roomfinder.android.util.tasks;

import static com.uofc.roomfinder.android.util.Constants.MAX_X_QUERY_COORDINATE;
import static com.uofc.roomfinder.android.util.Constants.MAX_Y_QUERY_COORDINATE;
import static com.uofc.roomfinder.android.util.Constants.MIN_X_QUERY_COORDINATE;
import static com.uofc.roomfinder.android.util.Constants.MIN_Y_QUERY_COORDINATE;
import static com.uofc.roomfinder.android.util.Constants.SPARTIAL_REF_NAD83;

import java.lang.reflect.Method;

import android.os.AsyncTask;
import android.widget.Toast;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.ags.query.QueryTask;
import com.uofc.roomfinder.android.DataModel;
import com.uofc.roomfinder.android.map.MapDrawer;
import com.uofc.roomfinder.android.util.Constants;
import com.uofc.roomfinder.android.util.CoordinateUtil;
import com.uofc.roomfinder.android.util.GisServerUtil;
import com.uofc.roomfinder.android.util.RouteAnalyzer;
import com.uofc.roomfinder.entities.routing.Route;
import com.uofc.roomfinder.entities.routing.RoutePoint;
import com.uofc.roomfinder.util.Util;

public class RoomWithRouteQuery extends AsyncTask<Object, Void, FeatureSet> {

	private String impedance;
	private String building;
	private String room;

	@Override
	protected void onPreExecute() {

	}

	/**
	 * performs a search on the map server
	 * 
	 * @param mapActivity
	 *            the map Activity from which this Query is launched
	 * @param queryParams
	 *            elements: 1: building, 2: room, 3: impedance
	 * @return resultSet of query
	 */
	@Override
	protected FeatureSet doInBackground(Object... params) {

		// exit condition
		if (params == null || params.length < 2 || params[1] == null)
			return null;

		building = (String) params[0];
		room = (String) params[1];

		// perhaps there could be an impedance attribute be set
		if (params.length > 1)
			impedance = (String) params[1];

		// set loading screen
//		Looper.prepare();
//		DataModel.getInstance().getMapActivity()
//				.setProgressDialog(ProgressDialog.show(DataModel.getInstance().getMapActivity(), "", "Please wait... loading route"));

		// set output fields for result
		String[] outputFields = { com.uofc.roomfinder.android.util.Constants.QUERY_COL_FLR_ID };

		// set layer and build where clause for query
		String whereClause = Constants.QUERY_COL_RM_ID + "='" + room + "'" + " AND " + Constants.QUERY_COL_BLD_ID + "='"
				+ GisServerUtil.getBuildingAbbreviationForQuery(building) + "'";

		Query query = new Query();
		query.setGeometry(new Envelope(MIN_X_QUERY_COORDINATE, MIN_Y_QUERY_COORDINATE, MAX_X_QUERY_COORDINATE, MAX_Y_QUERY_COORDINATE));
		query.setOutSpatialReference(SpatialReference.create(SPARTIAL_REF_NAD83));
		query.setOutFields(outputFields);
		query.setReturnIdsOnly(false);
		query.setReturnGeometry(true);
		query.setWhere(whereClause);

		System.out.println(whereClause + " " + Constants.MAPSERVER_ROOM_QUERY_URL);

		QueryTask qTask = new QueryTask(Constants.MAPSERVER_ROOM_QUERY_URL);
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

		// TODO: remove in final version. url has to be accessed in an async thread
		try {
			Class<?> strictModeClass = Class.forName("android.os.StrictMode");
			Class<?> strictModeThreadPolicyClass = Class.forName("android.os.StrictMode$ThreadPolicy");
			Object laxPolicy = strictModeThreadPolicyClass.getField("LAX").get(null);
			Method method_setThreadPolicy = strictModeClass.getMethod("setThreadPolicy", strictModeThreadPolicyClass);
			method_setThreadPolicy.invoke(null, laxPolicy);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// System.out.println(result);
		// System.out.println(result.getGraphics().length);

		// if there is an result
		if (result != null && result.getGraphics() != null && result.getGraphics().length > 0) {
			Route route = null;

			// remove graphics from graphics layer
			DataModel.getInstance().getMapActivity().getMapView().getGraphicsLayer().removeAll();

			// display graphic
			Graphic[] grs = result.getGraphics();
			DataModel.getInstance().getMapActivity().getMapView().getGraphicsLayer().addGraphics(grs);

			// get Route vom NA server
			// Route route = new Route(this.mapActivity.getCurrentPosition(), new RoutePoint(centerPoint.getX(), centerPoint.getY()));
			Point startPoint = DataModel.getInstance().getCurrentPositionNAD83();
			Point endPoint = CoordinateUtil.getCenterCoordinateOfGeometry(grs[0].getGeometry());

			// get floor out of result feature set
			String floorResult = (String) result.getGraphics()[0].getAttributeValue(com.uofc.roomfinder.android.util.Constants.QUERY_COL_FLR_ID);

			// build destination point for route
			RoutePoint routeEnd = new RoutePoint(endPoint.getX(), endPoint.getY(), CoordinateUtil.getZCoordFromFloor(floorResult));

			System.out.println(routeEnd.getX() + ", " + routeEnd.getY());

			// if there is an impedance attribute pay attention to it...
			if (impedance != null)
				route = new Route(new RoutePoint(startPoint.getX(), startPoint.getY()), routeEnd, impedance);
			else
				route = new Route(new RoutePoint(startPoint.getX(), startPoint.getY()), routeEnd);

			// set destination to data model
			DataModel.getInstance().setDestinationPoint(routeEnd);
			DataModel.getInstance().setDestinationText("Route to " + building + " " + room);

			// System.out.println(startPoint.getX() + " - " + startPoint.getY());
			// System.out.println(endPoint.getX() + " - " + endPoint.getY());
			// System.out.println("result: " + result.getGraphics()[0].getAttributeValue(com.uofc.roomfinder.android.util.Constants.QUERY_COL_FLR_ID) );
			// System.out.println("result: " + result);

			// display route
			if (route.getPath().size() > 1) {

				try {
					// analyze route
					if (route.getRouteSegments().size() == 0) {
						if (!RouteAnalyzer.analyzeRoute(route)) {
							return;
						}
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
			} else {
				Toast toast = Toast.makeText(DataModel.getInstance().getMapActivity(), "no route could be found", Toast.LENGTH_LONG);
				toast.show();
			}
		} else {
			// try modify search params
			GisServerUtil.startRoomWithRouteQuery(building, Util.getOnlyNumerics(room), impedance);

			Toast toast = Toast.makeText(DataModel.getInstance().getMapActivity(), building + " " + room + " could not be found on map", Toast.LENGTH_LONG);
			toast.show();

		}
		// DataModel.getInstance().getMapActivity().getProgressDialog().dismiss();
		System.out.println("async thread completed");
	}
}
