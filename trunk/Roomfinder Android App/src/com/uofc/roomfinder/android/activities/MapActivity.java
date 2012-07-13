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
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.event.OnStatusChangedListener;
import com.uofc.roomfinder.R;
import com.uofc.roomfinder.android.DataModel;
import com.uofc.roomfinder.android.map.MapDrawer;
import com.uofc.roomfinder.android.util.Constants;
import com.uofc.roomfinder.android.util.CoordinateUtil;
import com.uofc.roomfinder.android.util.GisServerUtil;
import com.uofc.roomfinder.android.views.CampusMapView;
import com.uofc.roomfinder.android.views.RouteNavigationBar;
import com.uofc.roomfinder.entities.Point3D;
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
	private ImageView info_wifi;

	boolean actualizing = true;

	// Activity methods
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		// Request for the progress bar to be shown in the title
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setTheme(android.R.style.Theme_Translucent_NoTitleBar);
		setContentView(R.layout.map_activity);

		// Make sure the progress bar is visible
		setProgressBarVisibility(true);

		// set loading screen
		// this.progressDialog = ProgressDialog.show(this, "", "Please wait....initializing maps.");
		mapView = (CampusMapView) findViewById(R.id.map);

		// register for nav bar touch events
		View.OnTouchListener navBarListener = new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// System.err.println("I've been touched");
				handleNavBarTouch((int) event.getX(), (int) event.getY());
				return false;
			}
		};
		findViewById(R.id.nav_bar).setOnTouchListener(navBarListener);

		// register vor nav bar layout touch events (to go to the next and last segment)
		View.OnTouchListener navBarLayoutListener = new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				handleNavBarLayoutTouch((int) event.getX(), (int) event.getY());
				return false;
			}

		};
		findViewById(R.id.layout_navbar).setOnTouchListener(navBarLayoutListener);

		// mapView.init();
		DataModel.getInstance().setMap(this);

		// System.out.println(mapView.getMapBoundaryExtent().getYMax() +", "+ mapView.getMapBoundaryExtent().getXMax() + " - " +
		// mapView.getMapBoundaryExtent().getYMin() +","+mapView.getMapBoundaryExtent().getXMin());

		// location listener
		// DataModel.getInstance().setCurrentPositionWGS84(new Point(-114.127575, 51.080126)); //somewhere in MS
		// DataModel.getInstance().setCurrentPositionNAD83(new Point(701192.8861, 5662659.7696)); //franks office
		// DataModel.getInstance().setCurrentPositionWGS84(new Point(-114.130147, 51.080267)); // franks office

		// System.out.println(DataModel.getInstance().getCurrentPositionNAD83().getX() + ", " + DataModel.getInstance().getCurrentPositionNAD83().getY());
		//
		// System.out.println(DataModel.getInstance().getCurrentPositionWGS84().getX());
		// System.out.println(DataModel.getInstance().getCurrentPositionNAD83().getX());

		// info box
		info_box_layout = (LinearLayout) findViewById(R.id.info_box);
		info_box_text = (TextView) findViewById(R.id.info_txt);
		info_box_img = (ImageView) findViewById(R.id.info_img);
		info_wifi = (ImageView) findViewById(R.id.logo_wifi);

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
			System.out.println("mapactivity: " + impedance);

			// start async task
			if (impedance == null) {
				GisServerUtil.startRoomQuery(building, room);
			} else {
				GisServerUtil.startRoomWithRouteQuery(building, room, impedance);
			}

		}

		mapView.setOnStatusChangedListener(new OnStatusChangedListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void onStatusChanged(Object source, STATUS status) {

				// mapView.getLayer(1).setVisible(false);

				if (source == mapView && status == STATUS.INITIALIZED) {
					// set location listener
					// LocationService ls = mapView.getLocationService();
					// ls.setAutoPan(false);
					// ls.start();
					// ls.setLocationListener(DataModel.getInstance().getLocationHandler().getLocationListener());
				}
			}
		});
	}

	public ImageView getInfo_box_img() {
		return info_box_img;
	}

	public void setInfo_box_img(ImageView info_box_img) {
		this.info_box_img = info_box_img;
	}

	public ImageView getInfo_wifi() {
		return info_wifi;
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

	// getter & setter public ProgressDialog getProgressDialog() { return progressDialog; }

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
	 * handles touch events to the right and left of the navigation bar (left of the bar, decrement active segment and display it)
	 * 
	 * @param x
	 * @param y
	 */
	private void handleNavBarLayoutTouch(int x, int y) {

		// System.out.println("click: " + x + "y: " + y + ", " + mapNavBar.getNavbarParts().get(0).left);
		int startOfNavbar = getNavbarOffsetX();
		int endOfNavbar = getNavbarOffsetX() + mapNavBar.getNavbarParts().get(mapNavBar.getNavbarParts().size() - 1).right;

		if (x < startOfNavbar)
			this.mapNavBar.decrementActiveElement();
		else if (x > endOfNavbar)
			this.mapNavBar.incrementActiveElement();

		try {
			MapDrawer.displayRouteSegment(this.mapNavBar.getActiveElement());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * handles touch events of the navbar (switch segments by clicking on specific rectangles)
	 * 
	 * @param touchX
	 * @param touchY
	 */
	private void handleNavBarTouch(int touchX, int touchY) {
		// System.out.println(touchX + ", " + touchY);

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
	}

	/**
	 * handles touches on the display
	 */
	/*
	 * @Override public boolean onTouchEvent(MotionEvent event) {
	 * 
	 * // touch coordinates relative to the map view int touchX = (int) event.getX() - getNavbarOffsetX(); int touchY = (int) event.getY() -
	 * getMapviewOffsetY();
	 * 
	 * handleNavBarTouch(touchX, touchY);
	 * 
	 * int eventaction = event.getAction();
	 * 
	 * switch (eventaction) { case MotionEvent.ACTION_DOWN: System.out.println("action down"); break;
	 * 
	 * case MotionEvent.ACTION_MOVE: System.out.println("action move"); break;
	 * 
	 * case MotionEvent.ACTION_UP: System.out.println("action up");
	 * 
	 * break; }
	 * 
	 * // tell the system that we handled the event and no further processing is required return true; }
	 */

	private MenuItem navArMenuItem;

	/**
	 * creates option menu out of xml file
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map_menu_options, menu);

		navArMenuItem = menu.getItem(4);
		navArMenuItem.setVisible(false);

		return true;
	}

	/**
	 * handles option menu inputs
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// make navbar invisible
		disableNavBarLayout();

		Intent nextScreen = null;

		switch (item.getItemId()) {
		case R.id.item_route:
			// is pos available
			if (!DataModel.getInstance().isCurrentPositionAvailable()) {
				Toast.makeText(getApplicationContext(), "No position available yet.", Toast.LENGTH_SHORT).show();
				break;
			}

			if (mapNavBar != null) {
				mapNavBar.setActiveElement(0);
			}

			nextScreen = new Intent(getApplicationContext(), SearchForm.class);
			nextScreen.putExtra("requestCode", Constants.SEARCH_ROOM_WITH_ROUTE);
			startActivityForResult(nextScreen, Constants.SEARCH_ROOM_WITH_ROUTE);
			break;

		case R.id.item_search_room:
			nextScreen = new Intent(getApplicationContext(), SearchForm.class);
			nextScreen.putExtra("requestCode", Constants.SEARCH_ROOM);
			startActivityForResult(nextScreen, Constants.SEARCH_ROOM);
			break;

		case R.id.item_quicklinks:

			if (mapNavBar != null) {
				mapNavBar.setActiveElement(0);
			}

			// is pos available
			if (!DataModel.getInstance().isCurrentPositionAvailable()) {
				Toast.makeText(getApplicationContext(), "No position available yet.", Toast.LENGTH_SHORT).show();
				break;
			}

			nextScreen = new Intent(getApplicationContext(), Quicklinks.class);
			startActivityForResult(nextScreen, Constants.QUICKLINKS);
			break;

		case R.id.item_ar:
			nextScreen = new Intent();
			nextScreen.setAction(Intent.ACTION_VIEW);
			DataModel m = DataModel.getInstance();

			nextScreen.setDataAndType(Uri.parse(Constants.REST_ANNOTATION_BUILDINGS_URL), "application/mixare-json");

			startActivity(nextScreen);
			break;

//		case R.id.item_arial_toggle:
//			if (this.mapView.isArialEnabled()) {
//				this.mapView.disableArialImage();
//			} else {
//				this.mapView.enableArialImage();
//			}
//
//			break;

		case R.id.item_nav_ar:

			nextScreen = new Intent();
			nextScreen.setAction(Intent.ACTION_VIEW);

			if (DataModel.getInstance().getDestinationPoint() != null) {

				Point3D dest = CoordinateUtil.transformToWGS84(DataModel.getInstance().getDestinationPoint());
				Point3D next = CoordinateUtil.transformToWGS84(mapNavBar.getActiveWaypoint());

				System.out.println("x: " + dest.getX());
				System.out.println("y: " + dest.getY());

				String url = Constants.REST_ANNOTATION_NAVIGATION_URL + "?next_x=" + next.getX() + "&next_y=" + next.getY() + "&next_z=" + next.getZ()
						+ "&dest_x=" + dest.getX() + "&dest_y=" + dest.getY() + "&dest_z=" + dest.getZ() + "&next_text="
						+ mapNavBar.getActiveSegment().getDescription() + "&dest_text=" + DataModel.getInstance().getDestinationText();

				try {
					System.out.println(UrlReader.stringToUri(url).toString());

					nextScreen.setDataAndType(Uri.parse(UrlReader.stringToUri(url).toString()), "application/mixare-json");
					startActivity(nextScreen);

				} catch (Exception e) {

					e.printStackTrace();
				}
			}

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

			// get x, y, z coordinate
			double x = intent.getDoubleExtra("roomX", -1);
			double y = intent.getDoubleExtra("roomY", -1);
			double z = intent.getDoubleExtra("roomZ", -1);
			String text = intent.getStringExtra("roomText");

			// exit condition
			if (x == -1 || y == -1 || z == -1) {
				Toast.makeText(getApplicationContext(), "error 102: no coordinates came back from quicklinks", Toast.LENGTH_SHORT).show();
				return;
			}

			GisServerUtil.createRoute(new Point3D(x, y, z), text);
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

		// user either wants to look at a route or for a searched room
		// since the searched thing has not to be on his current floor -> disable automatic floor switching
		DataModel.getInstance().setUpdateFloorToCurrentPos(false);
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
	public void enableNavBarLayout() {
		findViewById(R.id.layout_navbar).setVisibility(View.VISIBLE);

		try {
			navArMenuItem.setVisible(true);
		} catch (Exception e) {
		}

		// disable floor layer switching, user can now switch layer via the route nav bar
		DataModel.getInstance().setUpdateFloorToCurrentPos(false);
	}

	/**
	 * makes navbar linear layout invisible
	 */
	public void disableNavBarLayout() {
		findViewById(R.id.layout_navbar).setVisibility(View.INVISIBLE);
		navArMenuItem.setVisible(false);
	}

	public void showWifiLogo(boolean visible) {
		if (visible)
			findViewById(R.id.logo_wifi).setVisibility(View.VISIBLE);
		else
			findViewById(R.id.logo_wifi).setVisibility(View.INVISIBLE);

	}

	public ProgressDialog getProgressDialog() {
		return progressDialog;
	}

}