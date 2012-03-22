package com.uofc.roomfinder.android.map;

import static com.uofc.roomfinder.android.map.Constants.MAX_X_QUERY_COORDINATE;
import static com.uofc.roomfinder.android.map.Constants.MAX_Y_QUERY_COORDINATE;
import static com.uofc.roomfinder.android.map.Constants.MIN_X_QUERY_COORDINATE;
import static com.uofc.roomfinder.android.map.Constants.MIN_Y_QUERY_COORDINATE;
import static com.uofc.roomfinder.android.map.Constants.SPARTIAL_REF;
import android.os.AsyncTask;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.ags.query.QueryTask;
import com.uofc.roomfinder.android.activities.MapActivity;

public class AsyncMapQueryTask extends AsyncTask<Object, Void, FeatureSet> {

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
	 *            second element: whereClause
	 * @return resultSet of query
	 */
	@Override
	protected FeatureSet doInBackground(Object... params) {
		if (params == null || params.length <= 2 || params[2] == null)
			return null;

		String queryUrl = (String) params[0];
		String whereClause = (String) params[1];
		this.mapActivity = (MapActivity) params[2];
		
		//set loading screen
		//this.mapActivity.setProgressDialog(ProgressDialog.show(this.mapActivity, "", "Please wait....query task is executing"));

		SpatialReference sr = SpatialReference.create(SPARTIAL_REF);
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

		if (result != null) {
			Graphic[] grs = result.getGraphics();

			if (grs.length > 0) {
				mapActivity.getGraphicsLayer().addGraphics(grs);
				message = (grs.length == 1 ? "1 result has " : Integer.toString(grs.length) + " results have ") + "come back";
			}

		}
		
		System.out.println(message);
		//this.mapActivity.getProgressDialog().dismiss();

		//Toast toast = Toast.makeText(this.mapActivity, message, Toast.LENGTH_LONG);
		//toast.show();
	}


}
