package com.uofc.roomfinder.android.util.tasks;

import static com.uofc.roomfinder.android.util.Constants.MAX_X_QUERY_COORDINATE;
import static com.uofc.roomfinder.android.util.Constants.MAX_Y_QUERY_COORDINATE;
import static com.uofc.roomfinder.android.util.Constants.MIN_X_QUERY_COORDINATE;
import static com.uofc.roomfinder.android.util.Constants.MIN_Y_QUERY_COORDINATE;
import static com.uofc.roomfinder.android.util.Constants.SPARTIAL_REF_NAD83;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.FeatureSet;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.ags.query.QueryTask;
import com.uofc.roomfinder.android.DataModel;
import com.uofc.roomfinder.android.util.Constants;
import com.uofc.roomfinder.android.util.GisServerUtil;
import com.uofc.roomfinder.util.Util;

public class RoomQuery extends AsyncTask<Object, Void, FeatureSet> {

	private String impedance = null;
	private String building;
	private String room;

	ProgressDialog dialog;

	@Override
	protected void onPreExecute() {
		dialog = ProgressDialog.show(DataModel.getInstance().getMapActivity(), "loading", "calculating route...");
	}

	/**
	 * performs a search on the map server
	 * 
	 * @param queryParams
	 *            elements: 1: building, 2: room, 3: impedance (optional, if set -> display route)
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
		if (params.length > 2)
			impedance = (String) params[2];

		// set output fields for result
		// we need the floor id for calculating the z coordinate
		String[] outputFields = { Constants.QUERY_ROOM_COL_FLR_ID };

		// set layer and build where clause for query
		String whereClause = Constants.QUERY_ROOM_COL_RM_ID + "='" + room + "'" + " AND " + Constants.QUERY_ROOM_COL_BLD_ID + "='"
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

		// if there is an result
		if (result != null && result.getGraphics() != null && result.getGraphics().length > 0) {

			if (impedance == null) {
				// display only the building shape
				GisServerUtil.createBuildingShape(result, building, room);
			} else {
				// create a route, analyze and then display it
				System.out.println(impedance);
				GisServerUtil.createRoute(result, building, room, impedance);
			}

		} else {
			// try modify search params
			GisServerUtil.startRoomWithRouteQuery(building, Util.getOnlyNumerics(room), impedance);

			Toast toast = Toast.makeText(DataModel.getInstance().getMapActivity(), building + " " + room + " could not be found on map", Toast.LENGTH_LONG);
			toast.show();

		}
		dialog.dismiss();
	}

}
