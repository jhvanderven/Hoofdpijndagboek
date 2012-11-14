package org.nvh.hoofdpijndagboek;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.MenuItem;

public class HPDMedicins extends SherlockFragmentActivity {

	public void onCreate(Bundle savedInstanceState) {
		setTheme(MainActivity.THEME); // Used for theme switching in samples
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_stack);
		// Create the list fragment and add it as our sole content.
		if (getSupportFragmentManager().findFragmentById(android.R.id.content) == null) {
			ArrayListFragment list = new ArrayListFragment();
			getSupportFragmentManager().beginTransaction()
					.add(android.R.id.content, list).commit();
		}
	}

	public static class ArrayListFragment extends SherlockListFragment {
		List<String> items = new ArrayList<String>();
		String uriBaseForCalendar;

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			uriBaseForCalendar = Utils.getCalendarUriBase(getActivity()
					.getContentResolver());
			getPillsFromCalendar();
			registerForContextMenu(getListView());
			setHasOptionsMenu(true);
		}

		public void getPillsFromCalendar() {
			// remove the items in the list
			items.clear();
			Calendar eergisteren = Calendar.getInstance();
			eergisteren.add(Calendar.DAY_OF_MONTH, -2);
			ContentResolver contentResolver = getActivity()
					.getContentResolver();
			Uri.Builder builder = Uri.parse(
					uriBaseForCalendar + "instances/when/").buildUpon();
			ContentUris.appendId(builder, eergisteren.getTimeInMillis());
			ContentUris.appendId(builder, Calendar.getInstance()
					.getTimeInMillis());

			Cursor eventCursor = contentResolver.query(builder.build(),
					new String[] { "title", "begin", "description" }, null,
					null, "startDay ASC, startMinute ASC");
			if (eventCursor.getCount() > 0) {
				while (eventCursor.moveToNext()) {
					String title = eventCursor.getString(0);
					if (title.equals(getString(R.string.calendar_pill_title))) {
						items.add(eventCursor.getString(2)
								+ getString(R.string.pill_timing)
								+ ":"
								+ new SimpleDateFormat(
										getString(R.string.very_long_date_time))
										.format(eventCursor.getLong(1)));
					}
				}
			}

			eventCursor.close();

			setListAdapter(new MedicinArrayAdapter(getActivity(), items));
		}

		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			Log.i("FragmentList", "Item clicked: " + id);
		}

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			super.onCreateContextMenu(menu, v, menuInfo);
			MenuInflater inflater = getActivity().getMenuInflater();
			inflater.inflate(R.menu.medicin_item_menu, (android.view.Menu) menu);
		}

		@Override
		public boolean onContextItemSelected(android.view.MenuItem item) {
			if (item.getItemId() == R.id.menu_edit) {
				// open the dialog again
				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
						.getMenuInfo();
				String description = (String) getListAdapter().getItem(
						info.position);
				String name = Utils.parseDescription(description, getActivity()
						.getString(R.string.pill_name));
				String number = Utils.parseDescription(description,
						getActivity().getString(R.string.pill_number));
				String effect = Utils.parseDescription(description,
						getActivity().getString(R.string.pill_effect));
				String when = Utils.parseDescription(description, getActivity()
						.getString(R.string.pill_timing));
				Bundle b = new Bundle();
				b.putString("medicin", name);
				b.putString("count", number);
				b.putString("effect", effect);
				b.putString("when", when);
				DialogFragment dialog = new NewPillDialogFragment(this);
				dialog.setArguments(b);
				dialog.show(getActivity().getSupportFragmentManager(),
						"newPillDialogFragment");

				return true;
			} else if (item.getItemId() == R.id.menu_delete) {
				// remove from list and calendar
				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
						.getMenuInfo();
				String description = (String) getListAdapter().getItem(
						info.position);
				String when = Utils.parseDescription(description, getActivity()
						.getString(R.string.pill_timing));
				try {
					Utils.deleteFromCalendar(
							getActivity().getContentResolver(), getActivity()
									.getString(R.string.calendar_pill_title),
							Utils.parse(
									when,
									getActivity().getString(
											R.string.very_long_date_time)));
					getPillsFromCalendar();
				} catch (ParseException e) {
				}
				return true;
			}
			return super.onContextItemSelected(item);
		}

		@Override
		public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu,
				com.actionbarsherlock.view.MenuInflater inflater) {
			inflater.inflate(R.menu.medicin_main_menu, menu);
			super.onCreateOptionsMenu(menu, inflater);
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			switch (item.getItemId()) {
			case R.id.menu_add:
				DialogFragment dialog = new NewPillDialogFragment(this);
				dialog.show(getActivity().getSupportFragmentManager(),
						"newPillDialogFragment");

				return true;
			}
			return super.onOptionsItemSelected(item);
		}

	}
}
