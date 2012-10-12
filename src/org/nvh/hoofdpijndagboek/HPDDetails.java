package org.nvh.hoofdpijndagboek;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class HPDDetails extends SherlockFragmentActivity {
    int mStackLevel = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(MainActivity.THEME); //Used for theme switching in samples
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_stack);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("level", mStackLevel);
    }

    public static class SymptomsFragment extends SherlockFragment {
    	private static View view;
    	
    	@Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.details_layout, container, false);
            view=v;
            Spinner spinner = (Spinner) v.findViewById(R.id.spinnerWeer);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.weather_array,
    				android.R.layout.simple_spinner_item);
    		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    		spinner.setAdapter(adapter);
    
    		spinner = (Spinner) v.findViewById(R.id.spinnerHumeur);
    		adapter = ArrayAdapter.createFromResource(getActivity(), R.array.humeur_array,
    				android.R.layout.simple_spinner_item);
    		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    		spinner.setAdapter(adapter);
            return v;
        }
        
        public static String getData(){
        	StringBuilder sb = new StringBuilder();
            Spinner spinner = (Spinner) view.findViewById(R.id.spinnerWeer);
        	sb.append(view.getContext().getString(R.string.weer)).append(":").append(spinner.getSelectedItem().toString()).append("\n");
    		spinner = (Spinner) view.findViewById(R.id.spinnerHumeur);
        	sb.append(view.getContext().getString(R.string.humeur)).append(":").append(spinner.getSelectedItem().toString()).append("\n");
        	return sb.toString();
        }
    }
}
