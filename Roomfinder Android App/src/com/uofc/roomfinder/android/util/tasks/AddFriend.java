package com.uofc.roomfinder.android.util.tasks;

import org.mixare.R;

import android.os.AsyncTask;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.uofc.roomfinder.android.DataModel;
import com.uofc.roomfinder.android.RoomFinderApplication;
import com.uofc.roomfinder.android.util.Constants;
import com.uofc.roomfinder.entities.Point3D;
import com.uofc.roomfinder.util.UrlReader;

public class AddFriend extends AsyncTask<String, Void, String> {

	String userName = null;
	String friendsName = null;

	@Override
	protected void onPreExecute() {
	}

	/**
	 * add friend via REST
	 * 
	 * @param queryParams
	 *            : 1st: own user name, 2nd: friend's name
	 * 
	 * @return resultSet of query
	 */
	@Override
	protected String doInBackground(String... params) {

		// exit condition
		if (params == null || params[1] == null || params.length < 2)
			return null;

		userName = params[0];
		friendsName = params[1];

		String queryUrl = Constants.REST_ADD_FRIEND_URL + "?user_name=" + userName + "&friend_name=" + friendsName;
		return UrlReader.readFromURL(queryUrl);
	}

	@Override
	protected void onPostExecute(String result) {
		// if there is an result
		if (result != null) {
			if (result.equals("success"))
				Toast.makeText(RoomFinderApplication.getAppContext(), "friend added: \"" + friendsName + "\"", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(RoomFinderApplication.getAppContext(), "error adding: \"" + friendsName + "\"", Toast.LENGTH_SHORT).show();
		}
	}
}
