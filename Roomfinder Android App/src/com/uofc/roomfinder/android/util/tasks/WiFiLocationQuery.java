package com.uofc.roomfinder.android.util.tasks;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.uofc.roomfinder.android.DataModel;
import com.uofc.roomfinder.android.util.Constants;
import com.uofc.roomfinder.entities.Point3D;
import com.uofc.roomfinder.util.UrlReader;

public class WiFiLocationQuery extends AsyncTask<String, Void, String> {

	String macAddresses = null;
	String rssi = null;
	String frequencies = null;

	@Override
	protected void onPreExecute() {
	}

	/**
	 * sends wifi access point information to server server triangulates position of device and returns the Location
	 * 
	 * @param queryParams
	 *            elements: 1: macs of access points, 2: RSSI of access points
	 * @return resultSet of query
	 */
	@Override
	protected String doInBackground(String... params) {

		// exit condition
		if (params == null || params[1] == null || params.length < 3)
			return null;

		macAddresses = params[0];
		rssi = params[1];
		frequencies = params[2];

		String queryUrl = Constants.REST_WIFI_LOCATION_URL + "?macAddresses=" + macAddresses + "&powerLevels=" + rssi + "&frequencies=" + frequencies;

		System.out.println("locationquery: " + queryUrl);

		return UrlReader.readFromURL(queryUrl);
	}

	@Override
	protected void onPostExecute(String result) {
		// if there is an result
		if (result != null) {
			Gson gson = new GsonBuilder().serializeNulls().create();
			Point3D pt = gson.fromJson(result, Point3D.class);

			DataModel.getInstance().setWifiPosition(new Point3D(pt.getX(),pt.getY(),pt.getZ()));
			System.out.println(pt.getY() + "," + pt.getX() + " " + pt.getZ());
		}
	}
}
