package org.nvh.hoofdpijndagboek;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class HeadacheCalendarView extends View {
	public class CalendarOnLongClickListener implements OnLongClickListener {
		@Override
		public boolean onLongClick(View v) {
			HeadacheCalendarView view = (HeadacheCalendarView) v;
			SetCalendarDisplayDialog scdd = new SetCalendarDisplayDialog();
			// this works because our context is the MainActivity
			FragmentActivity a = (FragmentActivity) getContext();
			Bundle args = new Bundle();
			args.putInt("rows", view.cr);
			args.putInt("columns", view.cc);
			scdd.setArguments(args);
			scdd.show(a.getSupportFragmentManager(), null);
			return false;
		}
	}

	public class SetCalendarDisplayDialog extends DialogFragment {
		EditText etCols, etRows;

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			// Get the layout inflater
			LayoutInflater inflater = getActivity().getLayoutInflater();

			// Inflate and set the layout for the dialog
			// Pass null as the parent view because its going in the dialog
			// layout
			View v = inflater.inflate(R.layout.calendar_columns, null);
			builder.setView(v)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									int c = Integer.parseInt(etCols.getText()
											.toString());
									int r = Integer.parseInt(etRows.getText()
											.toString());
									HeadacheCalendarView.this.cc = c;
									HeadacheCalendarView.this.cr = r;
									HeadacheCalendarView.this.cell = new Cell(
											c, r);
									HeadacheCalendarView.this.invalidate();
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// User cancelled the dialog
								}
							});

			builder.setMessage("Hatseflats");
			// Create the AlertDialog object and return it
			AlertDialog dialog = builder.create();
			Bundle b = getArguments();
			etCols = (EditText) v.findViewById(R.id.calendar_cols);
			etRows = (EditText) v.findViewById(R.id.calendar_rows);
			Integer c = b.getInt("columns");
			Integer r = b.getInt("rows");
			etCols.setText(c.toString());
			etRows.setText(r.toString());
			return dialog;
		}
	}

	public int cc = 21; // calendar columns
	public int cr = 21; // calendar rows
	int w, h;
	Paint p;
	SimpleDateFormat f = new SimpleDateFormat("M");
	SimpleDateFormat df = new SimpleDateFormat("d");
	Rect rect = new Rect(); // lint says to create up front and reuse
	Cell cell = new Cell(cc, cr);

	public HeadacheCalendarView(Context context) {
		super(context);
		init();
	}

	public HeadacheCalendarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		p = new Paint();
		setOnLongClickListener(new CalendarOnLongClickListener());
	}

	private class Cell {
		public int i;
		public int j;
		public int N = cc;
		public int M = cr;

		public Cell(int N, int M) {
			this.N = N;
			this.M = M;
		}

		public Cell get(Calendar c) {
			Calendar now = Calendar.getInstance();
			// long days = (now.getTimeInMillis() - c.getTimeInMillis())
			// / (1000 * 60 * 60 * 24);
			// int d = (int) days;
			// int d = now.get(Calendar.YEAR)
			// * now.getActualMaximum(Calendar.DAY_OF_YEAR)
			// + now.get(Calendar.DAY_OF_YEAR) - c.get(Calendar.YEAR)
			// * c.getActualMaximum(Calendar.DAY_OF_YEAR) -
			// c.get(Calendar.DAY_OF_YEAR);

			int d = now.get(Calendar.DAY_OF_YEAR);
			if (now.get(Calendar.YEAR) == c.get(Calendar.YEAR)) {
				d -= c.get(Calendar.DAY_OF_YEAR);
			} else {
				now.add(Calendar.YEAR, -1);
				d += now.getActualMaximum(Calendar.DAY_OF_YEAR);
				while (now.get(Calendar.YEAR) != c.get(Calendar.YEAR)) {
					now.add(Calendar.YEAR, -1);
					d += now.getActualMaximum(Calendar.DAY_OF_YEAR);
				}
				d -= c.get(Calendar.DAY_OF_YEAR);
			}
			if (d == cell.N * cell.M) {
				cell.i = cell.j = 0;
			} else {
				cell.i = cell.N - (d / cell.M) - 1;
				// cell.j = (cell.M * cell.N - d - 1) % cell.M;
				cell.j = cell.M * cell.N - d - cell.i * cell.M - 1;
			}
			return cell;
		}

		public Calendar getDate(Cell c) {
			// untested!
			Calendar now = Calendar.getInstance();
			now.roll(Calendar.DAY_OF_YEAR, (c.i % c.N) * c.M + c.j);
			return now;
		}
	}

	private void highlight(Canvas canvas, int xstep, int ystep, int targetDayOfWeek, int color, boolean fill){
		p.setColor(color);
		Calendar cal = Calendar.getInstance();
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		int yDayOffset = (dayOfWeek - targetDayOfWeek + 7) % 7;
		Calendar day = Calendar.getInstance();
		day.add(Calendar.DAY_OF_YEAR, -1 * yDayOffset);
		for (int w = cc * cr / 7; w > 0; w--) {
			cell.get(day);
			rect.left = cell.i * xstep + 1;
			rect.top = cell.j * ystep;
			rect.right = rect.left + xstep;
			if(fill){
				rect.bottom = rect.top + ystep;
			}else{
				rect.bottom = rect.top + ystep / 5;
			}
			canvas.drawRect(rect, p);
			day.add(Calendar.DAY_OF_YEAR, -7);
		}
		
	}
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		w = getWidth();
		h = getHeight();
		p.setColor(Color.BLACK);
		p.setStyle(Style.FILL);
		canvas.drawPaint(p);

		// TODO: Center the control horizontally...

		// this is going to be a view that can handle the past year
		// what it actually shows will be in a setting
		p.setColor(Color.LTGRAY);
		int xstep = (int) (w / cc);
		int ystep = (int) (h / cr);
		int xend = (int) (cc * xstep);
		int yend = (int) (cr * ystep);
		for (double i = 0; i <= xend; i += xstep) {
			for (double j = 0; j <= yend; j += ystep) {
				canvas.drawLine((int) i, 0, (int) i, yend, p);
				canvas.drawLine(0, (int) j, xend, (int) j, p);
			}
		}

		// bottom-right is today
