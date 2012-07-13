package com.uofc.roomfinder.android.activities;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.uofc.roomfinder.R;
import com.uofc.roomfinder.android.DataModel;
import com.uofc.roomfinder.android.activities.adapter.MainMenuDataset;
import com.uofc.roomfinder.android.activities.adapter.MainMenuListAdapter;
import com.uofc.roomfinder.android.util.Constants;
import com.uofc.roomfinder.android.util.LocationHandler;
import com.uofc.roomfinder.android.util.Util;
import com.uofc.roomfinder.android.util.tasks.AddFriend;

public class MainMenu extends ListActivity {

	private ArrayList<MainMenuDataset> data = new ArrayList<MainMenuDataset>();

	final int MENUITEM_DIRECTION = 0;
	final int MENUITEM_SEARCH_ROOM = 1;
	final int MENUITEM_EXPLORE = 2;
	final int MENUITEM_CAMPUS_MAP = 3;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		// activity layout
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Translucent_NoTitleBar);
		setContentView(R.layout.menu_main);

		// fill data array list with menu data
		populateData();

		// link data and view
		MainMenuListAdapter adapter = new MainMenuListAdapter(this, R.layout.menu_main_list_item, R.id.title, data);
		setListAdapter(adapter);
		getListView().setTextFilterEnabled(true);

		// do basic init stuff
		initRoomfinderApplication();

	}

	/**
	 * 
	 */
	private void initRoomfinderApplication() {
		// init LocationHandler
		DataModel.getInstance().setLocationHandler(new LocationHandler());

		// init WifiManager
		DataModel.getInstance().setWifiManager((WifiManager) getSystemService(Context.WIFI_SERVICE));

		// check if device has internet access
		if (!isOnline()) {
			AlertDialog ad = new AlertDialog.Builder(this).create();
			ad.setCancelable(false); // This blocks the 'BACK' button
			ad.setMessage("There is no internet connection available!");
			ad.setButton("Exit App", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					System.exit(0);
				}
			});
			ad.show();
		}

		// ask for user name on first launch
		if (Util.loadUsername() == null) {
			showUserNamePopup();
		} else {
			System.out.println(Util.loadUsername());
		}

	}

	/**
	 * creates option menu out of xml file
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu_options, menu);
		return true;
	}

	/**
	 * shows an alert dialogue asking for the user name
	 */
	private void showUserNamePopup() {
		// create alert dialog
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		// set texts
		alert.setTitle("Enter a nickname");
		alert.setMessage("This name is used for the social features.");
		alert.setCancelable(false);

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				System.out.println("value: " + value);
				Util.saveUsername(value);
			}
		});

		// alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog, int whichButton) {
		// // Canceled.
		// }
		// });

		alert.show();
	}

	/**
	 * shows an alert dialogue asking for the user name
	 */
	private void showAddFriendPopup() {
		// create alert dialog
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		// set texts
		alert.setTitle("Add friend");
		alert.setMessage("Enter friend's name (your name is \"" + Util.loadUsername() + "\")");
		alert.setCancelable(false);

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				new AddFriend().execute(Util.loadUsername(), value);
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
			}
		});

		alert.show();
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
	}

	@Override
	public void onListItemClick(ListView parent, View v, int position, long id) {

		Intent nextScreen;

		switch (position) {
		case MENUITEM_DIRECTION:
			// is pos available
			if (!DataModel.getInstance().isCurrentPositionAvailable()) {
				Toast.makeText(getApplicationContext(), "No position available yet.", Toast.LENGTH_SHORT).show();
				break;
			}

			nextScreen = new Intent(getApplicationContext(), SearchForm.class);
			nextScreen.putExtra("requestCode", Constants.SEARCH_ROOM_WITH_ROUTE);
			startActivityForResult(nextScreen, Constants.SEARCH_ROOM_WITH_ROUTE);
			break;

		case MENUITEM_SEARCH_ROOM:
			nextScreen = new Intent(getApplicationContext(), SearchForm.class);
			nextScreen.putExtra("requestCode", Constants.SEARCH_ROOM);
			startActivityForResult(nextScreen, Constants.SEARCH_ROOM);
			break;

		case MENUITEM_EXPLORE:
			nextScreen = new Intent();
			nextScreen.setAction(Intent.ACTION_VIEW);
			nextScreen.setDataAndType(Uri.parse(Constants.REST_ANNOTATION_BUILDINGS_URL), "application/mixare-json");
			startActivity(nextScreen);
			break;

		case MENUITEM_CAMPUS_MAP:
			nextScreen = new Intent(getApplicationContext(), MapActivity.class);
			startActivity(nextScreen);
			break;

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Receiving the Data
		Intent intent = data;

		// if no data then return
		if (data == null)
			return;

		System.out.println("requestcode: " + requestCode + "resultcode: " + resultCode);

		Intent nextScreen;

		switch (requestCode) {
		case Constants.SEARCH_ROOM:

			// forward parameter to map activity
			nextScreen = new Intent(getApplicationContext(), MapActivity.class);
			nextScreen.putExtra("room", intent.getStringExtra("room"));
			startActivity(nextScreen);
			break;

		case Constants.SEARCH_ROOM_WITH_ROUTE:

			// forward parameter to map activity
			nextScreen = new Intent(getApplicationContext(), MapActivity.class);
			nextScreen.putExtra("room", intent.getStringExtra("room"));
			nextScreen.putExtra("impedance", intent.getStringExtra("impedance"));
			startActivity(nextScreen);
			break;

		default:
			break;
		}
		System.out.println("getting result end");

	}

	/**
	 * helper method to fill the lists with data
	 */
	private void populateData() {
		String title;
		String detail;
		int imageId;
		MainMenuDataset dataset;

		title = "Directions to...";
		detail = "get a route from your position to destination";
		imageId = R.drawable.route_icon;
		dataset = new MainMenuDataset(imageId, title, detail);
		data.add(dataset);

		title = "Search";
		detail = "search for a prof or room";
		imageId = R.drawable.point_icon;
		dataset = new MainMenuDataset(imageId, title, detail);
		data.add(dataset);

		title = "Explore";
		detail = "discover the campus in another dimension";
		imageId = R.drawable.compass_icon;
		dataset = new MainMenuDataset(imageId, title, detail);
		data.add(dataset);

		title = "Campus Map";
		detail = "map of the UofC campus";
		imageId = R.drawable.map_icon;
		dataset = new MainMenuDataset(imageId, title, detail);
		data.add(dataset);

	}

	/**
	 * handles option menu inputs
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.item_add_friend:

			break;
		case R.id.item_change_name:
			showUserNamePopup();
			break;
		}

		return true;
	}

}
