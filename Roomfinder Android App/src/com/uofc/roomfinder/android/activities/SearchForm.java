package com.uofc.roomfinder.android.activities;

import static com.uofc.roomfinder.android.util.Constants.ROUTING_IMPEDANCE_AVOID_INDOOR;
import static com.uofc.roomfinder.android.util.Constants.ROUTING_IMPEDANCE_AVOID_OUTDOOR;
import static com.uofc.roomfinder.android.util.Constants.ROUTING_IMPEDANCE_AVOID_STAIRS;
import static com.uofc.roomfinder.android.util.Constants.ROUTING_IMPEDANCE_SHORTEST_PATH;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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

/**
 * this activity displays a search form
 * 
 * it queries the public LDAP directory
 * 
 * this activity should only be called with "startActivityForResult" from the parent activity, then it returns the result to the called intent the return
 * parameter names are "room" and "impedance"
 * 
 * @author benjaminlautenschlaeger
 * 
 */
public class SearchForm extends Activity {

	EditText inputSearch;
	ListView listView;
	Spinner spinnerImpedance;
	ContactList contacts;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_form);

		// input views
		inputSearch = (EditText) findViewById(R.id.EditSearch);
		spinnerImpedance = (Spinner) findViewById(R.id.spinner_imdedance);
		Button btnFinishForm = (Button) findViewById(R.id.btnFinishForm);

		// Listening to button event
		btnFinishForm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {

				// if no text is set do nothing
				if (inputSearch.getText().toString().equals("")) {
					return;
				}

				// threaded contact query
				new LoadContactsTask().execute(inputSearch.getText().toString());
			}
		});
	}

	/**
	 * gets the parent intent, sets data to it and closes this intent
	 * 
	 * return data -> building name + room number, should look like 'ICT550'
	 */
	private void returnToParentIntent(String buildingAndRoom) {

		Intent intent = new Intent();

		// room data from method param
		intent.putExtra("room", buildingAndRoom);

		// impedance from spinner
		intent.putExtra("impedance", getImpedanceAttributeBySpinnerID((int) spinnerImpedance.getSelectedItemId()));

		// get parent intent to pass the queried data back to it
		if (getParent() == null) {
			setResult(Activity.RESULT_OK, intent);
		} else {
			getParent().setResult(Activity.RESULT_OK, intent);
		}

		// and quit this activity
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// received data
		Intent newintent = data;
		int receivedData = Integer.parseInt(newintent.getStringExtra("selectedItem"));

		// return to parent view
		DataModel.getInstance().setDestinationText(contacts.get(receivedData).getCommonName());
		returnToParentIntent(contacts.get(receivedData).getRoomNumber().get(0));
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

	/**
	 * async tasks which loads the contact results (in "do in background")
	 * 
	 * and handles the result list (in "on post execute")
	 * 
	 * @author benjaminlautenschlaeger
	 * 
	 */
	private class LoadContactsTask extends AsyncTask<String, Void, ContactList> {
		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(SearchForm.this, "loading", "downloading contacts");

		}

		@Override
		protected ContactList doInBackground(String... params) {
			// send query to REST service (which querys the public UofC LDAP directory)
			ContactList contacts = new ContactList(UrlReader.readFromURL(Constants.REST_CONTACTS_URL + params[0]));
			return contacts;
		}

		@Override
		protected void onPostExecute(ContactList contacts) {
			SearchForm.this.contacts = contacts;

			// 0 result -> display: no result found
			if (contacts.size() == 0) {
				Toast.makeText(SearchForm.this, "no result found", Toast.LENGTH_LONG).show();

				/*
				 * List<String> values = new LinkedList<String>(); values.add("no result found"); String[] valuesStringArray = Arrays.copyOf(values.toArray(),
				 * values.toArray().length, String[].class); ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchForm.this,
				 * R.layout.list_item_search_form, R.id.product_label, valuesStringArray); listView.setAdapter(adapter); // 1 result -> return it
				 */
			} else if (contacts.size() == 1) {
				// only one result -> close search form

				// dismiss keyboard
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(inputSearch.getWindowToken(), 0);

				// return to parent view
				DataModel.getInstance().setDestinationText(contacts.get(0).getCommonName());
				returnToParentIntent(contacts.get(0).getRoomNumber().get(0));

			} else {
				// multiple results -> show list

				// dismiss keyboard
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(inputSearch.getWindowToken(), 0);

				// create list intent
				Intent resultListIntent = new Intent(getApplicationContext(), SearchResultList.class);

				ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

				// add contact data to values list, to pass it to new intent
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

					// each row in result list has three lines
					row.put("first", firstLine);
					row.put("second", secondLine);
					row.put("third", thirdLine);

					list.add(row);
				}
				resultListIntent.putExtra("data", list);
				startActivityForResult(resultListIntent, 1); // start only for result
			}

			dialog.dismiss();
		}
	}
}
