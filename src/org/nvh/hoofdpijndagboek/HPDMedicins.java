package org.nvh.hoofdpijndagboek;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

public class HPDMedicins extends SherlockFragmentActivity {
    private ArrayAdapter<String> mAdapter;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Must add the progress bar to the root of the layout
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
 
        mAdapter = new ArrayAdapter<String>(this, R.layout.medicin_row, new String[]{"one", "two","three"});
//        setListAdapter(mAdapter);
    }

}
