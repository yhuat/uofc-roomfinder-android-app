package com.uofc.roomfinder.android.util.tasks;

import static com.uofc.roomfinder.android.util.Constants.MAX_X_QUERY_COORDINATE;
import static com.uofc.roomfinder.android.util.Constants.MAX_Y_QUERY_COORDINATE;
import static com.uofc.roomfinder.android.util.Constants.MIN_X_QUERY_COORDINATE;
import static com.uofc.roomfinder.android.util.Constants.MIN_Y_QUERY_COORDINATE;
import static com.uofc.roomfinder.android.util.Constants.SPARTIAL_REF_NAD83;

import java.lang.reflect.Method;

import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.ags.query.QueryTask;
import com.uofc.roomfinder.android.DataModel;
import com.uofc.roomfinder.android.activities.MapActivity;
import com.uofc.roomfinder.android.map.MapDrawer;
import com.uofc.roomfinder.android.util.CoordinateUtil;
import com.uofc.roomfinder.entities.routing.Route;
import com.uofc.roomfinder.entities.routing.RoutePoint;
import com.uofc.roomfinder.util.Constants;

public class RoomQuery extends AsyncTask<Object, Void, FeatureSet> {

	final static int HAS_RESULTS = 1;
	final static int NO_RESULT = 2;
	final static int CLEAR_RESULT = 3;

	private MapActivity mapActivity = null;
	private String impedanceAttribute = null;

	@Override
	protected void onPreExecute() {

	}

	/**
	 * performs a search on the map server
	 * 
	 * @param mapActivity
	 *            the map Activity from which this Query is launched
	 * @param queryParams
	 *            first element of array: query URL <br/>
	 *            second element: whereClause 3rd: mapActivity
	 * @return resultSet of query
	 */
	@Override
	protected FeatureSet doInBackground(Object... params) {
		if (params == null || params.length <= 2 || params[2] == null)
			return null;

		String queryUrl = (String) params[0];
		String whereClause = (String) params[1];
		this.mapActivity = (MapActivity) params[2];

		// perhaps there could be an impedance attribute be set
		if (params.length > 2)
			impedanceAttribute = (String) params[3];

		// set loading screen
		// this.mapActivity.setProgressDialog(ProgressDialog.show(this.mapActivity, "", "Please wait....query task is executing"));

		// set output fields for result
		String[] outputFields = { com.uofc.roomfinder.android.util.Constants.QUERY_COL_FLR_ID };

		Query query = new Query();
		query.setGeometry(new Envelope(MIN_X_QUERY_COORDINATE, MIN_Y_QUERY_COORDINATE, MAX_X_QUERY_COORDINATE, MAX_Y_QUERY_COORDINATE));
		query.setOutSpatialReference(SpatialReference.create(SPARTIAL_REF_NAD83));
		query.setOutFields(outputFields);
		query.setReturnIdsOnly(false);
		query.setReturnGeometry(true);
		query.setWhere(whereClause);

		System.out.println(whereClause + " " + queryUrl);

		QueryTask qTask = new QueryTask(queryUrl);
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

		//System.out.println(result);
		//System.out.println(result.getGraphics().length);

		// if there is an result
		if (result != null && result.getGraphics() != null && result.getGraphics().length > 0) {

			Route route = null;

			// remove graphics from graphicslayer
			mapActivity.getGraphicsLayer().removeAll();

			// display graphic
			Graphic[] grs = result.getGraphics();
			mapActivity.getGraphicsLayer().addGraphics(grs);

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
			if (impedanceAttribute != null)
				route = new Route(new RoutePoint(startPoint.getX(), startPoint.getY()), routeEnd, impedanceAttribute);
			else
				route = new Route(new RoutePoint(startPoint.getX(), startPoint.getY()), routeEnd);

			//set destination to data model
			DataModel.getInstance().setDestinationPoint(routeEnd);
			
			// System.out.println(startPoint.getX() + " - " + startPoint.getY());
			// System.out.println(endPoint.getX() + " - " + endPoint.getY());
			// System.out.println("result: " + result.getGraphics()[0].getAttributeValue(com.uofc.roomfinder.android.util.Constants.QUERY_COL_FLR_ID) );
			// System.out.println("result: " + result);

			DataModel.getInstance().setRoute(route);
			DataModel.getInstance().getMap().getBtnMinus().setVisibility(View.VISIBLE);
			DataModel.getInstance().getMap().getBtnPlus().setVisibility(View.VISIBLE);

			// display route
			if (route.getPath().size() > 1) {
				MapDrawer.displayRoute(route);
			} else {
				Toast toast = Toast.makeText(this.mapActivity, "no route could be found", Toast.LENGTH_LONG);
				toast.show();
			}

		} else {
			Toast toast = Toast.makeText(this.mapActivity, "building could not be found on map", Toast.LENGTH_LONG);
			toast.show();
		}
		// this.mapActivity.getProgressDialog().dismiss();
	}

}
