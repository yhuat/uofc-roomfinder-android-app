package com.uofc.roomfinder.android.util;

/**
 * collected Constants for ArcGIS map use
 */
public class Constants {

	// old URLs
	// public final static String GIS_MAPSERVER_URL = "http://asebeast2.cpsc.ucalgary.ca:7000/ArcGIS/rest/services/RoomFinder/MapServer";
	// public final static int GIS_LAYER_LINES = 0;
	// public final static int GIS_LAYER_ROOMS = 1;

	// services hosted by the UofC
	public final static String UOFC_ARCGIS_SERVER_URL = "http://136.159.24.32/ArcGIS/rest/services";
	public final static String MAPSERVER_BUILDINGS_URL = UOFC_ARCGIS_SERVER_URL + "/Buildings/MapServer";
	public final static String MAPSERVER_ROOM_QUERY_URL = UOFC_ARCGIS_SERVER_URL + "/Rooms/Rooms/MapServer/111";

	public final static String QUERY_COL_RM_ID = "SDE.DBO.Building_Room.RM_ID";
	public final static String QUERY_COL_BLD_ID = "SDE.DBO.Building_Room.BLD_ID";
	public final static String QUERY_COL_FLR_ID = "SDE.DBO.Building_Room.FLR_ID";

	public final static String ROUTING_IMPEDANCE_SHORTEST_PATH = "Length";
	public final static String ROUTING_IMPEDANCE_AVOID_OUTDOOR = "OutdoorCost";
	public final static String ROUTING_IMPEDANCE_AVOID_INDOOR = "IndoorCost";
	public final static String ROUTING_IMPEDANCE_AVOID_STAIRS = "StairCost";

	// http://136.159.24.32/ArcGIS/rest/services

	// services hosted on the roomfinder server
	public final static String ROOMFINDER_SERVER_URL = "http://ec2-23-20-196-109.compute-1.amazonaws.com:8080/UofC_Roomfinder_Server/rest";
	public final static String REST_ANNOTATION_BUILDINGS_URL = ROOMFINDER_SERVER_URL + "/annotation/cat/buildings";
	public final static String REST_CONTACTS_URL = ROOMFINDER_SERVER_URL + "/contact/name/";

	public final static int SPARTIAL_REF_NAD83 = 26911;
	public final static int SPARTIAL_REF_WGS84 = 4326;

	// initial extent for room map server data
	public static final double MAX_X_QUERY_COORDINATE = 637778.954772949;
	public static final double MAX_Y_QUERY_COORDINATE = 5638651.80877686;
	public static final double MIN_X_QUERY_COORDINATE = 701643.474182129;
	public static final double MIN_Y_QUERY_COORDINATE = 5671355.65698242;

	// activity results (for start as result)
	public final static int SEARCH_ROOM = 1;
	public final static int SEARCH_ROOM_WITH_ROUTE = 2;
	public final static int QUICKLINKS = 3;
	
	//map drawing
	public static final double SEGMENT_ZOOM_FACTOR = 2.2;
	public static final double ROOM_ZOOM_FACTOR = 12.0;
	

}
