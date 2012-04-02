package com.uofc.roomfinder.android.util;

/**
 * collected Constants for ArcGIS map use
 */
public class Constants {

	public final static String GIS_MAPSERVER_URL = "http://asebeast2.cpsc.ucalgary.ca:7000/ArcGIS/rest/services/RoomFinder/MapServer";
	public final static String GIS_MAPSERVER_BUILDINGS_URL = "http://136.159.24.32/ArcGIS/rest/services/Buildings/MapServer";

	public final static int GIS_LAYER_LINES = 0;
	public final static int GIS_LAYER_ROOMS = 1;

	public final static int SPARTIAL_REF_MAP = 26911;
	public final static int SPARTIAL_REF_WGS84 = 4326;
	

	// initial extent for room map server data
	public static final double MAX_X_QUERY_COORDINATE = 701235.973828125;
	public static final double MAX_Y_QUERY_COORDINATE = 5662703.85075684;
	public static final double MIN_X_QUERY_COORDINATE = 700913.603198242;
	public static final double MIN_Y_QUERY_COORDINATE = 5662605.69160156;

}
