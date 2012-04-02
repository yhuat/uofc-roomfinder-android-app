package com.uofc.roomfinder.android.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.ags.query.QueryTask;
import com.uofc.roomfinder.android.activities.MapActivity;
import com.uofc.roomfinder.android.util.CoordinateUtil;
import com.esri.core.geometry.Point;

/**
 * 
 * @author lauteb
 */
public class BuildingDAOImpl {

	final static String BUILDING_SERVER_URL = "http://136.159.24.32/ArcGIS/rest/services/Buildings/MapServer";
	final static String BUILDING_QUERY_LAYER = "0";
	final static String BUILDING_ID_COLUMN_NAME = "SDE.DBO.Building_Info.BLDG_ID";
	
	final static double BUILDING_X_MIN = 634576.26786499;
	final static double BUILDING_Y_MIN = 5637016.59738464;
	final static double BUILDING_X_MAX = 704983.367510986;
	final static double BUILDING_Y_MAX = 5672991.24801331;
	final static int BUILDING_SPATIAL_REF = 26911;
	
	public static int updateBuildingTable() {

		//build URL & query string
		//String queryUrl = BUILDING_SERVER_URL + "/" + BUILDING_QUERY_LAYER;
		//String whereClause = 	BUILDING_ID_COLUMN_NAME + " like '%'";
		//String whereClause = 	"SDE.DBO.Building_Info.BLDG_ID='ICT'";
		
		String queryUrl = Constants.GIS_MAPSERVER_URL + "/" + Constants.GIS_LAYER_ROOMS;
		String whereClause = "RM_ID='116'";

		//create query
		Query query = new Query();
		query.setGeometry(new Envelope(BUILDING_X_MIN, BUILDING_Y_MIN, BUILDING_X_MAX, BUILDING_Y_MAX));
		query.setOutSpatialReference(SpatialReference.create(BUILDING_SPATIAL_REF));
		query.setReturnGeometry(true);
		query.setWhere(whereClause);

		System.out.println(whereClause + " " + queryUrl);

		QueryTask qTask = new QueryTask(queryUrl);
		FeatureSet fs = null;
		
		//execute query
		try {
			fs = qTask.execute(query);
		} catch (Exception e) {
			System.out.println("error on executing!");
			e.printStackTrace();
		}


		if (fs != null) {
			System.out.println(fs.getGraphics().length + " matchs found");
			Graphic[] grs = fs.getGraphics();

			Geometry geo = CoordinateUtil.transformGeometryToWGS84(grs[0].getGeometry(), SpatialReference.create(BUILDING_SPATIAL_REF));
			Point centerPoint = CoordinateUtil.getCenterCoordinateOfGeometry(geo);
			System.out.println(centerPoint.getY() + ", " + centerPoint.getX());
		}else{
			System.out.println("no match found");
		}

		return -1;
	}
}
