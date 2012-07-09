package com.uofc.roomfinder.android.util;

import android.graphics.Color;

/**
 * Constants for ArcGIS map use
 */
public class Constants {

	// GIS hosted by the UofC (ask Tom McCaffrey)
	// ======================
	public final static String UOFC_ARCGIS_SERVER_URL = "http://136.159.24.32/ArcGIS/rest/services";
	public final static String MAPSERVER_BUILDINGS_URL = UOFC_ARCGIS_SERVER_URL + "/Buildings/MapServer";
	public final static String MAPSERVER_ROOMS_URL = UOFC_ARCGIS_SERVER_URL + "/Rooms/Rooms/MapServer";
	public final static String MAPSERVER_AERIAL_URL = UOFC_ARCGIS_SERVER_URL + "/Imagery/TrueOrtho2011b_cached/MapServer";
	public final static String MAPSERVER_ROOM_QUERY_URL = UOFC_ARCGIS_SERVER_URL + "/Rooms/Rooms/MapServer/111";

	public final static String QUERY_ROOM_COL_RM_ID = "SDE.DBO.Building_Room.RM_ID";
	public final static String QUERY_ROOM_COL_BLD_ID = "SDE.DBO.Building_Room.BLD_ID";
	public final static String QUERY_ROOM_COL_FLR_ID = "SDE.DBO.Building_Room.FLR_ID";

	public final static String QUERY_BUILDING_COL_ID = "SDE.DBO.Building_Info.BLDG_ID";
	public final static String QUERY_BUILDING_COL_NAME = "SDE.DBO.Building_Info.BLDG_NAME";

	public final static String ROUTING_IMPEDANCE_SHORTEST_PATH = "Length";
	public final static String ROUTING_IMPEDANCE_AVOID_OUTDOOR = "OutdoorCost";
	public final static String ROUTING_IMPEDANCE_AVOID_INDOOR = "IndoorCost";
	public final static String ROUTING_IMPEDANCE_AVOID_STAIRS = "StairCost";

	// services hosted on the roomfinder server
	// ========================================
	// REST URLS
	public final static String ROOMFINDER_SERVER_URL = "http://ec2-23-20-196-109.compute-1.amazonaws.com:8080/UofC_Roomfinder_Server/rest";
	// public final static String ROOMFINDER_SERVER_URL = "http://192.168.1.102:8080/UofC_Roomfinder_Server/rest";

	// REST interfaces
	public final static String REST_ANNOTATION_BUILDINGS_URL = ROOMFINDER_SERVER_URL + "/annotation/cat/buildings";
	public final static String REST_ANNOTATION_NAVIGATION_URL = ROOMFINDER_SERVER_URL + "/annotation/navigation";
	public final static String REST_CONTACTS_URL = ROOMFINDER_SERVER_URL + "/contact/name/";
	public final static String REST_WIFI_LOCATION_URL = ROOMFINDER_SERVER_URL + "/location";
	public final static String REST_ADD_FRIEND_URL = ROOMFINDER_SERVER_URL + "/friend/add";

	// GIS settings
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

	// map drawing
	public static final double SEGMENT_ZOOM_FACTOR = 2.2;
	public static final double ROOM_ZOOM_FACTOR = 12.0;
	
	public static final int MAP_UPDATE_INTERVAL = 2500;
	public static final int COLOR_GPS_MARKER = Color.BLUE;
	public static final int COLOR_WIFI_MARKER = Color.YELLOW;
	
	public enum LocationProvider {
		GPS, WIFI;
	}

}
