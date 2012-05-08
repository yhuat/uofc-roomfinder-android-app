package com.uofc.roomfinder.android.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.Layer;
import com.esri.core.geometry.Point;
import com.uofc.roomfinder.R;
import com.uofc.roomfinder.android.DataModel;
import com.uofc.roomfinder.android.map.MapDrawer;
import com.uofc.roomfinder.android.util.Constants;
import com.uofc.roomfinder.android.util.GisServerUtil;
import com.uofc.roomfinder.android.views.CampusMapView;
import com.uofc.roomfinder.android.views.RouteNavigationBar;
import com.uofc.roomfinder.util.UrlReader;

public class MapActivity extends Activity {

	ProgressDialog progressDialog;

	// views and layouts
	RouteNavigationBar mapNavBar; // map navigation bar
	CampusMapView mapView;
	TextView txtStatusBar;

	// info box
	private LinearLayout info_box_layout;
	private TextView info_box_text;
	private ImageView info_box_img;

	boolean actualizing = true;

	// Activity methods
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Translucent_NoTitleBar);
		setContentView(R.layout.map_activity);

		// set loading screen
		// this.progressDialog = ProgressDialog.show(this, "", "Please wait....initializing maps.");
		mapView = (CampusMapView) findViewById(R.id.map);
		// mapView.init();
		DataModel.getInstance().setMap(this);

		// System.out.println(mapView.getMapBoundaryExtent().getYMax() +", "+ mapView.getMapBoundaryExtent().getXMax() + " - " +
		// mapView.getMapBoundaryExtent().getYMin() +","+mapView.getMapBoundaryExtent().getXMin());

		// location listener
		// DataModel.getInstance().setCurrentPositionWGS84(new Point(-114.127575, 51.080126)); //somewhere in MS
		// DataModel.getInstance().setCurrentPositionNAD83(new Point(701192.8861, 5662659.7696)); //franks office
		DataModel.getInstance().setCurrentPositionWGS84(new Point(-114.130147, 51.080267)); // franks office

		System.out.println(DataModel.getInstance().getCurrentPositionNAD83().getX() + ", " + DataModel.getInstance().getCurrentPositionNAD83().getY());

		System.out.println(DataModel.getInstance().getCurrentPositionWGS84().getX());
		System.out.println(DataModel.getInstance().getCurrentPositionNAD83().getX());

		// info box
		info_box_layout = (LinearLayout) findViewById(R.id.info_box);
		info_box_text = (TextView) findViewById(R.id.info_txt);
		info_box_img = (ImageView) findViewById(R.id.info_img);

		// textBTN
		txtStatusBar = (TextView) findViewById(R.id.txt_status);

		// create an instance of map nav bar
		mapNavBar = (RouteNavigationBar) findViewById(R.id.nav_bar);

		// if a destination building is set in the intent data -> create a route to get there
		Intent intent = getIntent();
		if (intent.getStringExtra("room") != null) {

			// split in room and building
			// received data should look like ICT550
			String receivedData = intent.getStringExtra("room");
			String regex = "(?<=[\\w&&\\D])(?=\\d)";
			String building = receivedData.split(regex)[0];
			String room = receivedData.split(regex)[1];
			String impedance = intent.getStringExtra("impedance");

			// start async task
			if (impedance == null) {
				GisServerUtil.startRoomQuery(building, room);
			} else {
				GisServerUtil.startRoomWithRouteQuery(building, room, impedance);
			}

		}

		/*
		 * mapView.setOnStatusChangedListener(new OnStatusChangedListener() {
		 * 
		 * private static final long serialVersionUID = 1L;
		 * 
		 * @Override public void onStatusChanged(Object source, STATUS status) {
		 * 
		 * System.out.println("---Status: " + status); System.out.println("----1: " + mapView.getLayer(0)); //System.out.println("----2: " +
		 * mapView.getLayer(1));
		 * 
		 * //mapView.getLayer(1).setVisible(false);
		 * 
		 * if (source == mapView && status == STATUS.INITIALIZED) {
		 * 
		 * 
		 * 
		 * // set location listener LocationService ls = mapView.getLocationService(); ls.setAutoPan(false); LocationListener locationListener = new
		 * LocationListener() {
		 * 
		 * // Zooms to the current location when first GPS fix arrives public void onLocationChanged(Location loc) { System.out.println("onLocationChange");
		 * 
		 * double locy = loc.getLatitude(); double locx = loc.getLongitude(); double locz = loc.getAltitude(); float accuracy = loc.getAccuracy();
		 * 
		 * // TODO test locy = 51.080652; locx = -114.129195;
		 * 
		 * // save current position in singleton DataModel.getInstance().setCurrentPosition(new RoutePoint(locx, locy));
		 * 
		 * // debug print on display txtStatusBar.setText("lat: " + locy + " long: " + locx + "\nalt: " + locz + " acc: " + accuracy + "m");
		 * 
		 * // actualize only if accuracy is better than 300m // if (accuracy < 300) { // if (actualizing) { // actualizing = false; Point wgspoint = new
		 * Point(locx, locy); Point mapPoint = (Point) GeometryEngine.project(wgspoint, SpatialReference.create(Constants.SPARTIAL_REF_MAP),
		 * mapView.getSpatialReference()); Unit mapUnit = mapView.getSpatialReference().getUnit(); //System.out.println(mapUnit); double zoomWidth =
		 * Unit.convertUnits(0.08, Unit.create(LinearUnit.Code.MILE_US), mapUnit); System.out.println(zoomWidth); Envelope zoomExtent = new Envelope(mapPoint,
		 * zoomWidth, zoomWidth); mapView.setExtent(zoomExtent); // } }
		 * 
		 * @Override public void onProviderDisabled(String provider) { // TODO Auto-generated method stub System.out.println("Doh, no Location service"); }
		 * 
		 * @Override public void onProviderEnabled(String provider) { // TODO Auto-generated method stub
		 * System.out.println("Yeah, we have an enabled location service");
		 * 
		 * }
		 * 
		 * @Override public void onStatusChanged(String provider, int status, Bundle extras) { // TODO Auto-generated method stub
		 * System.out.println("location service status update");
		 * 
		 * } }; ls.start(); ls.setLocationListener(locationListener);
		 * 
		 * LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		 * locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, locationListener);
		 * locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, locationListener);
		 * 
		 * } } });
		 */
		// this.progressDialog.dismiss();
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

	// getter & setter
	public ProgressDialog getProgressDialog() {
		return progressDialog;
	}

	public void setProgressDialog(ProgressDialog progressDialog) {
		this.progressDialog = progressDialog;
	}

	public CampusMapView getMapView() {
		return mapView;
	}

	public RouteNavigationBar getMapNavBar() {
		return mapNavBar;
	}

	/**
	 * this method returns the y offset of the map view (the height of the android bar + app title text)
	 * 
	 * @return
	 */
	public int getMapviewOffsetY() {
		int mOffset[] = new int[2];
		this.mapNavBar.getLocationOnScreen(mOffset);
		return mOffset[1];
	}
	
	/**
	 * this method returns the x offset of the navbar
	 * 
	 * @return
	 */
	public int getNavbarOffsetX() {
		int mOffset[] = new int[2];
		this.mapNavBar.getLocationOnScreen(mOffset);
		return mOffset[0];
	}

	/**
	 * handles touches on the display
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int j = 0;
		for (Layer layer : this.mapView.getLayers()) {
			System.out.println(j++ + " - " + layer.isVisible() + " - " + layer.getName() + " - " + layer.getUrl());
		}

		// touch coordinates relative to the map view
		int touchX = (int) event.getX() - getNavbarOffsetX();
		int touchY = (int) event.getY() - getMapviewOffsetY();

		int eventaction = event.getAction();

		switch (eventaction) {
		case MotionEvent.ACTION_DOWN:
			System.out.println("action down");
			break;

		case MotionEvent.ACTION_MOVE:
			System.out.println("action move");
			break;

		case MotionEvent.ACTION_UP:
			System.out.println("action up");

			// check if touch hit a nav bar rectangle
			int i = 0;
			for (Rect rect : mapNavBar.getNavbarParts()) {
				if (rect.contains(touchX, touchY)) {
					try {
						MapDrawer.displayRouteSegment(i);
						mapNavBar.setActiveElement(i);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				i++;
			}

			break;
		}

		// tell the system that we handled the event and no further processing is required
		return true;
	}

	/**
	 * creates option menu out of xml file
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map_menu, menu);
		return true;
	}

	/**
	 * handles option menu inputs
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Intent nextScreen = null;

		switch (item.getItemId()) {
		case R.id.item_route:
			nextScreen = new Intent(getApplicationContext(), SearchForm.class);
			startActivityForResult(nextScreen, Constants.SEARCH_ROOM_WITH_ROUTE);
			break;

		case R.id.item_search_room:
			nextScreen = new Intent(getApplicationContext(), SearchForm.class);
			startActivityForResult(nextScreen, Constants.SEARCH_ROOM);
			break;

		case R.id.item_quicklinks:
			nextScreen = new Intent(getApplicationContext(), Quicklinks.class);
			startActivityForResult(nextScreen, Constants.QUICKLINKS);
			break;

		case R.id.item_ar:
			nextScreen = new Intent();
			nextScreen.setAction(Intent.ACTION_VIEW);
			DataModel m = DataModel.getInstance();

			if (m.getDestinationPoint() == null) {
				nextScreen.setDataAndType(Uri.parse(Constants.REST_ANNOTATION_BUILDINGS_URL), "application/mixare-json");
			} else {
				String uri = m.getDestinationAsWgs84().getJsonUrl() + m.getDestinationText();
				try {
					nextScreen.setDataAndType(Uri.parse(UrlReader.stringToUri(uri).toString()), "application/mixare-json");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			startActivity(nextScreen);
			break;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		System.out.println("getting result");

		// Receiving the Data
		Intent intent = data;

		// regex for splitting building and room (e.g. ICT550 -> ICT, 550)
		String regex = "(?<=[\\w&&\\D])(?=\\d)";

		String building;
		String room;
		String impedance;

		// if no data then return
		if (data == null)
			return;

		System.out.println("requestcode: " + requestCode + "resultcode: " + resultCode);

		switch (requestCode) {
		case Constants.SEARCH_ROOM:
			// exit condition
			if (intent.getStringExtra("room").split(regex).length < 2) {
				Toast.makeText(getApplicationContext(), "error 102: building and room could not be splitted", Toast.LENGTH_SHORT).show();
				return;
			}

			// split in room and building
			// received data should look like ICT550
			building = intent.getStringExtra("room").split(regex)[0];
			room = intent.getStringExtra("room").split(regex)[1];
			GisServerUtil.startRoomQuery(building, room);
			break;

		case Constants.SEARCH_ROOM_WITH_ROUTE:
			// exit condition
			if (intent.getStringExtra("room").split(regex).length < 2) {
				Toast.makeText(getApplicationContext(), "error 102: building and room could not be splitted", Toast.LENGTH_SHORT).show();
				return;
			}

			// split in room and building
			// received data should look like ICT550 and should have the param impedance
			building = intent.getStringExtra("room").split(regex)[0];
			room = intent.getStringExtra("room").split(regex)[1];
			impedance = intent.getStringExtra("impedance");
			GisServerUtil.startRoomWithRouteQuery(building, room, impedance);
			break;

		case Constants.QUICKLINKS:

			// exit condition
			if (intent.getStringExtra("room").split(regex).length < 2) {
				Toast.makeText(getApplicationContext(), "error 102: building and room could not be splitted", Toast.LENGTH_SHORT).show();
				return;
			}

			// split in room and building
			// received data should look like ICT550 and should have the param impedance
			building = intent.getStringExtra("room").split(regex)[0];
			room = intent.getStringExtra("room").split(regex)[1];
			impedance = "Length";
			GisServerUtil.startRoomWithRouteQuery(building, room, impedance);
			break;

		default:
			break;
		}
		System.out.println("getting result end");

	}

	/**
	 * displays an info box on map view at the bottom of the screen
	 * 
	 * default icon on the left: info icon
	 * 
	 * @param infoText
	 *            text to be displayed in the box
	 */
	public void displayInfoBox(String infoText) {
		displayInfoBox(infoText, R.drawable.info_icon);
	}

	/**
	 * displays an info box on map view at the bottom of the screen with defined image
	 * 
	 * @param infoText
	 *            text to be displayed in the box
	 * @param imgID
	 *            ID of the image to display on the left
	 */
	public void displayInfoBox(String infoText, int imgID) {
		// make layout visible if it is not visible already
		if (info_box_layout.getVisibility() != View.VISIBLE) {
			info_box_layout.setVisibility(View.VISIBLE);
		}

		// set text to box
		info_box_text.setText(infoText);

		// set image and set transparency
		info_box_img.setImageResource(imgID);
		info_box_img.setAlpha(200);
	}
	
	/**
	 * makes navbar linear layout visible
	 */
	public void enableNavBarLayout(){
		System.out.println(findViewById(R.id.layout_navbar).getVisibility());
		findViewById(R.id.layout_navbar).setVisibility(View.VISIBLE);
		System.out.println(findViewById(R.id.layout_navbar).getVisibility());
	}

}