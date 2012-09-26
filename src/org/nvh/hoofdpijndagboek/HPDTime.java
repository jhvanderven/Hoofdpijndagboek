package org.nvh.hoofdpijndagboek;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class HPDTime extends SherlockFragmentActivity {

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

	public static class TimingFragment extends SherlockFragment {
		private static TextView startDateTime;
		private static TextView endDateTime;

		private static int startDateYear;
		private static int startDateMonth;
		private static int startDateDay;
		private static int startTimeHour;
		private static int startTimeMinute;
		private static int endDateYear;
		private static int endDateMonth;
		private static int endDateDay;
		private static int endTimeHour;
		private static int endTimeMinute;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.time_layout, container, false);
			Spinner spinner = (Spinner) v.findViewById(R.id.spinnerErnst);
			ArrayAdapter<CharSequence> adapter = ArrayAdapter
					.createFromResource(getActivity(), R.array.lgh_array,
							android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter);

			final Calendar c = Calendar.getInstance();
			c.add(Calendar.HOUR, -5);
			startTimeHour = c.get(Calendar.HOUR_OF_DAY);
			startTimeMinute = 0;// c.get(Calendar.MINUTE);
			startDateDay = c.get(Calendar.DAY_OF_MONTH);
			startDateMonth = c.get(Calendar.MONTH);
			startDateYear = c.get(Calendar.YEAR);

			endDateDay = startDateDay;
			endDateMonth = startDateMonth;
			endDateYear = startDateYear;
			endTimeHour = startTimeHour + 4;
			endTimeMinute = startTimeMinute;
			startDateTime = (TextView) v.findViewById(R.id.startDateTime);
			endDateTime = (TextView) v.findViewById(R.id.endDateTime);
			updateTimes(startDateTime, startDateYear, startDateMonth,
					startDateDay, startTimeHour, startTimeMinute);
			updateTimes(endDateTime, endDateYear, endDateMonth, endDateDay,
					endTimeHour, endTimeMinute);
			Button b = (Button) v.findViewById(R.id.startDate);
			b.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					showDatePickerDialog(v);

				}
			});
			b = (Button) v.findViewById(R.id.startTime);
			b.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					showTimePickerDialog(v);

				}
			});
			b = (Button) v.findViewById(R.id.endDate);
			b.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					showDatePickerDialog(v);

				}
			});
			b = (Button) v.findViewById(R.id.endTime);
			b.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					showTimePickerDialog(v);

				}
			});

			return v;
		}

		private static void updateTimes(TextView v, int year, int month,
				int day, int hour, int minute) {
			String s = String.format("%4d-%02d-%02d %02d:%02d", year,
					month + 1, day, hour, minute);
			v.setText(s);
		}

		public void showTimePickerDialog(View v) {
			DialogFragment newFragment = TimePickerFragment.newInstance(v
					.getId());
			newFragment.show(getActivity().getSupportFragmentManager(),
					v.toString());
		}

		public void showDatePickerDialog(View v) {
			DialogFragment newFragment = DatePickerFragment.newInstance(v
					.getId());
			newFragment.show(getActivity().getSupportFragmentManager(),
					v.toString());
		}

		// public boolean onCreateOptionsMenu(Menu menu) {
		// getActivity().getMenuInflater().inflate(R.menu.activity_main, menu);
		// return true;
		// }
		//
		// public void sendMessage(View view) {
		// Intent intent = new Intent(Intent.ACTION_INSERT)
		// .setType("vnd.android.cursor.item/event")
		// // .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
		// // beginTime.getTimeInMillis())
		// // .putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
		// // endTime.getTimeInMillis())
		// .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
		// // just included for completeness
		// .putExtra(Events.TITLE, "Hoofdpijnaanval")
		// // .putExtra(Events.DESCRIPTION, message)
		// .putExtra(Events.EVENT_LOCATION, "Hoogland")
		// .putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY)
		// .putExtra(Events.ACCESS_LEVEL, Events.ACCESS_PRIVATE)
		// .putExtra(Intent.EXTRA_EMAIL, "upload@nvh.org");
		// startActivity(intent);
		// }

		public static class TimePickerFragment extends DialogFragment implements
				TimePickerDialog.OnTimeSetListener {

			public static TimePickerFragment newInstance(int caller_id) {
				TimePickerFragment f = new TimePickerFragment();
				Bundle args = new Bundle();
				args.putInt("caller_id", caller_id);
				f.setArguments(args);
				return f;
			}

			@Override
			public Dialog onCreateDialog(Bundle savedInstanceState) {
				int caller_id = getArguments().getInt("caller_id");
				switch (caller_id) {
				case R.id.startTime:
					// Create a new instance of TimePickerDialog and return it
					return new TimePickerDialog(getActivity(), this,
							startTimeHour, startTimeMinute, true);
				case R.id.endTime:
					// Create a new instance of TimePickerDialog and return it
					return new TimePickerDialog(getActivity(), this,
							endTimeHour, endTimeMinute, true);
				}
				return null;
			}

			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				// Do something with the time chosen by the user
				int caller_id = getArguments().getInt("caller_id");
				switch (caller_id) {
				case R.id.startTime:
					startTimeHour = hourOfDay;
					startTimeMinute = minute;
					updateTimes(startDateTime, startDateYear, startDateMonth,
							startDateDay, startTimeHour, startTimeMinute);
					break;
				case R.id.endTime:
					endTimeHour = hourOfDay;
					endTimeMinute = minute;
					updateTimes(endDateTime, endDateYear, endDateMonth,
							endDateDay, endTimeHour, endTimeMinute);
					break;

				}
			}
		}

		public static class DatePickerFragment extends DialogFragment implements
				DatePickerDialog.OnDateSetListener {

			public static DatePickerFragment newInstance(int caller_id) {
				DatePickerFragment f = new DatePickerFragment();
				Bundle args = new Bundle();
				args.putInt("caller_id", caller_id);
				f.setArguments(args);
				return f;
			}

			@Override
			public Dialog onCreateDialog(Bundle savedInstanceState) {
				int caller_id = getArguments().getInt("caller_id");
				switch (caller_id) {
				case R.id.startDate:
					// Create a new instance of TimePickerDialog and return it
					return new DatePickerDialog(getActivity(), this,
							startDateYear, startDateMonth, startDateDay);
				case R.id.endDate:
					// Create a new instance of TimePickerDialog and return it
					return new DatePickerDialog(getActivity(), this,
							endDateYear, endDateMonth, endDateDay);
				}
				return null;
			}

			public void onDateSet(DatePicker view, int year, int month, int day) {
				int caller_id = getArguments().getInt("caller_id");
				switch (caller_id) {
				case R.id.endDate:
					endDateDay = day;
					endDateMonth = month;
					endDateYear = year;
					updateTimes(endDateTime, endDateYear, endDateMonth,
							endDateDay, endTimeHour, endTimeMinute);
					break;
				case R.id.startDate:
					startDateDay = day;
					startDateMonth = month;
					startDateYear = year;
					updateTimes(startDateTime, startDateYear, startDateMonth,
							startDateDay, startTimeHour, startTimeMinute);
					break;
				}
			}
		}
	}
}
