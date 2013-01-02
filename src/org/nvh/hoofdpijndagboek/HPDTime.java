package org.nvh.hoofdpijndagboek;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class HPDTime extends SherlockFragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(MainActivity.THEME); // Used for theme switching in samples
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_stack);
	}

	public static class TimingFragment extends SherlockFragment {

		public static Button startTime;
		public static Button startDate;
		public static Button endTime;
		public static Button endDate;

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

		public static void pleaseUpdate(HeadacheAttack a) {
			update(a);
		}

		private static void update(HeadacheAttack a) {
			startDateYear = a.start.get(Calendar.YEAR);
			startDateMonth = a.start.get(Calendar.MONTH);
			startDateDay = a.start.get(Calendar.DAY_OF_MONTH);
			startTimeHour = a.start.get(Calendar.HOUR_OF_DAY);
			startTimeMinute = a.start.get(Calendar.MINUTE);
			updateTimes(startDate, startTime, startDateYear, startDateMonth,
					startDateDay, startTimeHour, startTimeMinute);
			endDateYear = a.end.get(Calendar.YEAR);
			endDateMonth = a.end.get(Calendar.MONTH);
			endDateDay = a.end.get(Calendar.DAY_OF_MONTH);
			endTimeHour = a.end.get(Calendar.HOUR_OF_DAY);
			endTimeMinute = a.end.get(Calendar.MINUTE);
			updateTimes(endDate, endTime, endDateYear, endDateMonth,
					endDateDay, endTimeHour, endTimeMinute);
		}

		@Override
		public void onResume() {
			update(((MainActivity) getActivity()).getAttack());
			super.onResume();
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.time_layout, container, false);
			v.setTag(R.layout.time_layout);
			startDate = (Button) v.findViewById(R.id.startDate);
			startDate.setTag(R.id.startDate);
			startDate.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					showDatePickerDialog(v);

				}
			});
			startTime = (Button) v.findViewById(R.id.startTime);
			startTime.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					showTimePickerDialog(v);

				}
			});
			endDate = (Button) v.findViewById(R.id.endDate);
			endDate.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					showDatePickerDialog(v);

				}
			});
			endTime = (Button) v.findViewById(R.id.endTime);
			endTime.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					showTimePickerDialog(v);

				}
			});
			return v;
		}

		public static void updateTimes(TextView date, TextView time, int year,
				int month, int day, int hour, int minute) {
			String d = String.format("%4d-%02d-%02d", year, month + 1, day);
			String t = String.format("%02d:%02d", hour, minute);
			if (date != null)
				date.setText(d);
			if (time != null)
				time.setText(t);
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
					updateTimes(startDate, startTime, startDateYear,
							startDateMonth, startDateDay, startTimeHour,
							startTimeMinute);
					((MainActivity) getActivity()).getAttack().start.set(
							startDateYear, startDateMonth, startDateDay,
							hourOfDay, minute, 0);
					compareDates(true,
							((MainActivity) getActivity()).getAttack());
					break;
				case R.id.endTime:
					endTimeHour = hourOfDay;
					endTimeMinute = minute;
					updateTimes(endDate, endTime, endDateYear, endDateMonth,
							endDateDay, endTimeHour, endTimeMinute);
					((MainActivity) getActivity()).getAttack().end.set(
							endDateYear, endDateMonth, endDateDay, hourOfDay,
							minute, 0);
					compareDates(false,
							((MainActivity) getActivity()).getAttack());
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
					updateTimes(endDate, endTime, endDateYear, endDateMonth,
							endDateDay, endTimeHour, endTimeMinute);
					((MainActivity) getActivity()).getAttack().end.set(
							endDateYear, endDateMonth, endDateDay, endTimeHour,
							endTimeMinute, 0);
					compareDates(false,
							((MainActivity) getActivity()).getAttack());
					break;
				case R.id.startDate:
					startDateDay = day;
					startDateMonth = month;
					startDateYear = year;
					updateTimes(startDate, startTime, startDateYear,
							startDateMonth, startDateDay, startTimeHour,
							startTimeMinute);
					((MainActivity) getActivity()).getAttack().start.set(
							startDateYear, startDateMonth, startDateDay,
							startTimeHour, startTimeMinute, 0);
					compareDates(true,
							((MainActivity) getActivity()).getAttack());
					break;
				}
			}
		}

		public static void compareDates(boolean start, HeadacheAttack a) {
			// Make sure the end date is at least 1 minute/hour after the start
			// date
			Calendar s = Calendar.getInstance();
			s.set(Calendar.YEAR, startDateYear);
			s.set(Calendar.MONTH, startDateMonth);
			s.set(Calendar.DAY_OF_MONTH, startDateDay);
			s.set(Calendar.HOUR_OF_DAY, startTimeHour);
			s.set(Calendar.MINUTE, startTimeMinute);
			s.set(Calendar.SECOND, 0);
			s.set(Calendar.MILLISECOND, 0);
			Calendar e = Calendar.getInstance();
			e.set(Calendar.YEAR, endDateYear);
			e.set(Calendar.MONTH, endDateMonth);
			e.set(Calendar.DAY_OF_MONTH, endDateDay);
			e.set(Calendar.HOUR_OF_DAY, endTimeHour);
			e.set(Calendar.MINUTE, endTimeMinute);
			e.set(Calendar.SECOND, 0);
			e.set(Calendar.MILLISECOND, 0);
			if (start) {
				if (e.before(s)) {
					// make e => s+1h
					Calendar e2 = (Calendar) s.clone();
					e2.add(Calendar.HOUR_OF_DAY, 1);
					endDateYear = e2.get(Calendar.YEAR);
					endDateMonth = e2.get(Calendar.MONTH);
					endDateDay = e2.get(Calendar.DAY_OF_MONTH);
					endTimeHour = e2.get(Calendar.HOUR_OF_DAY);
					endTimeMinute = e2.get(Calendar.MINUTE);
					updateTimes(endDate, endTime, endDateYear, endDateMonth,
							endDateDay, endTimeHour, endTimeMinute);
					a.end.set(endDateYear, endDateMonth, endDateDay,
							endTimeHour, endTimeMinute, 0);
				}
			} else {
				if (s.after(e)) {
					// make s => e-1h
					Calendar s2 = (Calendar) e.clone();
					s2.add(Calendar.HOUR_OF_DAY, -1);
					startDateYear = s2.get(Calendar.YEAR);
					startDateMonth = s2.get(Calendar.MONTH);
					startDateDay = s2.get(Calendar.DAY_OF_MONTH);
					startTimeHour = s2.get(Calendar.HOUR_OF_DAY);
					startTimeMinute = s2.get(Calendar.MINUTE);
					updateTimes(startDate, startTime, startDateYear,
							startDateMonth, startDateDay, startTimeHour,
							startTimeMinute);
					a.start.set(startDateYear, startDateMonth, startDateDay,
							startTimeHour, startTimeMinute, 0);
				}
			}
		}
	}
}
