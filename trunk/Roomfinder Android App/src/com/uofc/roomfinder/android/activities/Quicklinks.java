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
import com.uofc.roomfinder.entities.Point3D;

public class Quicklinks extends ListActivity {

	// stores all the data for the list
	private ArrayList<QuicklinkDataset> data = new ArrayList<QuicklinkDataset>();

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Translucent_NoTitleBar);
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
		// create new intent with data to return (building name + room number, should look like 'ICT550')
		Intent newintent = new Intent();
		newintent.putExtra("roomX", data.get(position).getDestination().getX());
		newintent.putExtra("roomY", data.get(position).getDestination().getY());
		newintent.putExtra("roomZ", data.get(position).getDestination().getZ());
		newintent.putExtra("roomText", data.get(position).getTitle());

		// get parent intent
		if (getParent() == null) {
			setResult(Activity.RESULT_OK, newintent);
		} else {
			getParent().setResult(Activity.RESULT_OK, newintent);
		}

		// and quit this activity
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
		imageId = R.drawable.coffee;
		dataset = new QuicklinkDataset(imageId, title, new Point3D(700960.5776, 5662459.9725, 8.0));
		data.add(dataset);

		title = "Good Earth @ ICT";
		imageId = R.drawable.coffee;
		dataset = new QuicklinkDataset(imageId, title, new Point3D(700994.0805, 5662656.6906, 1));
		data.add(dataset);

		title = "Fitness Centre";
		imageId = R.drawable.runner;
		dataset = new QuicklinkDataset(imageId, title, new Point3D(700788.8957, 5662392.7682, 4));
		data.add(dataset);

		title = "Olympic Oval";
		imageId = R.drawable.runner;
		dataset = new QuicklinkDataset(imageId, title, new Point3D(700674.5955, 5662318.9493, 4));
		data.add(dataset);
		
		title = "Squash Court";
		imageId = R.drawable.runner;
		dataset = new QuicklinkDataset(imageId, title, new Point3D(700788.8957, 5662392.7682, 4));
		data.add(dataset);
		
		title = "Rogers Store";
		imageId = R.drawable.shopping;
		dataset = new QuicklinkDataset(imageId, title, new Point3D(701081.4262,5662560.9774, 4));
		data.add(dataset);
		

	}

}
