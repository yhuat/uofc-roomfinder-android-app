package org.mixare;

import org.mixare.R;
import org.mixare.data.UrlReader;

import android.os.AsyncTask;
import android.widget.Toast;

public class AddGraffiti extends AsyncTask<String, Void, String> {

	@Override
	protected void onPreExecute() {
	}

	/**
	 * add graffiti via REST
	 * 
	 * @param queryParams
	 *            : 1st: text, 2nd: user name, lat, long
	 * 
	 * @return resultSet of query
	 */
	@Override
	protected String doInBackground(String... params) {

		// exit condition
		if (params == null || params[1] == null || params.length < 4)
			return null;

		String queryUrl = DataView.REST_ADD_GRAFFITI_URL + "?text=" + params[0] + "&user=" + params[1] + "&lat=" + params[2] + "&long=" + params[3];
		System.out.println(queryUrl);
		System.out.println(queryUrl);
		System.out.println(queryUrl);
		System.out.println(queryUrl);
		System.out.println(queryUrl);
		return UrlReader.readFromURL(queryUrl);
	}

	@Override
	protected void onPostExecute(String result) {
		// if there is an result
		if (result != null) {
			// handle result
		}
	}
}
