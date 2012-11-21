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
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
		private static View view;

		public static Button startTime;
		public static Button startDate;
		public static Button endTime;
		public static Button endDate;

		public static RadioGroup ernst;

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

		public static String getData() {
			StringBuilder sb = new StringBuilder();
			sb.append(view.getContext().getString(R.string.ernst)).append(":");
			RadioButton b = (RadioButton) view.findViewById(ernst
					.getCheckedRadioButtonId());
			sb.append(b.getText()).append("\n");
			return sb.toString();
		}

		public static void pleaseUpdate(HeadacheAttack a, String[] lgh) {
			update(a, lgh);
		}

		private static void update(HeadacheAttack a, String[] lgh) {
			int id;
			if (a.ernst.equalsIgnoreCase(lgh[0])) {
				id = R.id.laag;
			} else if (a.ernst.equalsIgnoreCase(lgh[1])) {
				id = R.id.gemiddeld;
			} else {
				id = R.id.hoog;
			}
			RadioButton b = (RadioButton) view.findViewById(id);
			b.setChecked(true);
			updateTimes(startDate, startTime, a.start.get(Calendar.YEAR),
					a.start.get(Calendar.MONTH),
					a.start.get(Calendar.DAY_OF_MONTH),
					a.start.get(Calendar.HOUR_OF_DAY),
					a.start.get(Calendar.MINUTE));
			updateTimes(endDate, endTime, a.end.get(Calendar.YEAR),
					a.end.get(Calendar.MONTH),
					a.end.get(Calendar.DAY_OF_MONTH),
					a.end.get(Calendar.HOUR_OF_DAY), a.end.get(Calendar.MINUTE));
		}

		@Override
		public void onResume() {
			update(((MainActivity) getActivity()).getAttack(), new String[] {
					getActivity().getString(R.string.laag),
					getActivity().getString(R.string.gemiddeld),
					getActivity().getString(R.string.hoog) });
			super.onResume();
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.time_layout, container, false);
			v.setTag(R.layout.time_layout);
			ernst = (RadioGroup) v.findViewById(R.id.ernst);
			// ArrayAdapter<CharSequence> adapter = ArrayAdapter
			// .createFromResource(getActivity(), R.array.lgh_array,
			// android.R.layout.simple_spinner_item);
			// adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// ernst.setAdapter(adapter);

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
			// startDateTime = (TextView) v.findViewById(R.id.startDateTime);
			// endDateTime = (TextView) v.findViewById(R.id.endDateTime);
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
			updateTimes(startDate, startTime, startDateYear, startDateMonth,
					startDateDay, startTimeHour, startTimeMinute);
			updateTimes(endDate, endTime, endDateYear, endDateMonth,
					endDateDay, endTimeHour, endTimeMinute);

			view = v;
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
					break;
				case R.id.endTime:
					endTimeHour = hourOfDay;
					endTimeMinute = minute;
					updateTimes(endDate, endTime, endDateYear, endDateMonth,
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
					updateTimes(endDate, endTime, endDateYear, endDateMonth,
							endDateDay, endTimeHour, endTimeMinute);
					break;
				case R.id.startDate:
					startDateDay = day;
					startDateMonth = month;
					startDateYear = year;
					updateTimes(startDate, startTime, startDateYear,
							startDateMonth, startDateDay, startTimeHour,
							startTimeMinute);
					break;
				}
			}
		}
	}
}
