package com.uofc.roomfinder.android.activities;

import static com.uofc.roomfinder.android.util.Constants.*;
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
import android.widget.Spinner;


import com.uofc.roomfinder.R;
import com.uofc.roomfinder.android.DataModel;
import com.uofc.roomfinder.android.util.Constants;
import com.uofc.roomfinder.entities.Contact;
import com.uofc.roomfinder.entities.ContactList;
import com.uofc.roomfinder.util.UrlReader;

public class SearchActivity extends Activity {
	
	EditText inputSearch;
	ListView listView;
	Spinner spinnerImpedance;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.room_search_form_layout);

		inputSearch = (EditText) findViewById(R.id.EditSearch);
		listView = (ListView) findViewById(R.id.resultList);

		spinnerImpedance = (Spinner) findViewById(R.id.spinner_imdedance);
		Button btnFinishForm = (Button) findViewById(R.id.btnFinishForm);

		// Listening to button event
		btnFinishForm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {

				// if no text is set do nothing
				if (inputSearch.getText().toString().equals("")) {
					// TODO: add message to view: no search criteria found, for back use back button
					return;
				}

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
				System.out.println(spinnerImpedance.getSelectedItemPosition());
				intent.putExtra("impedance", getImpedanceAttributeBySpinnerID((int) spinnerImpedance.getSelectedItemId()));

				// send query to REST service (which querys the public UofC LDAP directory)
				final ContactList contacts = new ContactList(UrlReader.readFromURL(Constants.REST_CONTACTS_URL + inputSearch.getText().toString()));

				// 0 result -> display: no result found
				if (contacts.size() == 0) {
					List<String> values = new LinkedList<String>();
					values.add("no result found");
					String[] valuesStringArray = Arrays.copyOf(values.toArray(), values.toArray().length, String[].class);
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchActivity.this, R.layout.list_item_search_form, R.id.product_label,
							valuesStringArray);
					listView.setAdapter(adapter);
					// 1 result -> return it
				} else if (contacts.size() == 1) {
					// send data to next activity
					intent.putExtra("room", contacts.get(0).getRoomNumber().get(0));
					DataModel.getInstance().setDestinationText(contacts.get(0).getCommonName());
					setResult(RESULT_OK, intent);
					finish();
				} else {
					// if more results -> show result list
					List<String> values = new LinkedList<String>(); // list to display in list view

					// add contact data to values list
					for (Contact contact : contacts) {
						String strContact = "(";
						if (contact.getPreName() != null) {
							strContact += contact.getPreName();
							if (contact.getSurName() != null)
								strContact += " ";
						}
						if (contact.getSurName() != null)
							strContact += contact.getSurName();
						strContact += ")";
						values.add(contact.getRoomNumber().get(0) + strContact);
					}

					String[] valuesStringArray = Arrays.copyOf(values.toArray(), values.toArray().length, String[].class);

					// params of constructor: context, layout for the row, ID of the View to which the data is written, data
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchActivity.this, R.layout.list_item_search_form, R.id.product_label,
							valuesStringArray);
					listView.setAdapter(adapter);

					listView.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
							System.out.println(contacts.get(position).getPreName());
							System.out.println(contacts.get(position).getRoomNumber());

							System.out.println(position);
							intent.putExtra("room", contacts.get(position).getRoomNumber().get(0));
							DataModel.getInstance().setDestinationText(contacts.get(position).getCommonName());
							setResult(RESULT_OK, intent);
							finish();
						}
					});
				}
			}

		});

	}

	/**
	 * helper method to get the impedance attribute by spinner ID e.g. for '0' it returns 'Length' -> impedance attribute for shortest path
	 * 
	 * @param selectedItemId
	 *            selected spinner id
	 * @return impedance attribute
	 */
	private String getImpedanceAttributeBySpinnerID(int selectedItemId) {

		switch (selectedItemId) {
		case 0:
			return ROUTING_IMPEDANCE_SHORTEST_PATH;
		case 1:
			return ROUTING_IMPEDANCE_AVOID_INDOOR;
		case 2:
			return ROUTING_IMPEDANCE_AVOID_OUTDOOR;
		case 3:
			return ROUTING_IMPEDANCE_AVOID_STAIRS;

		default:
			return ROUTING_IMPEDANCE_SHORTEST_PATH;
		}
	}
}
