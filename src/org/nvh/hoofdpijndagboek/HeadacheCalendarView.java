package org.nvh.hoofdpijndagboek;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class HeadacheCalendarView extends View implements OnGestureListener {
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
		RadioGroup cols;
		RadioGroup rows;

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
									int csel = cols.getCheckedRadioButtonId();
									RadioButton b = (RadioButton) cols.findViewById(csel);
									int c = Integer.parseInt((String) b
											.getText()) * 7;
									int rsel = rows.getCheckedRadioButtonId();
									b = (RadioButton) rows.findViewById(rsel);
									int r = Integer.parseInt((String) b
											.getText()) * 7;
									HeadacheCalendarView.this.cc = c;
									HeadacheCalendarView.this.cr = r;
									SharedPreferences settings = ((MainActivity) getContext())
											.getPreferences(Context.MODE_PRIVATE);
									SharedPreferences.Editor editor = settings
											.edit();
									editor.putInt("calendarColumns", c);
									editor.putInt("calendarRows", r);
									editor.commit();
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

			builder.setMessage(getString(R.string.title_for_grid));
			// Create the AlertDialog object and return it
			AlertDialog dialog = builder.create();
			Bundle b = getArguments();
			rows = (RadioGroup) v.findViewById(R.id.radioWeeksRows);
			cols = (RadioGroup) v.findViewById(R.id.radioWeeksColumns);
			Integer c = b.getInt("columns");
			Integer r = b.getInt("rows");
			switch (c) {
			case 7:
				((RadioButton)cols.findViewById(R.id.week1c)).setChecked(true);
				break;
			case 14:
				((RadioButton)cols.findViewById(R.id.week2c)).setChecked(true);
				break;
			case 21:
				((RadioButton)cols.findViewById(R.id.week3c)).setChecked(true);
				break;
			case 28:
				((RadioButton)cols.findViewById(R.id.week4c)).setChecked(true);
				break;
			case 35:
				((RadioButton)cols.findViewById(R.id.week5c)).setChecked(true);
				break;
			default:
				((RadioButton)cols.findViewById(R.id.week3c)).setChecked(true);
				break;
			}
			switch (r) {
			case 7:
				((RadioButton)rows.findViewById(R.id.week1r)).setChecked(true);
				break;
			case 14:
				((RadioButton)rows.findViewById(R.id.week2r)).setChecked(true);
				break;
			case 21:
				((RadioButton)rows.findViewById(R.id.week3r)).setChecked(true);
				break;
			case 28:
				((RadioButton)rows.findViewById(R.id.week4r)).setChecked(true);
				break;
			case 35:
				((RadioButton)rows.findViewById(R.id.week5r)).setChecked(true);
				break;
			default:
				((RadioButton)rows.findViewById(R.id.week3r)).setChecked(true);
				break;
			}
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
	Rect bounds = new Rect();
	Cell cell = new Cell(cc, cr);
	HashMap<Long, List<Integer>> nEvents = new HashMap<Long, List<Integer>>();
	String calendarURI = null;

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
		SharedPreferences settings = ((MainActivity) getContext())
				.getPreferences(Context.MODE_PRIVATE);
		cc = settings.getInt("calendarColumns", 21);
		cr = settings.getInt("calendarRows", 21);
		cell = new Cell(cc, cr);
		calendarURI = getCalendarUriBase();
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

	private void highlight(Canvas canvas, int xstep, int ystep,
			int targetDayOfWeek, int color, boolean fill) {
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
			if (fill) {
				rect.bottom = rect.top + ystep;
			} else {
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
		p.setColor(Color.WHITE);
		int xstep = (int) (w / cc + 0.5);
		int ystep = (int) (h / cr + 0.5);
		int xend = (int) (cc * xstep);
		int yend = (int) (cr * ystep);
		for (double i = 0; i <= xend; i += xstep) {
			for (double j = 0; j <= yend; j += ystep) {
				canvas.drawLine((int) i, 0, (int) i, yend, p);
				canvas.drawLine(0, (int) j, xend, (int) j, p);
			}
		}

		// bottom-right is today
		// highlight(canvas, xstep, ystep, Calendar.MONDAY, Color.CYAN, false);
		highlight(canvas, xstep, ystep, Calendar.SATURDAY,
				Color.argb(128, 192, 192, 192), true);
		highlight(canvas, xstep, ystep, Calendar.SUNDAY,
				Color.argb(128, 255, 192, 192), true);

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
		nEvents.clear();
		Calendar cal = Calendar.getInstance();
		// TODO: I wonder if this works for all phone makers... I read that some
		// replaced the calendar with something of their own.
		// But that was before com.android.calendar
		// http://android-agenda-widget.googlecode.com/svn-history/r17/android-calendar-provider/trunk/src/com/everybodyallthetime/android/provider/calendar/CalendarProvider.java
		Uri.Builder builder = Uri.parse(calendarURI + "instances/when/")
				.buildUpon();
		cal.add(Calendar.DAY_OF_YEAR, -cc * cr);
		ContentUris.appendId(builder, cal.getTimeInMillis());
		ContentUris.appendId(builder, Calendar.getInstance().getTimeInMillis());

		Cursor eventCursor = contentResolver
				.query(builder.build(), new String[] { "title", "begin", "end",
						"allDay", "description" }, null, null,
						"startDay ASC, startMinute ASC");
		if (eventCursor.getCount() > 0) {
			String[] lgh = getResources().getStringArray(R.array.lgh_array);

			// read in all events and store in a hash by date
			while (eventCursor.moveToNext()) {
				Calendar begin = Calendar.getInstance();
				begin.setTimeInMillis(eventCursor.getLong(1));
				Calendar end = Calendar.getInstance();
				end.setTimeInMillis(eventCursor.getLong(2));
				// Boolean allDay = !eventCursor.getString(3).equals("0");
				String description = eventCursor.getString(4);
				Calendar date = (Calendar) begin.clone();
				String title = eventCursor.getString(0);
				setColor(lgh, description, title);
				do {
					Calendar cd = (Calendar) date.clone();
					cd.set(Calendar.HOUR, 0);
					cd.set(Calendar.MINUTE, 0);
					cd.set(Calendar.SECOND, 0);
					cd.set(Calendar.MILLISECOND, 0);
					List<Integer> c = nEvents.get(cd.getTimeInMillis());
					if (c == null) {
						c = new ArrayList<Integer>();
					}
					c.add(p.getColor());
					nEvents.put(cd.getTimeInMillis(), c);
					date.add(Calendar.DAY_OF_MONTH, 1);
				} while (date.before(end));
			}

			// show the events as coloured rects in the grid
			for (Long millis : nEvents.keySet()) {
				Calendar d = Calendar.getInstance();
				d.setTimeInMillis(millis);
				cell.get(d);
				int width = (int) ((float) (xstep - 2) / nEvents.get(millis)
						.size());
				int event = 0;
				for (Integer color : nEvents.get(millis)) {
					p.setColor(color);
					rect.left = cell.i * xstep + 1 + ((event > 0) ? 1 : 0)
							+ event * width;
					rect.top = cell.j * ystep + 1;
					if (event != (nEvents.get(millis).size() - 1)) {
						rect.right = rect.left + width - 1;
					} else {
						// fix rounding errors by extending last rect
						rect.right = cell.i * xstep + xstep;
					}
					rect.bottom = rect.top + ystep - 1;
					canvas.drawRect(rect, p);
					if (event != (nEvents.get(millis).size() - 1)) {
						p.setColor(Color.BLACK);
						canvas.drawLine(rect.right + 1, rect.top,
								rect.right + 1, rect.bottom, p);
					}
					event++;
				}
			}
			eventCursor.close();
			
			// mark beginnings of month, I want these on top.
			p.setTextSize(Math.min(ystep - 5, xstep-2));
			Calendar first = Calendar.getInstance();
			first.set(Calendar.DAY_OF_MONTH, 1);
			// mark first of the month
			for (int m = (int) (12.0 * cc * cr / 365 + 1); m > 0; m--) {
				cell.get(first);

				// fill the cell with ltgray
				p.setColor(Color.LTGRAY);
				rect.left = cell.i * xstep + 1;
				rect.top = cell.j * ystep;
				rect.right = rect.left + xstep - 1;
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
				String s = f.format(first.getTime());
				p.getTextBounds(s, 0, s.length(), bounds);
				canvas.drawText(s, cell.i * xstep + (xstep - bounds.width()) / 2,
						(cell.j) * ystep + ystep - 3, p);
				first.add(Calendar.MONTH, -1);
			}
		}
	}

	private void setColor(String[] lgh, String description, String title) {
		if (title.equals(getContext().getString(R.string.calendar_entry_title))) {
			int ernst = parseDescription(description,
					getContext().getString(R.string.ernst), lgh);
			if (ernst == 0) {
				p.setColor(Color.GRAY);
			} else if (ernst == 1) {
				p.setColor(Color.MAGENTA);
			} else if (ernst == 2) {
				p.setColor(Color.YELLOW);
			} else if (ernst == 3) {
				p.setColor(Color.RED);
			}
		} else {
			p.setColor(Color.LTGRAY);
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
			managedCursor.close();
		} catch (Exception e) {
			// eat
		}

		if (managedCursor != null) {
			calendarUriBase = "content://calendar/";
		} else {
			calendars = Uri.parse("content://com.android.calendar/calendars");
			try {
				managedCursor = cr.query(calendars, null, null, null, null);
				managedCursor.close();
			} catch (Exception e) {
				// eat
			}

			if (managedCursor != null) {
				calendarUriBase = "content://com.android.calendar/";
			}

		}

		return calendarUriBase;
	}

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

	//
	// private void addEvent(int m_selectedCalendarId) {
	// ContentValues l_event = new ContentValues();
	// l_event.put("calendar_id", m_selectedCalendarId);
	// l_event.put("title", "roman10 calendar tutorial test");
	// l_event.put("description", "This is a simple test for calendar api");
	// l_event.put("eventLocation", "@home");
	// l_event.put("dtstart", System.currentTimeMillis());
	// l_event.put("dtend", System.currentTimeMillis() + 1800 * 1000);
	// l_event.put("allDay", 0);
	// // status: 0~ tentative; 1~ confirmed; 2~ canceled
	// l_event.put("eventStatus", 1);
	// // 0~ default; 1~ confidential; 2~ private; 3~ public
	// l_event.put("visibility", 0);
	// // 0~ opaque, no timing conflict is allowed; 1~ transparency, allow
	// // overlap of scheduling
	// l_event.put("transparency", 0);
	// // 0~ false; 1~ true
	// l_event.put("hasAlarm", 1);
	// Uri l_eventUri;
	// l_eventUri = Uri.parse("content://com.android.calendar/events");
	// // Uri l_uri = this.getContentResolver().insert(l_eventUri, l_event);
	// }

	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
}
