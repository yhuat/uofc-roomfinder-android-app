package com.uofc.roomfinder.android.activities;

import com.esri.arcgis.android.samples.attributequery.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class SearchActivity extends Activity {

	EditText inputRoom;
	Spinner inputBuilding;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.room_search_form_layout);

		inputRoom = (EditText) findViewById(R.id.EditTextRoom);
		inputBuilding = (Spinner) findViewById(R.id.SpinnerBuilding);
		Button btnFinishForm = (Button) findViewById(R.id.btnFinishForm);

		// Listening to button event
		btnFinishForm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Starting a new Intent (back to MapActivity)
				Intent intent = new Intent();

				// send data to next activity
				intent.putExtra("room", inputRoom.getText().toString());
				intent.putExtra("building", inputBuilding.getSelectedItem().toString());
				setResult(RESULT_OK, intent);

				// logging
				Log.e("SearchFormScreen", "room: " + inputRoom.getText() + " - building: " + inputBuilding.getSelectedItem().toString());

				finish();
			}
		});

	}
}
