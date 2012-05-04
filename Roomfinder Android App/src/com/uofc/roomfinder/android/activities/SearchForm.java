package com.uofc.roomfinder.android.activities;

import static com.uofc.roomfinder.android.util.Constants.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.uofc.roomfinder.R;
import com.uofc.roomfinder.android.DataModel;
import com.uofc.roomfinder.android.util.Constants;
import com.uofc.roomfinder.entities.Contact;
import com.uofc.roomfinder.entities.ContactList;
import com.uofc.roomfinder.util.UrlReader;

public class SearchForm extends Activity {

	EditText inputSearch;
	ListView listView;
	Spinner spinnerImpedance;
	ContactList contacts;
	Intent resultIntent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_form);

		inputSearch = (EditText) findViewById(R.id.EditSearch);

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

				// Starting a new Intent (MapActivity)
				resultIntent = new Intent(getApplicationContext(), MapActivity.class);
				System.out.println("imp: " + getImpedanceAttributeBySpinnerID((int) spinnerImpedance.getSelectedItemId()));
				SearchForm.this.resultIntent.putExtra("impedance", getImpedanceAttributeBySpinnerID((int) spinnerImpedance.getSelectedItemId()));

				// send query to REST service (which querys the public UofC LDAP directory)
				final ContactList contacts = new ContactList(UrlReader.readFromURL(Constants.REST_CONTACTS_URL + inputSearch.getText().toString()));
				SearchForm.this.contacts = contacts;

				// 0 result -> display: no result found
				if (contacts.size() == 0) {
					Toast.makeText(SearchForm.this, "no result found", Toast.LENGTH_LONG).show();

					/*
					 * List<String> values = new LinkedList<String>(); values.add("no result found"); String[] valuesStringArray =
					 * Arrays.copyOf(values.toArray(), values.toArray().length, String[].class); ArrayAdapter<String> adapter = new
					 * ArrayAdapter<String>(SearchForm.this, R.layout.list_item_search_form, R.id.product_label, valuesStringArray);
					 * listView.setAdapter(adapter); // 1 result -> return it
					 */
				} else if (contacts.size() == 1) {
					// dismiss keyboard
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(inputSearch.getWindowToken(), 0);

					// send data to next activity
					SearchForm.this.resultIntent.putExtra("room", contacts.get(0).getRoomNumber().get(0));
					DataModel.getInstance().setDestinationText(contacts.get(0).getCommonName());

					startActivity(resultIntent);

				} else {

					// dismiss keyboard
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(inputSearch.getWindowToken(), 0);

					Intent resultListIntent = new Intent(getApplicationContext(), SearchResultList.class);

					ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

					// add contact data to values list
					for (Contact contact : contacts) {
						HashMap<String, String> row = new HashMap<String, String>();

						String strContact = contact.getCommonName();

						String firstLine = strContact;
						String secondLine = "";
						String thirdLine = contact.getRoomNumber().get(0);

						if (contact.getTelephoneNumbers().size() > 0) {
							secondLine += "tel: " + contact.getTelephoneNumbers().get(0);

							if (contact.getEmails().size() > 0) {
								secondLine += " / email: " + contact.getEmails().get(0);
							}
						}

						row.put("first", firstLine);
						row.put("second", secondLine);
						row.put("third", thirdLine);

						list.add(row);
					}

					resultListIntent.putExtra("data", list);
					startActivityForResult(resultListIntent, 1); // start only for result
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Receiving the Data
		Intent newintent = data;

		int receivedData = Integer.parseInt(newintent.getStringExtra("selectedItem"));
		System.out.println("received: " + receivedData);

		SearchForm.this.resultIntent.putExtra("room", contacts.get(receivedData).getRoomNumber().get(0));
		DataModel.getInstance().setDestinationText(contacts.get(0).getCommonName());

		startActivity(resultIntent);
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
