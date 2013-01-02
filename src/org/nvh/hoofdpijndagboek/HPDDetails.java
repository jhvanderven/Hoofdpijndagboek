package org.nvh.hoofdpijndagboek;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class HPDDetails extends SherlockFragmentActivity {
	int mStackLevel = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(MainActivity.THEME); // Used for theme switching in samples
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

		public static void pleaseUpdate(HeadacheAttack a, String[] weer,
				String[] humeur) {
			update(a, weer, humeur);
		}

		private static void update(HeadacheAttack a, String[] weer,
				String[] humeur) {
			if (view != null) {
				((CheckBox) view.findViewById(R.id.menstruatie))
						.setChecked(a.menstruatie);
				((CheckBox) view.findViewById(R.id.misselijk))
						.setChecked(a.misselijk);
				((CheckBox) view.findViewById(R.id.licht)).setChecked(a.licht);
				((CheckBox) view.findViewById(R.id.duizelig))
						.setChecked(a.duizelig);
				((CheckBox) view.findViewById(R.id.geur)).setChecked(a.geur);
				((CheckBox) view.findViewById(R.id.inslapen))
						.setChecked(a.inslapen);
				((CheckBox) view.findViewById(R.id.doorslapen))
						.setChecked(a.doorslapen);
				((CheckBox) view.findViewById(R.id.stoelgang))
						.setChecked(a.stoelgang);
				((Spinner) view.findViewById(R.id.spinnerHumeur))
						.setSelection(Utils.getArrayIndex(humeur, a.humeur));
				((Spinner) view.findViewById(R.id.spinnerWeer))
						.setSelection(Utils.getArrayIndex(weer, a.weer));
			}
		}

		@Override
		public void onResume() {
			HeadacheAttack a = ((MainActivity) getActivity()).getAttack();
			update(a,
					getActivity().getResources().getStringArray(
							R.array.weather_array), getActivity()
							.getResources()
							.getStringArray(R.array.humeur_array));
			super.onResume();
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = inflater
					.inflate(R.layout.details_layout, container, false);
			view = v;
			final Spinner weer = (Spinner) v.findViewById(R.id.spinnerWeer);
			ArrayAdapter<CharSequence> adapter = ArrayAdapter
					.createFromResource(getActivity(), R.array.weather_array,
							android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			weer.setAdapter(adapter);
			weer.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View v, int position,
						   long id) {
					((MainActivity)getActivity()).getAttack().weer = (String) parent.getItemAtPosition(position);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					((MainActivity)getActivity()).getAttack().weer = "";
				}
			});
			final Spinner humeur = (Spinner) v.findViewById(R.id.spinnerHumeur);
			adapter = ArrayAdapter.createFromResource(getActivity(),
					R.array.humeur_array, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			humeur.setAdapter(adapter);
			humeur.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View v, int position,
						   long id) {
					((MainActivity)getActivity()).getAttack().humeur = (String) parent.getItemAtPosition(position);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					((MainActivity)getActivity()).getAttack().humeur = "";
				}
			});
			final CheckBox menstruatie = (CheckBox) v.findViewById(R.id.menstruatie);
			menstruatie.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					((MainActivity)getActivity()).getAttack().menstruatie=isChecked;
				}
			});
			final CheckBox misselijk = (CheckBox) v.findViewById(R.id.misselijk);
			misselijk.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					((MainActivity)getActivity()).getAttack().misselijk=isChecked;
				}
			});
			final CheckBox licht = (CheckBox) v.findViewById(R.id.licht);
			licht.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					((MainActivity)getActivity()).getAttack().licht=isChecked;
				}
			});
			final CheckBox duizelig = (CheckBox) v.findViewById(R.id.duizelig);
			duizelig.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					((MainActivity)getActivity()).getAttack().duizelig=isChecked;
				}
			});
			final CheckBox geur = (CheckBox) v.findViewById(R.id.geur);
			geur.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					((MainActivity)getActivity()).getAttack().geur=isChecked;
				}
			});
			final CheckBox inslapen = (CheckBox) v.findViewById(R.id.inslapen);
			inslapen.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					((MainActivity)getActivity()).getAttack().inslapen=isChecked;
				}
			});
			final CheckBox doorslapen = (CheckBox) v.findViewById(R.id.doorslapen);
			doorslapen.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					((MainActivity)getActivity()).getAttack().doorslapen=isChecked;
				}
			});
			final CheckBox stoelgang = (CheckBox) v.findViewById(R.id.stoelgang);
			stoelgang.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					((MainActivity)getActivity()).getAttack().stoelgang=isChecked;
				}
			});
			return v;
		}
	}
}
