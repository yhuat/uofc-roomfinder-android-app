package com.uofc.roomfinder.android.activities;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.esri.arcgis.android.samples.attributequery.R;
import com.uofc.roomfinder.android.util.Constants;
import com.uofc.roomfinder.entities.Contact;
import com.uofc.roomfinder.entities.ContactList;
import com.uofc.roomfinder.util.UrlReader;

public class SearchActivity extends Activity {

	EditText inputSearch;
	ListView listView;

	// Spinner inputBuilding;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.room_search_form_layout);

		inputSearch = (EditText) findViewById(R.id.EditSearch);
		listView = (ListView) findViewById(R.id.resultList);

		// inputBuilding = (Spinner) findViewById(R.id.SpinnerBuilding);
		Button btnFinishForm = (Button) findViewById(R.id.btnFinishForm);

		// Listening to button event
		btnFinishForm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {

				// TODO: remove in final version. url has to be accessed in an async thread
				try {
					Class<?> strictModeClass = Class.forName("android.os.StrictMode");
					Class<?> strictModeThreadPolicyClass = Class.forName("android.os.StrictMode$ThreadPolicy");
					Object laxPolicy = strictModeThreadPolicyClass.getField("LAX").get(null);
					Method method_setThreadPolicy = strictModeClass.getMethod("setThreadPolicy", strictModeThreadPolicyClass);
					method_setThreadPolicy.invoke(null, laxPolicy);
				} catch (Exception e) {
					e.printStackTrace();
				}

				// Starting a new Intent (back to MapActivity)
				final Intent intent = new Intent();

				// send query to REST service (which querys the public UofC LDAP directory)
				final ContactList contacts = new ContactList(UrlReader.readFromURL(Constants.REST_CONTACTS_URL + inputSearch.getText().toString()));

				// 1 result -> return it
				if (contacts.size() == 1) {
					// send data to next activity
					intent.putExtra("room", contacts.get(0).getRoomNumber().get(0));
					setResult(RESULT_OK, intent);
					finish();
				} else {
					// if more results -> show result list
					List<String> values = new LinkedList<String>(); // list to display in list view

					// add contact data to values list
					for (Contact contact : contacts) {
						values.add(contact.getRoomNumber().get(0) + " (" + contact.getPreName() + " " + contact.getSurName() + ")");
					}

					String[] valuesStringArray = Arrays.copyOf(values.toArray(), values.toArray().length, String[].class);

					// params of constructor: context, layout for the row, ID of the View to which the data is written, data
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchActivity.this, R.layout.list_item_search_form, R.id.product_label,
							valuesStringArray);
					listView.setAdapter(adapter);

					listView.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
							// System.out.println(contacts.get(position).getRoomNumber());
							intent.putExtra("room", contacts.get(position).getRoomNumber().get(0));
							setResult(RESULT_OK, intent);
							finish();
						}
					});
				}
			}
		});

	}
}
