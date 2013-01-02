package org.nvh.hoofdpijndagboek;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class HeadachePreferences extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
