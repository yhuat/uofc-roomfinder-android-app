package com.uofc.roomfinder.android.activities;

import java.util.Arrays;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.LocationService;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.arcgis.android.samples.attributequery.R;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.LinearUnit;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.Unit;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.SimpleFillSymbol;
import com.uofc.roomfinder.android.DataModel;
import com.uofc.roomfinder.android.util.Constants;
import com.uofc.roomfinder.android.util.MyLocation;
import com.uofc.roomfinder.android.util.MyLocation.LocationResult;
import com.uofc.roomfinder.android.util.tasks.RoomQuery;
import com.uofc.roomfinder.entities.routing.RoutePoint;

public class MapActivity extends Activity {

	private final static int SEARCH_CODE = 1;

	MapView mapView;
	GraphicsLayer graphicsLayer;
	ImageButton btnSearchForm;
	ImageButton btnArView;
	ProgressDialog progressDialog;
	TextView txtStatusBar;

	boolean actualizing = true;

	// getter & setter
	public ProgressDialog getProgressDialog() {
		return progressDialog;
	}

	public void setProgressDialog(ProgressDialog progressDialog) {
		this.progressDialog = progressDialog;
	}

	public GraphicsLayer getGraphicsLayer() {
		return graphicsLayer;
	}

	// Activity methods
	@Override
	public void onCreate(Bundle savedInstanceState) {
		System.out.println("onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity_layout);
		mapView = (MapView) findViewById(R.id.map);

		// WTF?????? without this layer no layer is displayed, I have no Idea why!
		// simple workaround: add it and set the visibility to false...
		ArcGISDynamicMapServiceLayer beastLayer = new ArcGISDynamicMapServiceLayer("http://asebeast2.cpsc.ucalgary.ca:7000/ArcGIS/rest/services/RoomFinder/MapServer");
		beastLayer.setVisible(false);
		mapView.addLayer(beastLayer);
		
		//layer for rooms
		ArcGISDynamicMapServiceLayer roomLayer = new ArcGISDynamicMapServiceLayer("http://136.159.24.32/ArcGIS/rest/services/Rooms/Rooms/MapServer");
		roomLayer.setVisible(true);	
		mapView.addLayer(roomLayer);	
		
		// add layer for buildings
		ArcGISDynamicMapServiceLayer buildingLayer = new ArcGISDynamicMapServiceLayer(Constants.MAPSERVER_BUILDINGS_URL);
		buildingLayer.setOpacity(0.4f);
		mapView.addLayer(buildingLayer);
		
		// graphics layer for routes and POIs
		graphicsLayer = new GraphicsLayer();
		SimpleRenderer sr = new SimpleRenderer(new SimpleFillSymbol(Color.RED));
		graphicsLayer.setRenderer(sr);
		mapView.addLayer(graphicsLayer);
		
		mapView.setMaxResolution(10000.0);
		
		System.out.println(mapView.getMaxResolution());
		System.out.println(mapView.getMinResolution());
		
		//System.out.println(mapView.getMapBoundaryExtent().getYMax() +", "+ mapView.getMapBoundaryExtent().getXMax() + " - " +  mapView.getMapBoundaryExtent().getYMin() +","+mapView.getMapBoundaryExtent().getXMin());
		

		//location listener
		DataModel.getInstance().setCurrentPositionWGS84(new Point(-114.127575, 51.080126));
		System.out.println(DataModel.getInstance().getCurrentPositionWGS84().getX());
		System.out.println(DataModel.getInstance().getCurrentPositionNAD83().getX());
		
		// textBTN
		txtStatusBar = (TextView) findViewById(R.id.txt_status);

		// button for search form
		btnSearchForm = (ImageButton) findViewById(R.id.btn_search);
		btnSearchForm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// switch to search form screen
				Intent nextScreen = new Intent(getApplicationContext(), SearchActivity.class);
				startActivityForResult(nextScreen, SEARCH_CODE); // start only for result
			}
		});

		// button for AR view
		btnArView = (ImageButton) findViewById(R.id.btn_ar);
		btnArView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent();
				i.setAction(Intent.ACTION_VIEW);
				i.setDataAndType(Uri.parse(Constants.REST_ANNOTATION_BUILDINGS_URL), "application/mixare-json");
				startActivity(i);
			}
		});

		
		/*
		mapView.setOnStatusChangedListener(new OnStatusChangedListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void onStatusChanged(Object source, STATUS status) {

				System.out.println("---Status: " + status);
				System.out.println("----1: " + mapView.getLayer(0));
				//System.out.println("----2: " + mapView.getLayer(1));
				
				//mapView.getLayer(1).setVisible(false);
				
				if (source == mapView && status == STATUS.INITIALIZED) {

					
					
					// set location listener
					LocationService ls = mapView.getLocationService();
					ls.setAutoPan(false);
					LocationListener locationListener = new LocationListener() {

						// Zooms to the current location when first GPS fix arrives
						public void onLocationChanged(Location loc) {
							System.out.println("onLocationChange");

							double locy = loc.getLatitude();
							double locx = loc.getLongitude();
							double locz = loc.getAltitude();
							float accuracy = loc.getAccuracy();

							// TODO test
							locy = 51.080652;
							locx = -114.129195;

							// save current position in singleton
							DataModel.getInstance().setCurrentPosition(new RoutePoint(locx, locy));

							// debug print on display
							txtStatusBar.setText("lat: " + locy + " long: " + locx + "\nalt: " + locz + " acc: " + accuracy + "m");

							// actualize only if accuracy is better than 300m
							// if (accuracy < 300) {
							// if (actualizing) {
							// actualizing = false;
							Point wgspoint = new Point(locx, locy);
							Point mapPoint = (Point) GeometryEngine.project(wgspoint, SpatialReference.create(Constants.SPARTIAL_REF_MAP),
									mapView.getSpatialReference());
							Unit mapUnit = mapView.getSpatialReference().getUnit();
							//System.out.println(mapUnit);
							double zoomWidth = Unit.convertUnits(0.08, Unit.create(LinearUnit.Code.MILE_US), mapUnit);
							System.out.println(zoomWidth);
							Envelope zoomExtent = new Envelope(mapPoint, zoomWidth, zoomWidth);
							mapView.setExtent(zoomExtent);
							// }
						}

						@Override
						public void onProviderDisabled(String provider) {
							// TODO Auto-generated method stub
							System.out.println("Doh, no Location service");
						}

						@Override
						public void onProviderEnabled(String provider) {
							// TODO Auto-generated method stub
							System.out.println("Yeah, we have an enabled location service");

						}

						@Override
						public void onStatusChanged(String provider, int status, Bundle extras) {
							// TODO Auto-generated method stub
							System.out.println("location service status update");

						}
					};
					ls.start();
					ls.setLocationListener(locationListener);

					LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, locationListener);
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, locationListener);

				}
			}
		});*/
	}

	@Override
	protected void onPause() {
		super.onPause();
		mapView.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mapView.unpause();
		System.out.println("onResume");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Receiving the Data
		Intent intent = data;
		
		//if no data then return
		if (data == null)
			return;
		
		// split in room and building
		// received data should look like ICT550
		String receivedData = intent.getStringExtra("room");
		String regex = "(?<=[\\w&&\\D])(?=\\d)";
		String building = receivedData.split(regex)[0];
		String room = receivedData.split(regex)[1];
		
		String impedance = intent.getStringExtra("impedance");

		Log.e("MapScreen", "searching room: " + room + " and building: " + building + "and impedance: " + impedance);

		// set layer and build where clause for query
		String targetLayer = Constants.MAPSERVER_ROOM_QUERY_URL;
		String whereClause = Constants.QUERY_COL_RM_ID + "='" + room + "'" + " AND " + Constants.QUERY_COL_BLD_ID + "='" + building + "'";

		// start query task
		Object[] queryParams = { targetLayer, whereClause, this, impedance };
		RoomQuery asyncQuery = new RoomQuery();
		asyncQuery.execute(queryParams);
	}
}