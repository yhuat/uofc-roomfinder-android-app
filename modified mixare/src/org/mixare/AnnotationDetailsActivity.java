package org.mixare;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.content.Intent;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class AnnotationDetailsActivity extends ListActivity {

	static final ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

	@Override
	@SuppressWarnings("unchecked")
	protected void onCreate(android.os.Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.anno_details);

		SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.anno_details_list_item, new String[] { "first", "second", "third" }, new int[] {
				R.id.first_line, R.id.second_line, R.id.third_line }

		);

		System.out.println(getIntent().getSerializableExtra("data"));
		list.clear();
		list.addAll((ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("data"));

		setListAdapter(adapter);

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
//
//		// return the selected index to search form
//		final Intent intent = new Intent();
//		intent.putExtra("selectedItem", "" + position);
//		setResult(RESULT_OK, intent);
//		finish();
	}

}
