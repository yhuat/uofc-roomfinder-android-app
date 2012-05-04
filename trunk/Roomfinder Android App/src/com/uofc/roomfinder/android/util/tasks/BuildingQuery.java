package com.uofc.roomfinder.android.util.tasks;

import static com.uofc.roomfinder.android.util.Constants.SPARTIAL_REF_NAD83;

import java.lang.reflect.Method;

import android.os.AsyncTask;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.ags.query.QueryTask;
import com.uofc.roomfinder.entities.routing.Route;

public class BuildingQuery extends AsyncTask<Object, Void, FeatureSet> {

	private final static String BUILDING_QUERY_URL = "http://136.159.24.32/ArcGIS/rest/services/Buildings/MapServer/0";
	
	@Override
	protected void onPreExecute() {

	}

	/**
	 * performs a search on the map server
	 * 
	 * @param mapActivity
	 *            the map Activity from which this Query is launched
	 * @param queryParams
	 *            first element of array: queryEnvelope <br/>
	 * @return returns every building in the query envelope
	 */
	@Override
	protected FeatureSet doInBackground(Object... params) {
		if (params == null || params.length < 1)
			return null;

		Envelope queryEnvelope = (Envelope) params[0];

		Query query = new Query();
		query.setGeometry(queryEnvelope);
		query.setOutSpatialReference(SpatialReference.create(SPARTIAL_REF_NAD83));
		query.setReturnIdsOnly(false);
		query.setReturnGeometry(true);
		//query.setWhere(whereClause);

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
		//System.out.println(result.getGraphics().length);

		// if there is an result
		if (result != null && result.getGraphics() != null && result.getGraphics().length > 0) {

			Route route = null;

			Graphic[] grs = result.getGraphics();

			// System.out.println("result: " + result.getGraphics()[0].getAttributeValue(com.uofc.roomfinder.android.util.Constants.QUERY_COL_FLR_ID) );
			// System.out.println("result: " + result);

		
		} else {
		}
	}

}
