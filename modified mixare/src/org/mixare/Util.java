package org.mixare;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Util {

	/**
	 * loads the user name out of the shared preferences
	 * 
	 * @return user name
	 */
	public static String loadUsername(MixContext context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String userName = preferences.getString("user_name", null);
		return userName;
	}

}
