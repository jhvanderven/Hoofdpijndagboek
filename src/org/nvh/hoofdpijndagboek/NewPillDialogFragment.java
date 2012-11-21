package org.nvh.hoofdpijndagboek;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.nvh.hoofdpijndagboek.HPDMedicins.ArrayListFragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SlidingDrawer;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class NewPillDialogFragment extends DialogFragment {

	ArrayAdapter<String> spinnerArrayAdapter;
	Spinner pills;
	Spinner pillEffect;
	Button pillDate;
	Button pillTime;
	int year, month, day, hour, minute;
	TextView pillNumber;
	ArrayListFragment alf;
	String baseUriForCalendar;

	public NewPillDialogFragment() {
		alf = null;
	}

	public NewPillDialogFragment(ArrayListFragment arrayListFragment) {
		alf = arrayListFragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		baseUriForCalendar = Utils.getCalendarUriBase(getActivity()
				.getContentResolver());
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.enter_pill, null);

		// get the widgets
		pills = (Spinner) v.findViewById(R.id.spinner_pill_name);
		pillEffect = (Spinner) v.findViewById(R.id.pill_effect);
		pillTime = (Button) v.findViewById(R.id.pill_timing_hour);
		pillDate = (Button) v.findViewById(R.id.pill_timing_day);
		pillNumber = (TextView) v.findViewById(R.id.pill_number);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				getActivity(), R.array.pill_effect_array,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		pillEffect.setAdapter(adapter);
		Calendar now = Calendar.getInstance();
		year = now.get(Calendar.YEAR);
		month = now.get(Calendar.MONTH);
		day = now.get(Calendar.DAY_OF_MONTH);
		hour = now.get(Calendar.HOUR_OF_DAY);
		minute = now.get(Calendar.MINUTE);

		pills.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				Spinner spinner = (Spinner) arg0
						.findViewById(R.id.spinner_pill_name);
				String choice = spinner.getSelectedItem().toString();
				if (choice.equalsIgnoreCase(getString(R.string.new_pill))) {
					// user must enter a new pill
					AlertDialog.Builder alert = new AlertDialog.Builder(
							getActivity());

					alert.setTitle(getString(R.string.pill_new_medicin));
					alert.setMessage(getString(R.string.pill_new_message));

					// Set an EditText view to get user input
					final EditText input = new EditText(getActivity());
					alert.setView(input);

					alert.setPositiveButton(getString(R.string.dialogOk),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									String medication = input.getText()
											.toString();
									Utils.saveMedication(medication,
											getActivity());
									spinnerArrayAdapter.add(medication);
									pills.setSelection(spinnerArrayAdapter
											.getPosition(medication));
								}

							});

					alert.setNegativeButton(getString(R.string.dialogCancel),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// Canceled.
								}
							});

					alert.show();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_dropdown_item);
		List<String> pillNames = Utils.getPills(getActivity());

		Bundle b = getArguments();
		int position = 0;
		int selectedPos = 0;
		for (String p : pillNames) {
			spinnerArrayAdapter.add(p);
			if (b != null) {
				if (p.equalsIgnoreCase(b.getString("medicin"))) {
					selectedPos = position;
				}
				position++;
			}
		}

		pills.setAdapter(spinnerArrayAdapter);
		if (b != null) {
			pills.setSelection(selectedPos);
			pillNumber.setText(b.getString("count"));
			pillEffect.setSelection(Utils.getArrayIndex(getResources()
					.getStringArray(R.array.pill_effect_array), b
					.getString("effect")));
			try {
				Date savedDate = Utils.parse(b.getString("when"),
						getString(R.string.very_long_date_time));
				Calendar c = Calendar.getInstance();
				c.setTime(savedDate);
				year = c.get(Calendar.YEAR);
				month = c.get(Calendar.MONTH);
				day = c.get(Calendar.DAY_OF_MONTH);
				hour = c.get(Calendar.HOUR_OF_DAY);
				minute = c.get(Calendar.MINUTE);
				pillDate.setText(new SimpleDateFormat(
						getString(R.string.short_date_time)).format(savedDate));
			} catch (ParseException pe) {
				Toast.makeText(getActivity(), R.string.date_parse_error,
						Toast.LENGTH_SHORT).show();
			}
		} else {
			if (spinnerArrayAdapter.getItem(0).equalsIgnoreCase(
					getActivity().getString(R.string.new_pill))) {
				pills.setSelection(spinnerArrayAdapter.getCount() - 1);
			} else {
				pills.setSelection(0);
			}
			Date d = new Date(now.getTimeInMillis());
			pillDate.setText(new SimpleDateFormat(
					getString(R.string.short_date_time)).format(d));
			pillNumber.setText("1");
		}
		pillTime.setText(String.format("%02d:%02d", hour, minute));

		pillTime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TimePickerDialog tpd = new TimePickerDialog(getActivity(),
						new TimePickHandler(), hour, minute, true);
				tpd.show();

			}
		});
		pillDate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				DatePickerDialog dpd = new DatePickerDialog(getActivity(),
						new DatePickHandler(), year, month, day);
				dpd.show();

			}
		});
		builder.setView(v)
				.setTitle(R.string.menu_add)
				.setNegativeButton(getString(R.string.dialogCancel), null)
				.setPositiveButton(getString(R.string.dialogOk),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								Bundle b = getArguments();
								if (b != null) {
									// we have to delete the event identified by
									// the title and the date
									try {
										Utils.deleteFromCalendar(
												getActivity()
														.getContentResolver(),
												getActivity()
														.getString(
																R.string.calendar_pill_title),
												Utils.parse(
														b.getString("when"),
														getActivity()
																.getString(
																		R.string.very_long_date_time)));
									} catch (ParseException e) {
										Toast.makeText(getActivity(),
												R.string.date_parse_error,
												Toast.LENGTH_SHORT).show();
									}
								}
								// add this to the calendar
								ContentResolver cr = getActivity()
										.getContentResolver();
								ContentValues values = new ContentValues();
								GregorianCalendar gc = new GregorianCalendar(
										year, month, day, hour, minute);
								values.put("calendar_id", 1);
								values.put("dtstart", gc.getTimeInMillis());
								gc.add(Calendar.MINUTE, 5);
								values.put("dtend", gc.getTimeInMillis());
								values.put("title",
										getString(R.string.calendar_pill_title));
								StringBuilder message = new StringBuilder();
								message.append(getString(R.string.pill_name))
										.append(":")
										.append(pills.getSelectedItem())
										.append("\n");
								message.append(getString(R.string.pill_number))
										.append(":")
										.append(pillNumber.getText())
										.append("\n");
								message.append(getString(R.string.pill_effect))
										.append(":")
										.append(pillEffect.getSelectedItem())
										.append("\n");
								values.put("description", message.toString());
								values.put("eventTimezone", TimeZone
										.getDefault().getID());
								values.put("hasAlarm", 0); // no alarm
								values.put("eventStatus", 1); // confirmed
								if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.FROYO) {
									values.put("availability", 1); // allow other appointments
									values.put("accessLevel", 2); // private
								}
								Uri result = cr.insert(Uri
										.parse(baseUriForCalendar + "events"),
										values);
								Long event_id = ContentUris.parseId(result);
								Uri deleteReminders = Uri
										.parse(baseUriForCalendar + "reminders");
								Cursor cursor = cr.query(deleteReminders,
										new String[] { "_id" }, "event_id="
												+ event_id.toString(), null,
										null);
								while (cursor.moveToNext()) {
									// never get here :-(
									Uri deleteUri = ContentUris.withAppendedId(
											deleteReminders, cursor.getLong(0));
									cr.delete(deleteUri, null, null);
								}
								cursor.close();
								if (alf != null) {
									alf.getPillsFromCalendar(); // update list
								}
							}
						});
		;
		return builder.create();
	}

	private class TimePickHandler implements OnTimeSetListener {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int theMinute) {
			hour = hourOfDay;
			minute = theMinute;
			pillTime.setText(String.format("%02d:%02d", hourOfDay, minute));
		}
	}

	private class DatePickHandler implements OnDateSetListener {

		@Override
		public void onDateSet(DatePicker view, int theYear, int monthOfYear,
				int dayOfMonth) {
			year = theYear;
			month = monthOfYear;
			day = dayOfMonth;
			Date d = new GregorianCalendar(year, monthOfYear, dayOfMonth)
					.getTime();
			pillDate.setText(new SimpleDateFormat(
					getString(R.string.short_date_time)).format(d));
		}
	}
}
