package org.nvh.hoofdpijndagboek;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
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
			v.setTag(R.layout.details_layout);
			view = v;
			Spinner spinner = (Spinner) v.findViewById(R.id.spinnerWeer);
			ArrayAdapter<CharSequence> adapter = ArrayAdapter
					.createFromResource(getActivity(), R.array.weather_array,
							android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter);

			spinner = (Spinner) v.findViewById(R.id.spinnerHumeur);
			adapter = ArrayAdapter.createFromResource(getActivity(),
					R.array.humeur_array, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter);
			return v;
		}

		public static String getData() {
			StringBuilder sb = new StringBuilder();
			if (view != null) {
				Spinner spinner = (Spinner) view.findViewById(R.id.spinnerWeer);
				sb.append(view.getContext().getString(R.string.weer))
						.append(":")
						.append(spinner.getSelectedItem().toString())
						.append("\n");
				spinner = (Spinner) view.findViewById(R.id.spinnerHumeur);
				sb.append(view.getContext().getString(R.string.humeur))
						.append(":")
						.append(spinner.getSelectedItem().toString())
						.append("\n");
				sb.append(handleCheckBox(R.id.menstruatie, R.string.menstruatie));
				sb.append(handleCheckBox(R.id.misselijk, R.string.misselijk));
				sb.append(handleCheckBox(R.id.licht, R.string.licht));
				sb.append(handleCheckBox(R.id.duizelig, R.string.duizelig));
				sb.append(handleCheckBox(R.id.geur, R.string.geur));
				sb.append(handleCheckBox(R.id.inslapen, R.string.inslapen));
				sb.append(handleCheckBox(R.id.doorslapen, R.string.doorslapen));
				sb.append(handleCheckBox(R.id.stoelgang, R.string.stoelgang));
			}
			return sb.toString();
		}

		private static String handleCheckBox(int cbId, int stringId) {
			CheckBox cb = (CheckBox) view.findViewById(cbId);
			if (cb.isChecked()) {
				StringBuilder sb = new StringBuilder();
				sb.append(view.getContext().getString(stringId)).append(":")
						.append(view.getContext().getString(R.string.ja))
						.append("\n");
				return sb.toString();
			}

			return "";
		}
	}
}
