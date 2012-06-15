package com.uofc.roomfinder.android.util.tasks;

import android.os.AsyncTask;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.uofc.roomfinder.android.DataModel;
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
		if (params.length != 3)
			return null;

		Point3D start = (Point3D) params[0];
		Point3D destination = (Point3D) params[1];
		String impedanceAttribute = (String) params[2];

		Route route = new Route();

		// add stop features
		route.getStops().getFeatures().add(new RouteStopFeature(start, new RouteStopAttributes("Start", "unnamed")));
		route.getStops().getFeatures().add(new RouteStopFeature(destination, new RouteStopAttributes("Destination", "unnamed")));

		System.out.println(getJsonRouteFromServer(route, impedanceAttribute));
		
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

		/*
		 * old REST service (ARCGIS ROUTING REST SERVICE) is not able to process 3D coordinates
		 * 
		 * 
		 * String returnFormat = "pjson";
		 * 
		 * String solve_url = "/solve?" + "barriers=&" + "polylineBarriers=&" + "polygonBarriers=&" + "outSR=26911&" + "ignoreInvalidLocations=true&" +
		 * "accumulateAttributeNames=&" + "restrictionAttributeNames=RestrictedPath&" + "attributeParameterValues=&" + "restrictUTurns=esriNFSBAllowBacktrack&"
		 * + "useHierarchy=false&" + "returnDirections=true&" + "returnRoutes=true&" + "returnStops=false&" + "returnBarriers=false&" +
		 * "returnPolylineBarriers=false&" + "returnPolygonBarriers=false&" + "directionsLanguage=en-US&" + "directionsStyleName=NA+Desktop&" +
		 * "outputLines=esriNAOutputLineTrueShape&" + "findBestSequence=false&" + "preserveFirstStop=true&" + "preserveLastStop=true&" + "useTimeWindows=false&"
		 * + "startTime=&" + "outputGeometryPrecision=&" + "outputGeometryPrecisionUnits=esriMeters&" + "directionsTimeAttributeName=IndoorCost&" +
		 * "directionsLengthUnits=esriNAUMeters" + "&f=" + returnFormat + "&impedanceAttributeName=" + impedance;
		 * 
		 * String urlToRequest = Constants.NA_SERVER_URL + solve_url + "&stops=" + this.getStopsAsJsonString();
		 * 
		 * System.out.println(urlToRequest);
		 */

		// http://192.168.1.106:8080
		
		//http://ec2-23-20-196-109.compute-1.amazonaws.com:8080/UofC_Roomfinder_Server
		String url_server = "http://10.11.27.58:8080/UofC_Roomfinder_Server";

		String urlToRequest = url_server + "/rest/route?x1="
				+ route.getStops().getFeatures().get(0).getGeometry().getX() + "&y1=" + route.getStops().getFeatures().get(0).getGeometry().getY() + "&z1="
				+ route.getStops().getFeatures().get(0).getGeometry().getZ() + "&x2=" + route.getStops().getFeatures().get(1).getGeometry().getX() + "&y2="
				+ route.getStops().getFeatures().get(1).getGeometry().getY() + "&z2=" + route.getStops().getFeatures().get(1).getGeometry().getZ()
				+ "&impedance=" + impedance;
		
		System.out.println(urlToRequest);
		
		String result = UrlReader.readFromURL(urlToRequest);

		return result;
	}
}
