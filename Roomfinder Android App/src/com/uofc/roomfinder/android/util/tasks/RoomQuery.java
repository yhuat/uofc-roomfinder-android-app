package com.uofc.roomfinder.android.util.tasks;

import static com.uofc.roomfinder.android.util.Constants.MAX_X_QUERY_COORDINATE;
import static com.uofc.roomfinder.android.util.Constants.MAX_Y_QUERY_COORDINATE;
import static com.uofc.roomfinder.android.util.Constants.MIN_X_QUERY_COORDINATE;
import static com.uofc.roomfinder.android.util.Constants.MIN_Y_QUERY_COORDINATE;
import static com.uofc.roomfinder.android.util.Constants.SPARTIAL_REF_NAD83;

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
import com.uofc.roomfinder.android.util.Constants;
import com.uofc.roomfinder.android.util.CoordinateUtil;
import com.uofc.roomfinder.android.util.GisServerUtil;
import com.uofc.roomfinder.entities.routing.RoutePoint;
import com.uofc.roomfinder.util.Util;

public class RoomQuery extends AsyncTask<Object, Void, FeatureSet> {
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
	 *            elements: 1: building, 2: room
	 * @return resultSet of query
	 */
	@Override
	protected FeatureSet doInBackground(Object... params) {

		// exit condition
		if (params == null || params.length < 2 || params[1] == null)
			return null;

		building = (String) params[0];
		room = (String) params[1];

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

		// if there is an result
		if (result != null && result.getGraphics() != null && result.getGraphics().length > 0) {

			// remove graphics from graphics layer
			DataModel.getInstance().getMapActivity().getMapView().getGraphicsLayer().removeAll();

			// display graphic
			Graphic[] grs = result.getGraphics();
			DataModel.getInstance().getMapActivity().getMapView().getGraphicsLayer().addGraphics(grs);

			// zoom to room
			// create envelope which is a bit bigger than the route segment
			Envelope env = new Envelope();
			result.getGraphics()[0].getGeometry().queryEnvelope(env);		
			Envelope newEnv = new Envelope(env.getCenter(), env.getWidth() * Constants.ROOM_ZOOM_FACTOR, env.getHeight() * Constants.ROOM_ZOOM_FACTOR);
			DataModel.getInstance().getMapActivity().getMapView().setExtent(newEnv);

			// destination point
			Point endPoint = CoordinateUtil.getCenterCoordinateOfGeometry(grs[0].getGeometry());

			// build destination point
			String floorResult = (String) result.getGraphics()[0].getAttributeValue(com.uofc.roomfinder.android.util.Constants.QUERY_COL_FLR_ID);
			RoutePoint routeEnd = new RoutePoint(endPoint.getX(), endPoint.getY(), CoordinateUtil.getZCoordFromFloor(floorResult));
			
			//set layer to destination point
			DataModel.getInstance().getMapActivity().getMapView().setActiveHeight(routeEnd);

			// set destination to data model
			DataModel.getInstance().setDestinationPoint(routeEnd);

			//display result as an info box on map view
			DataModel.getInstance().getMapActivity().displayInfoBox(building + " " + room + "\n" + DataModel.getInstance().getDestinationText());

		} else {

			// try modify search params
			GisServerUtil.startRoomQuery(building, Util.getOnlyNumerics(room));

			Toast toast = Toast.makeText(DataModel.getInstance().getMapActivity(), building + " " + room + " could not be found on map", Toast.LENGTH_LONG);
			toast.show();

		}
	}
}