//		highlight(canvas, xstep, ystep, Calendar.MONDAY, Color.CYAN, false);
		highlight(canvas, xstep, ystep, Calendar.SATURDAY, Color.argb(128, 192, 192, 192), true);
		highlight(canvas, xstep, ystep, Calendar.SUNDAY, Color.argb(128, 255, 192, 192), true);

		// mark beginnings of month
		p.setTextSize(ystep - 5);
		Calendar first = Calendar.getInstance();
		first.set(Calendar.DAY_OF_MONTH, 1);
		// mark first of the month
		for (int m = (int) (12.0 * cc * cr / 365 + 1); m > 0; m--) {
			cell.get(first);

			// fill the cell with ltgray
			p.setColor(Color.LTGRAY);
			rect.left = cell.i * xstep + 1;
			rect.top = cell.j * ystep;
			rect.right = rect.left + xstep;
			rect.bottom = rect.top + ystep;
			canvas.drawRect(rect, p);

			if (first.get(Calendar.MONTH) == Calendar.JANUARY) {
				// new year
				p.setColor(Color.YELLOW);
			} else {
				// a red top
				p.setColor(Color.RED);
			}
			rect.bottom = rect.top + ystep / 5;
			canvas.drawRect(rect, p);

			// the name or number of the month
			p.setColor(Color.BLACK);
			Rect bounds = new Rect();
			String s = f.format(first.getTime());
			p.getTextBounds(s, 0, s.length(), bounds);
			canvas.drawText(s, cell.i * xstep + (xstep - bounds.width()) / 2,
					(cell.j) * ystep + ystep - 3, p);
			first.add(Calendar.MONTH, -1);
		}

		// use this to set the day of the month in each cell
		// cal = Calendar.getInstance();
		// for (int d = 0; d <= 350; d++) {
		// Cell c = getCell(cal);
		// // canvas.drawText(df.format(cal.getTime()), c.i * xstep + 5, -3 +
		// // ((c.j) * ystep), p);
		// canvas.drawText(
		// new Integer(cal.get(Calendar.DAY_OF_MONTH)).toString(), c.i
		// * xstep + 5, -3 + ((c.j+1) * ystep), p);
		// cal.add(Calendar.DAY_OF_YEAR, -1);
		// }

		ContentResolver contentResolver = getContext().getContentResolver();

		Uri uri = Uri.parse("content://com.android.calendar/calendars");
		String[] projection = new String[] { "_id" };

		Cursor cursor = contentResolver
				.query(uri, projection, null, null, null);

		int[] calIds = null;
		if (cursor != null) {
			cursor.moveToFirst();
			calIds = new int[cursor.getCount()];
			for (int i = 0; i < calIds.length; i++) {
				calIds[i] = cursor.getInt(0);
				cursor.moveToNext();
			}
			cursor.close();
		}

		Calendar cal = Calendar.getInstance();
		// TODO: I wonder if this works for all phone makers... I read that some
		// replaced the calendar with something of their own.
		// But that was before com.android.calendar
		// http://android-agenda-widget.googlecode.com/svn-history/r17/android-calendar-provider/trunk/src/com/everybodyallthetime/android/provider/calendar/CalendarProvider.java
		Uri.Builder builder = Uri.parse(
				getCalendarUriBase() + "instances/when/").buildUpon();
		cal.add(Calendar.DAY_OF_YEAR, -cc * cr);
		ContentUris.appendId(builder, cal.getTimeInMillis());
		ContentUris.appendId(builder, Calendar.getInstance().getTimeInMillis());

		Cursor eventCursor = contentResolver
				.query(builder.build(), new String[] { "title", "begin", "end",
						"allDay", "description" },
				// Filtering works on FP 4.0 but not on AT 2.2
				// "title=?",
				// new String[] { getContext().getString(
				// R.string.calendar_entry_title) },
						null, null, "startDay ASC, startMinute ASC");

		System.out.println("eventCursor count=" + eventCursor.getCount());
		if (eventCursor.getCount() > 0) {
			while (eventCursor.moveToNext()) {
				String title = eventCursor.getString(0);
				Calendar begin = Calendar.getInstance();
				// This converts time zones silently, we go from UTC to the
				// configured timezone
				begin.setTimeInMillis(eventCursor.getLong(1));
				Calendar end = Calendar.getInstance();
				end.setTimeInMillis(eventCursor.getLong(2));
				Boolean allDay = !eventCursor.getString(3).equals("0");
				String description = eventCursor.getString(4);
				if (title.contains("irthday")) {// TODO: check for yearly recurring rule...
					cell.get(begin);
					int i = cell.i;
					int j = cell.j;
					p.setColor(Color.BLUE);
					rect.left = i * xstep + xstep / 5;
					rect.top = j * ystep + ystep / 5;
					rect.right = rect.left + xstep - xstep / 5;
					rect.bottom = rect.top + ystep - ystep / 5;
					canvas.drawRect(rect, p);
				}
				if (title.equals(getContext().getString(
						R.string.calendar_entry_title))) {
					cell.get(begin);
					int i = cell.i;
					int j = cell.j;
					cell.get(end);
					int i2 = cell.i;
					int j2 = cell.j;
					String[] lgh = getResources().getStringArray(
							R.array.lgh_array);

					if (i == i2) {
						if (j == j2) {
							// same day
							int ernst = parseDescription(description,
									getContext().getString(R.string.ernst), lgh);
							if (ernst == 0) {
								p.setColor(Color.GREEN);
							} else if (ernst == 1) {
								p.setColor(Color.MAGENTA);
							} else if (ernst == 2) {
								p.setColor(Color.YELLOW);
							} else if (ernst == 3) {
								p.setColor(Color.RED);
							}
							rect.left = i * xstep + xstep / 5;
							rect.top = j * ystep + ystep / 5;
							rect.right = rect.left + xstep - xstep / 5;
							rect.bottom = rect.top + ystep - ystep / 5;
							canvas.drawRect(rect, p);
						}
					}
				}
				System.out.println("Title:" + title);
				System.out.println("Begin:" + begin);
				System.out.println("End:" + end);
				System.out.println("All Day:" + allDay);
				System.out.println("Decription:" + description);
				System.out.println("---------------------");
			}
		}
	}

	/*
	 * Determines if it's a pre 2.1 or a 2.2 calendar Uri, and returns the Uri
	 */
	private String getCalendarUriBase() {
		String calendarUriBase = null;
		Uri calendars = Uri.parse("content://calendar/calendars");
		Cursor managedCursor = null;
		ContentResolver cr = getContext().getContentResolver();
		try {
			managedCursor = cr.query(calendars, null, null, null, null);
		} catch (Exception e) {
			// eat
		}

		if (managedCursor != null) {
			calendarUriBase = "content://calendar/";
		} else {
			calendars = Uri.parse("content://com.android.calendar/calendars");
			try {
				managedCursor = cr.query(calendars, null, null, null, null);
			} catch (Exception e) {
				// eat
			}

			if (managedCursor != null) {
				calendarUriBase = "content://com.android.calendar/";
			}

		}

		return calendarUriBase;
	}

	/* add an event to calendar */

	private int parseDescription(String description, String what,
			String[] options) {
		if (description == null)
			return 0;
		if (description.contains(what)) {
			// find the entry and its :
			int p1 = description.indexOf(what) + what.length() + 1;
			int p2 = description.indexOf('\n', p1);
			String answer = null;
			if (p2 == -1) {
				answer = description.substring(p1);
			} else {
				answer = description.substring(p1, p2);
			}
			int i = 0;
			for (String option : options) {
				if (answer.equals(option)) {
					return i;
				}
				i++;
			}
			return 0;
		}
		return 0;
	}

	private void addEvent(int m_selectedCalendarId) {
		ContentValues l_event = new ContentValues();
		l_event.put("calendar_id", m_selectedCalendarId);
		l_event.put("title", "roman10 calendar tutorial test");
		l_event.put("description", "This is a simple test for calendar api");
		l_event.put("eventLocation", "@home");
		l_event.put("dtstart", System.currentTimeMillis());
		l_event.put("dtend", System.currentTimeMillis() + 1800 * 1000);
		l_event.put("allDay", 0);
		// status: 0~ tentative; 1~ confirmed; 2~ canceled
		l_event.put("eventStatus", 1);
		// 0~ default; 1~ confidential; 2~ private; 3~ public
		l_event.put("visibility", 0);
		// 0~ opaque, no timing conflict is allowed; 1~ transparency, allow
		// overlap of scheduling
		l_event.put("transparency", 0);
		// 0~ false; 1~ true
		l_event.put("hasAlarm", 1);
		Uri l_eventUri;
		l_eventUri = Uri.parse("content://com.android.calendar/events");
		// Uri l_uri = this.getContentResolver().insert(l_eventUri, l_event);
	}
}
