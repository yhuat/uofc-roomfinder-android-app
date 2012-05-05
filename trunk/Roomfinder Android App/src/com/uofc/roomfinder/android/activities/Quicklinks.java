package com.uofc.roomfinder.android.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import com.uofc.roomfinder.R;
import com.uofc.roomfinder.android.activities.adapter.QuicklinkDataset;
import com.uofc.roomfinder.android.activities.adapter.QuicklinksListAdapter;

public class Quicklinks extends ListActivity {

	// stores all the data for the list
	private ArrayList<QuicklinkDataset> data = new ArrayList<QuicklinkDataset>();

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Light_NoTitleBar);
		setContentView(R.layout.quicklinks_list);

		// fill data structure with data
		populateLists();

		// set adapter
		QuicklinksListAdapter adapter = new QuicklinksListAdapter(this, R.layout.quicklinks_list, R.id.title, data);
		setListAdapter(adapter);
		getListView().setTextFilterEnabled(true);
	}

	@Override
	public void onListItemClick(ListView parent, View v, int position, long id) {
		//create new intent with data to return (building name + room number, should look like 'ICT550')
		Intent newintent = new Intent();
		newintent.putExtra("room", data.get(position).getDestBuilding()+data.get(position).getDestRoom());
		
		//get parent intent
		if (getParent() == null) {
		    setResult(Activity.RESULT_OK, newintent);
		} else {
		    getParent().setResult(Activity.RESULT_OK, newintent);
		}
		
		//and quit this activity
		finish();		
	}

	/**
	 * helper method to fill the lists with data
	 */
	private void populateLists() {
		String title;
		int imageId;
		QuicklinkDataset dataset;

		title = "Tim Horton's @ MacHall";
		imageId = R.drawable.plus;
		dataset = new QuicklinkDataset(imageId, title, "MH", "214");
		data.add(dataset);

		title = "Good Earth @ ICT";
		imageId = R.drawable.plus;
		dataset = new QuicklinkDataset(imageId, title, "MH", "214");
		data.add(dataset);

		title = "Fitness Centre";
		imageId = R.drawable.plus;
		dataset = new QuicklinkDataset(imageId, title, "MH", "214");
		data.add(dataset);

	}

}
