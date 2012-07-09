package com.uofc.roomfinder.android.util.tasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.uofc.roomfinder.android.DataModel;
import com.uofc.roomfinder.android.util.Constants;
import com.uofc.roomfinder.entities.Point3D;
import com.uofc.roomfinder.entities.routing.Route;
import com.uofc.roomfinder.entities.routing.RouteStopAttributes;
import com.uofc.roomfinder.entities.routing.RouteStopFeature;
import com.uofc.roomfinder.util.UrlReader;
import com.uofc.roomfinder.util.gson.RouteJsonDeserializer;

public class RouteDownloader extends AsyncTask<Object, Void, String> {

	@Override
	protected void onPreExecute() {

	}

	@Override
	protected String doInBackground(Object... params) {
		if (params.length != 4)
			return null;

		Point3D start = (Point3D) params[0];
		Point3D destination = (Point3D) params[1];
		String impedanceAttribute = (String) params[2];
		start.setZ((Double) params[3]);

		System.out.println("z: " + start.getZ() + ", " + params[0] + ", " + params[1] + ", " + params[2]);

		Route route = new Route();

		// add stop features
		route.getStops().getFeatures().add(new RouteStopFeature(start, new RouteStopAttributes("Start", "unnamed")));
		route.getStops().getFeatures().add(new RouteStopFeature(destination, new RouteStopAttributes("Destination", "unnamed")));

		// System.out.println("route " + route + " impedance " + impedanceAttribute);
		// System.out.println(getJsonRouteFromServer(route, impedanceAttribute));

		return getJsonRouteFromServer(route, impedanceAttribute);
	}

	@Override
	protected void onPostExecute(String jsonResponse) {

		// result found
		if (jsonResponse != null) {
			// deserialze JSON String
			Gson gson = new GsonBuilder().registerTypeAdapter(Route.class, new RouteJsonDeserializer()).serializeNulls().create();
			Route route = gson.fromJson(jsonResponse, Route.class);

			// if one route has come back -> start analyzing it
			if (route.getPath().size() > 1) {
				new RouteAnalyzer().execute(route);

			} else {
				Toast toast = Toast.makeText(DataModel.getInstance().getMapActivity(), "no route could be found", Toast.LENGTH_LONG);
				toast.show();
			}

		} else {
			Toast.makeText(DataModel.getInstance().getMapActivity(), "error on downloading route from server", Toast.LENGTH_LONG).show();
		}

	}

	/**
	 * helper method to get route from NAServer in JSON format
	 * 
	 * @return JSON route
	 */
	private static String getJsonRouteFromServer(Route route, String impedance) {

		String urlToRequest = Constants.ROOMFINDER_SERVER_URL + "/route?x1=" + route.getStops().getFeatures().get(0).getGeometry().getX() + "&y1="
				+ route.getStops().getFeatures().get(0).getGeometry().getY() + "&z1=" + route.getStops().getFeatures().get(0).getGeometry().getZ() + "&x2="
				+ route.getStops().getFeatures().get(1).getGeometry().getX() + "&y2=" + route.getStops().getFeatures().get(1).getGeometry().getY() + "&z2="
				+ route.getStops().getFeatures().get(1).getGeometry().getZ() + "&impedance=" + impedance;

		System.out.println(urlToRequest);

		String result = UrlReader.readFromURL(urlToRequest);

		return result;
	}
}
