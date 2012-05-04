package com.uofc.roomfinder.android.activities;

import java.util.Vector;

import com.uofc.roomfinder.R;
import com.uofc.roomfinder.android.activities.adapter.MainMenuDataset;
import com.uofc.roomfinder.android.activities.adapter.MainMenuListAdapter;
import com.uofc.roomfinder.android.util.Constants;
import android.app.ListActivity;
import android.content.Intent;
import android.net.ParseException;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class MainMenu extends ListActivity {

	private Vector<MainMenuDataset> data;
	MainMenuDataset rd;

	final int MENUITEM_DIRECTION = 0;
	final int MENUITEM_EXPLORE = 1;
	final int MENUITEM_SEARCH_BUILDING = 2;
	final int MENUITEM_CAMPUS_MAP = 3;
	final int MENUITEM_FRIENDS = 4;
	final int MENUITEM_SEARCH_ROOM = 0;

	static final String[] title = new String[] { "Directions", "Explore", "Find Building", "Campus Map", "Friends" };

	static final String[] detail = new String[] { "get a route from your position to destination", "discover the campus in another dimension",
			"search for a building", "map of the UofC campus", "last known position of your friends" };

	private Integer[] imgid = { R.drawable.search, R.drawable.search, R.drawable.search, R.drawable.search, R.drawable.search };

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Light_NoTitleBar);
		setContentView(R.layout.menu_main);

		data = new Vector<MainMenuDataset>();
		for (int i = 0; i < title.length; i++) {

			try {
				rd = new MainMenuDataset(imgid[i], title[i], detail[i]);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			data.add(rd);
		}
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
			startActivity(nextScreen);
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

		default:
			break;
		}

		Toast.makeText(getApplicationContext(), "You have selected " + (position + 1) + "th item", Toast.LENGTH_SHORT).show();
	}

}
