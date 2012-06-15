package com.uofc.roomfinder.android;

import android.app.Application;
import android.content.Context;

public class RoomFinderApplication extends Application {
	private static Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		RoomFinderApplication.context = getApplicationContext();
	}

	public static Context getAppContext() {
		return RoomFinderApplication.context;
	}
}
