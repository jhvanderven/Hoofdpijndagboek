package org.nvh.hoofdpijndagboek;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;

public class EditPillsActivity extends SherlockFragmentActivity {
	public void onCreate(Bundle savedInstanceState) {
		setTheme(MainActivity.THEME); // Used for theme switching in samples
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_stack);
		// Create the list fragment and add it as our sole content.
		if (getSupportFragmentManager().findFragmentById(android.R.id.content) == null) {
			PillListFragment list = new PillListFragment();
			getSupportFragmentManager().beginTransaction()
					.add(android.R.id.content, list).commit();
		}
	}

	public static class PillListFragment extends SherlockListFragment {

		private String medication = "";
		private List<String> pills = null;

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			registerForContextMenu(getListView());
			pills = Utils.getPills(getActivity());
			setListAdapter(new ArrayAdapter<String>(getActivity(),
					android.R.layout.simple_list_item_1, pills));
			setHasOptionsMenu(true);
		}

		@Override
		public void onCreateOptionsMenu(Menu menu,
				com.actionbarsherlock.view.MenuInflater inflater) {
			inflater.inflate(R.menu.pills_main_menu, menu);
			super.onCreateOptionsMenu(menu, inflater);
		}

		@Override
		public boolean onOptionsItemSelected(
				com.actionbarsherlock.view.MenuItem item) {
			if (item.getItemId() == R.id.menu_save) {
				// write back the pill list to the preferences
				Utils.saveAllMedication(pills, getActivity());
				return true;
			} else if (item.getItemId() == R.id.menu_add) {
				editMedicationName(-1, "");
			}
			return super.onOptionsItemSelected(item);
		}

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			// TODO Auto-generated method stub
			super.onCreateContextMenu(menu, v, menuInfo);
			MenuInflater inflater = getActivity().getMenuInflater();
			inflater.inflate(R.menu.medicin_item_menu, (android.view.Menu) menu);
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean onContextItemSelected(MenuItem item) {
			if (item.getItemId() == R.id.menu_edit) {
				final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
						.getMenuInfo();
				medication = (String) getListAdapter().getItem(info.position);
				editMedicationName(info.position, medication);
				return true;
			} else if (item.getItemId() == R.id.menu_delete) {
				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
						.getMenuInfo();
				pills.remove(info.position);
				((ArrayAdapter<String>) getListAdapter())
						.notifyDataSetChanged();
				return true;
			}
			return super.onContextItemSelected(item);
		}

		public void editMedicationName(final int position,
				String originalMedication) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(getActivity().getString(R.string.pill_new_message));

			// Set up the input
			final EditText input = new EditText(getActivity());
			input.setInputType(InputType.TYPE_CLASS_TEXT);
			input.setText(originalMedication);
			builder.setView(input);

			// Set up the buttons
			builder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							medication = input.getText().toString();
							if (position == -1) {
								pills.add(medication);
							} else {
								pills.set(position, medication);
							}
						}
					});
			builder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});

			builder.show();
		}
	}
}
