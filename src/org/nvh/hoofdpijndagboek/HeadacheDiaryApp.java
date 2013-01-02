package org.nvh.hoofdpijndagboek;

import android.app.Application;

// This class is generated for the sole purpose to access the SharedPreferences from any part of the application. Also requires a modification to the manifest
public class HeadacheDiaryApp extends Application {

	private static Application app;

	@Override
	public void onCreate() {
		app = this;
		super.onCreate();
	}

	public static Application getApp() {
		return app;
	}
}
