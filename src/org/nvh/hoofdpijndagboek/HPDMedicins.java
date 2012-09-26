package org.nvh.hoofdpijndagboek;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class HPDMedicins extends SherlockFragmentActivity {

	public void onCreate(Bundle savedInstanceState) {
        setTheme(MainActivity.THEME); //Used for theme switching in samples
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_stack);
	}

	public static class MedicinFragment extends SherlockFragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.medicins_layout, container, false);
			return v;
		}
    }
}
