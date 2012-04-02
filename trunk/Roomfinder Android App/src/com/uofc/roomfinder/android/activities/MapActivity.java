package com.uofc.roomfinder.android.activities;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
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
import com.uofc.roomfinder.android.util.BuildingDAOImpl;
import com.uofc.roomfinder.android.util.Constants;
import com.uofc.roomfinder.util.async_tasks.RoomQuery;

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

		// add layer for room data
		mapView.addLayer(new ArcGISDynamicMapServiceLayer(Constants.GIS_MAPSERVER_URL));

		// add layer for buildings
		ArcGISDynamicMapServiceLayer buildingLayer = new ArcGISDynamicMapServiceLayer(Constants.GIS_MAPSERVER_BUILDINGS_URL);
		buildingLayer.setOpacity(0.4f);
		mapView.addLayer(buildingLayer);

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

				BuildingDAOImpl.updateBuildingTable();
			}
		});

		// button for AR view
		btnArView = (ImageButton) findViewById(R.id.btn_ar);
		btnArView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent();
				i.setAction(Intent.ACTION_VIEW);
				 i.setDataAndType(Uri.parse("http://ec2-23-20-196-109.compute-1.amazonaws.com:8080/UofC_Roomfinder_Server/rest/annotation/cat/buildings"),"application/mixare-json");
				//i.setDataAndType(Uri.parse("http://mixare.org/geotest.php"), "application/mixare-json");

				startActivity(i);
			}
		});

		mapView.setOnStatusChangedListener(new OnStatusChangedListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void onStatusChanged(Object source, STATUS status) {

				if (source == mapView && status == STATUS.INITIALIZED) {

					// Initialize graphics layer
					graphicsLayer = new GraphicsLayer();
					SimpleRenderer sr = new SimpleRenderer(new SimpleFillSymbol(Color.RED));
					graphicsLayer.setRenderer(sr);
					mapView.addLayer(graphicsLayer);

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

							txtStatusBar.setText("lat: " + locy + " long: " + locx + "\nalt: " + locz + " acc: " + accuracy + "m");

							// actualize only if accuracy is better than 300m
							// if (accuracy < 300) {
							if (actualizing) {
								actualizing = false;
								Point wgspoint = new Point(locx, locy);
								Point mapPoint = (Point) GeometryEngine.project(wgspoint, SpatialReference.create(Constants.SPARTIAL_REF_MAP),
										mapView.getSpatialReference());
								Unit mapUnit = mapView.getSpatialReference().getUnit();
								double zoomWidth = Unit.convertUnits(5, Unit.create(LinearUnit.Code.MILE_US), mapUnit);
								Envelope zoomExtent = new Envelope(mapPoint, zoomWidth, zoomWidth);
								mapView.setExtent(zoomExtent);
							}
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
		});
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
		System.out.println("onActivityResult");
		super.onActivityResult(requestCode, resultCode, data);

		// Receiving the Data
		Intent intent = data;
		String room = intent.getStringExtra("room");
		String building = intent.getStringExtra("building");

		Log.e("MapScreen", "searching room: " + room + " and building: " + building);

		// final String BUILDING_SERVER_URL = "http://136.159.24.32/ArcGIS/rest/services/Buildings/MapServer";
		// final String BUILDING_QUERY_LAYER = "0";
		// final String BUILDING_ID_COLUMN_NAME = "SDE.DBO.Building_Info.BLDG_ID";
		// String targetLayer = BUILDING_SERVER_URL + "/" + BUILDING_QUERY_LAYER;
		// String whereClause = BUILDING_ID_COLUMN_NAME + " like '%'";

		String targetLayer = Constants.GIS_MAPSERVER_URL + "/" + Constants.GIS_LAYER_ROOMS;
		String whereClause = "RM_ID='" + room + "'";
		// TODO: add building to where clause

		Object[] queryParams = { targetLayer, whereClause, this };

		RoomQuery asyncQuery = new RoomQuery();
		asyncQuery.execute(queryParams);
	}

}