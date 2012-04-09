package com.uofc.roomfinder.android.util;

/**
 * collected Constants for ArcGIS map use
 */
public class Constants {

	//old URLs
	//public final static String GIS_MAPSERVER_URL = "http://asebeast2.cpsc.ucalgary.ca:7000/ArcGIS/rest/services/RoomFinder/MapServer";
	//public final static int GIS_LAYER_LINES = 0;
	//public final static int GIS_LAYER_ROOMS = 1;
	
	
	//services hosted by the UofC
	public final static String UOFC_ARCGIS_SERVER_URL = "http://136.159.24.32/ArcGIS/rest/services";
	public final static String MAPSERVER_BUILDINGS_URL = UOFC_ARCGIS_SERVER_URL + "/Buildings/MapServer";
	public final static String MAPSERVER_ROOM_QUERY_URL = UOFC_ARCGIS_SERVER_URL + "/Rooms/Rooms/MapServer/111";
	
	
	//http://136.159.24.32/ArcGIS/rest/services
	
	//services hostes on the roomfinder server
	public final static String ROOMFINDER_SERVER_URL = "http://ec2-23-20-196-109.compute-1.amazonaws.com:8080/UofC_Roomfinder_Server/rest";
	public final static String REST_ANNOTATION_BUILDINGS_URL = ROOMFINDER_SERVER_URL + "/annotation/cat/buildings";
	public final static String REST_CONTACTS_URL = ROOMFINDER_SERVER_URL + "/contact/name/";


	public final static int SPARTIAL_REF_MAP = 26911;
	public final static int SPARTIAL_REF_WGS84 = 4326;
	

	// initial extent for room map server data
	public static final double MAX_X_QUERY_COORDINATE = 701235.973828125;
	public static final double MAX_Y_QUERY_COORDINATE = 5662703.85075684;
	public static final double MIN_X_QUERY_COORDINATE = 700913.603198242;
	public static final double MIN_Y_QUERY_COORDINATE = 5662605.69160156;

}
