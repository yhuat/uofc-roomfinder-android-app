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
import android.widget.TextView;
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

	private final static int SEARCH_CODE = 1;

	ProgressDialog progressDialog;

	// views and layouts
	RouteNavigationBar mapNavBar; // map navigation bar
	CampusMapView mapView;
	TextView txtStatusBar;

	boolean actualizing = true;

	// Activity methods
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);

		// set loading screen
		// this.progressDialog = ProgressDialog.show(this, "", "Please wait....initializing maps.");
		mapView = (CampusMapView) findViewById(R.id.map);
		mapView.init();
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
			GisServerUtil.startRouteQuery(building, room, impedance);
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
		this.mapView.getLocationOnScreen(mOffset);
		return mOffset[1];
	}

	/**
	 * handles touches on the display
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		// touch coordinates relative to the map view
		int touchX = (int) event.getX();
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
		switch (item.getItemId()) {
		case R.id.item_route:
			Intent nextScreen = new Intent(getApplicationContext(), SearchForm.class);
			startActivityForResult(nextScreen, SEARCH_CODE);
			break;

		case R.id.item_ar:
			Intent i = new Intent();
			i.setAction(Intent.ACTION_VIEW);
			DataModel m = DataModel.getInstance();

			if (m.getDestinationPoint() == null) {
				i.setDataAndType(Uri.parse(Constants.REST_ANNOTATION_BUILDINGS_URL), "application/mixare-json");
			} else {
				String uri = m.getDestinationAsWgs84().getJsonUrl() + m.getDestinationText();
				try {
					i.setDataAndType(Uri.parse(UrlReader.stringToUri(uri).toString()), "application/mixare-json");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			startActivity(i);
			break;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Receiving the Data
		Intent intent = data;

		// if no data then return
		if (data == null)
			return;

		// split in room and building
		// received data should look like ICT550
		String receivedData = intent.getStringExtra("room");
		String regex = "(?<=[\\w&&\\D])(?=\\d)";
		String building = receivedData.split(regex)[0];
		String room = receivedData.split(regex)[1];
		String impedance = intent.getStringExtra("impedance");

		GisServerUtil.startRouteQuery(building, room, impedance);
	}

}