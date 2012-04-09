package com.uofc.roomfinder.android.util.tasks;

import static com.uofc.roomfinder.android.util.Constants.MAX_X_QUERY_COORDINATE;
import static com.uofc.roomfinder.android.util.Constants.MAX_Y_QUERY_COORDINATE;
import static com.uofc.roomfinder.android.util.Constants.MIN_X_QUERY_COORDINATE;
import static com.uofc.roomfinder.android.util.Constants.MIN_Y_QUERY_COORDINATE;
import static com.uofc.roomfinder.android.util.Constants.SPARTIAL_REF_MAP;
import static com.uofc.roomfinder.android.util.Constants.SPARTIAL_REF_WGS84;

import java.lang.reflect.Method;

import android.graphics.Color;
import android.os.AsyncTask;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
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

		SpatialReference sr = SpatialReference.create(SPARTIAL_REF_MAP);
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

		String message = "No result comes back";

		// TODO: remove in final version. url has to be accessed in an async thread
		try {
			Class strictModeClass = Class.forName("android.os.StrictMode");
			Class strictModeThreadPolicyClass = Class.forName("android.os.StrictMode$ThreadPolicy");
			Object laxPolicy = strictModeThreadPolicyClass.getField("LAX").get(null);
			Method method_setThreadPolicy = strictModeClass.getMethod("setThreadPolicy", strictModeThreadPolicyClass);
			method_setThreadPolicy.invoke(null, laxPolicy);
		} catch (Exception e) {
			e.printStackTrace();	
		}
		
		//if there is an result
		if (result != null && result.getGraphics().length > 0) {
			Graphic[] grs = result.getGraphics();

			Geometry geo = CoordinateUtil.transformGeometryToWGS84(grs[0].getGeometry(), SpatialReference.create(SPARTIAL_REF_MAP));
			// Point centerPoint = CoordinateUtil.getCenterCoordinateOfGeometry(geo);
			Point centerPoint = CoordinateUtil.getCenterCoordinateOfGeometry(grs[0].getGeometry());
			System.out.println(centerPoint.getY() + ", " + centerPoint.getX());
			if (grs.length > 0) {
				mapActivity.getGraphicsLayer().addGraphics(grs);
				message = (grs.length == 1 ? "1 result has " : Integer.toString(grs.length) + " results have ") + "come back";
			}

			// get Route vom NA server
			// Route route = new Route(this.mapActivity.getCurrentPosition(), new RoutePoint(centerPoint.getX(), centerPoint.getY()));
			double START_X = 700326.68338;
			double START_Y = 5662241.3256;
			double END_X = 701586.12106;
			double END_Y = 5662819.3331;

			double ms_startX = 51.080126;
			double ms_startY = -114.127575;

			Point startPoint = centerPoint;
			// Point endPoint = CoordinateUtil.getCenterCoordinateOfGeometry(grs[0].getGeometry()); // new Point(END_X, END_Y, 0.0);
			Geometry geom = CoordinateUtil.transformGeometryToNAD83(new Point(ms_startY, ms_startX), SpatialReference.create(SPARTIAL_REF_WGS84)); // new
																																					// Point(END_X,
																																					// END_Y,
																																					// 0.0);
			Point endPoint = CoordinateUtil.getCenterCoordinateOfGeometry(geom);

			Route route = new Route(new RoutePoint(startPoint.getX(), startPoint.getY()), new RoutePoint(endPoint.getX(), endPoint.getY()));

			Point pa = null;
			Point pb = new Point(route.getPath().get(0).getX(), route.getPath().get(0).getY());
			Segment segment = new Line();
			Polyline pLine = new Polyline();
			Symbol lineSymbol = new SimpleLineSymbol(Color.BLUE, 4);

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
				int addedGraphicId = mapActivity.getGraphicsLayer().addGraphic(new Graphic(pLine, lineSymbol));

			} catch (Exception ex) {
				// log exception ex.getMessage()
				ex.printStackTrace();
			}

			System.out.println("path length: " + route.getPath().size());

		}

		System.out.println(message);
		// this.mapActivity.getProgressDialog().dismiss();

		// Toast toast = Toast.makeText(this.mapActivity, message, Toast.LENGTH_LONG);
		// toast.show();
	}

}
