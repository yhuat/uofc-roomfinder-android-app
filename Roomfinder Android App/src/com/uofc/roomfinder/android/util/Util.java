package com.uofc.roomfinder.android.util;

import com.uofc.roomfinder.android.RoomFinderApplication;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Util {

	/**
	 * saves a user name to the android preferences as a global variable
	 * 
	 * @param value
	 *            user name
	 */
	public static void saveUsername(String value) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RoomFinderApplication.getAppContext());
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("user_name", value);
		editor.commit();
	}

	/**
	 * loads the user name out of the shared preferences
	 * 
	 * @return user name
	 */
	public static String loadUsername() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RoomFinderApplication.getAppContext());
		String userName = preferences.getString("user_name", null);
		return userName;
	}

}
