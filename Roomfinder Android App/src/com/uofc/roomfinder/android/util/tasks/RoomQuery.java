package com.uofc.roomfinder.android.util.tasks;

import static com.uofc.roomfinder.android.util.Constants.MAX_X_QUERY_COORDINATE;
import static com.uofc.roomfinder.android.util.Constants.MAX_Y_QUERY_COORDINATE;
import static com.uofc.roomfinder.android.util.Constants.MIN_X_QUERY_COORDINATE;
import static com.uofc.roomfinder.android.util.Constants.MIN_Y_QUERY_COORDINATE;
import static com.uofc.roomfinder.android.util.Constants.SPARTIAL_REF_NAD83;

import java.lang.reflect.Method;

import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.Toast;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Line;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.Segment;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.Symbol;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.ags.query.QueryTask;
import com.uofc.roomfinder.android.DataModel;
import com.uofc.roomfinder.android.activities.MapActivity;
import com.uofc.roomfinder.android.util.CoordinateUtil;
import com.uofc.roomfinder.entities.routing.Route;
import com.uofc.roomfinder.entities.routing.RoutePoint;

public class RoomQuery extends AsyncTask<Object, Void, FeatureSet> {

	final static int HAS_RESULTS = 1;
	final static int NO_RESULT = 2;
	final static int CLEAR_RESULT = 3;

	private MapActivity mapActivity = null;

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

		// set loading screen
		// this.mapActivity.setProgressDialog(ProgressDialog.show(this.mapActivity, "", "Please wait....query task is executing"));

		SpatialReference sr = SpatialReference.create(SPARTIAL_REF_NAD83);
		Query query = new Query();
		query.setGeometry(new Envelope(MIN_X_QUERY_COORDINATE, MIN_Y_QUERY_COORDINATE, MAX_X_QUERY_COORDINATE, MAX_Y_QUERY_COORDINATE));
		query.setOutSpatialReference(sr);
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

		System.out.println(result);
		System.out.println(result.getGraphics().length);
		
		// if there is an result
		if (result != null && result.getGraphics() != null && result.getGraphics().length > 0) {

			//remove graphics from graphicslayer
			mapActivity.getGraphicsLayer().removeAll();
			
			// display graphic
			Graphic[] grs = result.getGraphics();
			mapActivity.getGraphicsLayer().addGraphics(grs);

			// get Route vom NA server
			// Route route = new Route(this.mapActivity.getCurrentPosition(), new RoutePoint(centerPoint.getX(), centerPoint.getY()));
			Point startPoint = DataModel.getInstance().getCurrentPositionNAD83();
			Point endPoint = CoordinateUtil.getCenterCoordinateOfGeometry(grs[0].getGeometry());
			System.out.println(startPoint.getX() + " - " + startPoint.getY());
			System.out.println(endPoint.getX() + " - " + endPoint.getY());
			Route route = new Route(new RoutePoint(startPoint.getX(), startPoint.getY()), new RoutePoint(endPoint.getX(), endPoint.getY()));

			if (route.getPath().size() > 1) {
				// create poly line
				Polyline pLine = new Polyline();
				Symbol lineSymbol = new SimpleLineSymbol(Color.BLUE, 4);

				// add each segment of route to a poly line
				Point pa = null;
				Point pb = new Point(route.getPath().get(0).getX(), route.getPath().get(0).getY());
				Segment segment = new Line();
				try {
					for (int i = 1; i < route.getPath().size(); i++) {
						// get points from path
						pa = pb;
						pb = new Point(route.getPath().get(i).getX(), route.getPath().get(i).getY());

						// set segment
						segment.setStart(pa);
						segment.setEnd(pb);

						// add segment to polyLine
						pLine.addSegment(segment, true);
					}
					// add created line to graphics layer
					mapActivity.getGraphicsLayer().addGraphic(new Graphic(pLine, lineSymbol));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}else{
				Toast toast = Toast.makeText(this.mapActivity, "no route could be found", Toast.LENGTH_LONG);
				toast.show();
			}
		}else{
			Toast toast = Toast.makeText(this.mapActivity, "building could not be found on map", Toast.LENGTH_LONG);
			toast.show();
		}
		// this.mapActivity.getProgressDialog().dismiss();
	}

}
