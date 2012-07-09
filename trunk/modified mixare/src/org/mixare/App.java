package org.mixare;

import android.content.Context;

public class App extends android.app.Application {
	private static Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		App.context = getApplicationContext();
	}

	public static Context getAppContext() {
		return App.context;
	}
}