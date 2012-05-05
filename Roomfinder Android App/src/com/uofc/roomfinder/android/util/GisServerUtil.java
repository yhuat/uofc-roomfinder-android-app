package com.uofc.roomfinder.android.util;

import com.uofc.roomfinder.android.util.tasks.RoomQuery;
import com.uofc.roomfinder.android.util.tasks.RoomWithRouteQuery;

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
	public static void startRoomWithRouteQuery(String building, String room, String impedance) {

		Object[] queryParams = { building, room, impedance };
		RoomWithRouteQuery asyncQuery = new RoomWithRouteQuery();
		asyncQuery.execute(queryParams);
	}

	/**
	 * starts an async task for querying a building
	 * 
	 * @param building
	 * @param room
	 * @param impedance
	 */
	public static void startRoomQuery(String building, String room) {

		Object[] queryParams = { building, room };
		RoomQuery asyncQuery = new RoomQuery();
		asyncQuery.execute(queryParams);
	}

}