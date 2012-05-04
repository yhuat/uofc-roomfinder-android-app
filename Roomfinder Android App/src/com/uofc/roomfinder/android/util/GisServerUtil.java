package com.uofc.roomfinder.android.util;

import com.uofc.roomfinder.android.util.tasks.RouteQuery;

public class GisServerUtil {

	/**
	 * converts the known abbreviation into the correct abbreviation for a query
	 * 
	 * @param buildingAbbreviation
	 * @return
	 */
	public static String getBuildingAbbreviationForQuery(String buildingAbbreviation) {
		String result = buildingAbbreviation;

		if (buildingAbbreviation.equals("KNA")) {
			result = "KN";
		}
		return result;
	}

	/**
	 * starts an async task for querying the route
	 * 
	 * @param building
	 * @param room
	 * @param impedance
	 */
	public static void startRouteQuery(String building, String room, String impedance) {
		// Log.e("MapScreen", "searching room: " + room + " and building: " + building + "and impedance: " + impedance);

		// start query task
		Object[] queryParams = { building, room, impedance };
		RouteQuery asyncQuery = new RouteQuery();
		asyncQuery.execute(queryParams);
	}

}