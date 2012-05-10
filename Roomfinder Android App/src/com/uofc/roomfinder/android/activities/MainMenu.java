package com.uofc.roomfinder.android.activities;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import com.uofc.roomfinder.R;
import com.uofc.roomfinder.android.activities.adapter.MainMenuDataset;
import com.uofc.roomfinder.android.activities.adapter.MainMenuListAdapter;
import com.uofc.roomfinder.android.util.Constants;

public class MainMenu extends ListActivity {

	private ArrayList<MainMenuDataset> data = new ArrayList<MainMenuDataset>();

	final int MENUITEM_DIRECTION = 0;
	final int MENUITEM_SEARCH_ROOM = 1;
	final int MENUITEM_EXPLORE = 2;
	final int MENUITEM_CAMPUS_MAP = 3;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Translucent_NoTitleBar);
		setContentView(R.layout.menu_main);

		// fill data array list with menu data
		populateData();

		// link data and view
		MainMenuListAdapter adapter = new MainMenuListAdapter(this, R.layout.menu_main_list_item, R.id.title, data);
		setListAdapter(adapter);
		getListView().setTextFilterEnabled(true);
	}

	@Override
	public void onListItemClick(ListView parent, View v, int position, long id) {

		Intent nextScreen;

		switch (position) {
		case MENUITEM_DIRECTION:
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

}
