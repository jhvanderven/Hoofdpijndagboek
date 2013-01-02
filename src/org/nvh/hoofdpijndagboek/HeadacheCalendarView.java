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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class HeadacheCalendarView extends View implements OnGestureListener {
	private GestureDetector gesturedetector;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// the trick is to call the onTouchEvent of the detector
		return gesturedetector.onTouchEvent(event);
	}

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
					.setPositiveButton(
							getActivity().getString(R.string.dialogOk),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									int csel = cols.getCheckedRadioButtonId();
									RadioButton b = (RadioButton) cols
											.findViewById(csel);
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
									HeadacheCalendarView.this.redraw();
								}
							})
					.setNegativeButton(
							getActivity().getString(R.string.dialogCancel),
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
				((RadioButton) cols.findViewById(R.id.week1c)).setChecked(true);
				break;
			case 14:
				((RadioButton) cols.findViewById(R.id.week2c)).setChecked(true);
				break;
			case 21:
				((RadioButton) cols.findViewById(R.id.week3c)).setChecked(true);
				break;
			case 28:
				((RadioButton) cols.findViewById(R.id.week4c)).setChecked(true);
				break;
			case 35:
				((RadioButton) cols.findViewById(R.id.week5c)).setChecked(true);
				break;
			default:
				((RadioButton) cols.findViewById(R.id.week3c)).setChecked(true);
				break;
			}
			switch (r) {
			case 7:
				((RadioButton) rows.findViewById(R.id.week1r)).setChecked(true);
				break;
			case 14:
				((RadioButton) rows.findViewById(R.id.week2r)).setChecked(true);
				break;
			case 21:
				((RadioButton) rows.findViewById(R.id.week3r)).setChecked(true);
				break;
			case 28:
				((RadioButton) rows.findViewById(R.id.week4r)).setChecked(true);
				break;
			case 35:
				((RadioButton) rows.findViewById(R.id.week5r)).setChecked(true);
				break;
			default:
				((RadioButton) rows.findViewById(R.id.week3r)).setChecked(true);
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
	Rect rect = new Rect(); // lint says to create up front and reuse
	Rect bounds = new Rect();
	Cell cell = new Cell(cc, cr);
	HashMap<Long, List<CalendarEvent>> calendarEvents = new HashMap<Long, List<CalendarEvent>>();
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
		calendarURI = Utils.getCalendarUriBase(getContext()
				.getContentResolver());
		gesturedetector = new GestureDetector(this.getContext(), this);
		redraw();
	}

	public void redraw() {
		new ReadCalendarTask().execute(cc, cr);
	}

	private class ReadCalendarTask extends
			AsyncTask<Integer, Void, HashMap<Long, List<CalendarEvent>>> {
		@Override
		protected void onPreExecute() {
			calendarEvents = null;
			super.onPreExecute();
		}

		protected HashMap<Long, List<CalendarEvent>> doInBackground(
				Integer... colRow) {
			return readCalendar(colRow[0], colRow[1]);
		}

		@Override
		protected void onPostExecute(HashMap<Long, List<CalendarEvent>> result) {
			calendarEvents = result;
			postInvalidate();
		}

		private HashMap<Long, List<CalendarEvent>> readCalendar(Integer cCol,
				Integer cRow) {
			ContentResolver contentResolver = getContext().getContentResolver();
			HashMap<Long, List<CalendarEvent>> myCalendarEvents = new HashMap<Long, List<CalendarEvent>>();
			Calendar calStart = Calendar.getInstance();
			calStart.add(Calendar.DAY_OF_YEAR, -cCol * cRow);
			Calendar calEnd = Calendar.getInstance();

			// http://android-agenda-widget.googlecode.com/svn-history/r17/android-calendar-provider/trunk/src/com/everybodyallthetime/android/provider/calendar/CalendarProvider.java
			Uri.Builder builder = Uri.parse(calendarURI + "instances/when/")
					.buildUpon();
			ContentUris.appendId(builder, calStart.getTimeInMillis());
			ContentUris.appendId(builder, calEnd.getTimeInMillis());

			Cursor eventCursor = contentResolver.query(builder.build(),
					new String[] { "title", "begin", "end", "allDay",
							"description" }, null, null,
					"startDay ASC, startMinute ASC");
			if (eventCursor.getCount() > 0) {
				// read in all events and store in a hash by date
				while (eventCursor.moveToNext()) {
					calEnd = Calendar.getInstance();
					CalendarEvent event = new CalendarEvent();
					event.start = Calendar.getInstance();
					event.start.setTimeInMillis(eventCursor.getLong(1));
					event.end = Calendar.getInstance();
					event.end.setTimeInMillis(eventCursor.getLong(2));
					event.text = eventCursor.getString(4);
					Calendar date = (Calendar) event.start.clone();

					// handle events that start very early
					if (date.before(calStart)) {
						date = (Calendar) calStart.clone();
					}
					date.set(Calendar.HOUR_OF_DAY, 0);
					date.set(Calendar.MINUTE, 0);
					date.set(Calendar.SECOND, 0);
					date.set(Calendar.MILLISECOND, 0);

					// handle events that end very late
					if (calEnd.after(event.end)) {
						calEnd = (Calendar) event.end.clone();
					}
					event.title = eventCursor.getString(0);
					setColor(event.text, event.title);
					event.color = p.getColor();
					// handle events that last multiple days
					do {
						List<CalendarEvent> c = myCalendarEvents.get(date
								.getTimeInMillis());
						if (c == null) {
							c = new ArrayList<CalendarEvent>();
						}
						c.add(event);
						myCalendarEvents.put(date.getTimeInMillis(), c);
						date.add(Calendar.DAY_OF_MONTH, 1);
					} while (date.before(calEnd));
				}
			}
			eventCursor.close();
			return myCalendarEvents;
		}
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
				cell.j = cell.M * cell.N - d - cell.i * cell.M - 1;
			}
			return cell;
		}

		public Calendar getDate(double x, double y) {
			Calendar now = Calendar.getInstance();
			int daysFromOrigin = (int) (Math.floor(x * N) * M + Math.floor(y
					* M));
			int daysFromNow = M * N - daysFromOrigin - 1;
			now.add(Calendar.DAY_OF_YEAR, -1 * daysFromNow);
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
			rect.left = cell.i * xstep + 2;
			rect.top = cell.j * ystep + 2;
			rect.right = rect.left + xstep - 4;
			if (fill) {
				rect.bottom = rect.top + ystep - 4;
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
		// this is going to be a view that can handle the past year
		// what it actually shows will be in a setting
		int xstep = (int) (w / cc + 0.5);
		int ystep = (int) (h / cr + 0.5);
		p.setTextSize(Math.min(ystep - 5, xstep - 10));

		highlight(canvas, xstep, ystep, Calendar.SATURDAY,
				Color.argb(128, 192, 192, 192), false);
		highlight(canvas, xstep, ystep, Calendar.SUNDAY,
				Color.argb(128, 255, 192, 192), false);

		if (calendarEvents != null) {
			drawEvents(canvas, xstep, ystep);
		}
		drawMonths(canvas, xstep, ystep);
	}

	private void drawEvents(Canvas canvas, int xstep, int ystep) {
		// show the events as coloured rects in the grid
		int width, eventCount, pillCount;
		p.setAlpha(255);
		for (Long millis : calendarEvents.keySet()) {
			Calendar d = Calendar.getInstance();
			d.setTimeInMillis(millis);
			cell.get(d);
			eventCount = 0;
			pillCount = 0;
			for (CalendarEvent event : calendarEvents.get(millis)) {
				if (event.title.equalsIgnoreCase(getContext().getString(
						R.string.calendar_pill_title))) {
					pillCount++;
				} else {
					eventCount++;
				}
			}
			width = (int) ((float) (xstep - eventCount) / eventCount);
			if (width < 1) {
				// At certain day representations, the width
				// becomes zero with more than one event per day.
				// show that something is not normal
				for (int i = 1; i < xstep; i++) {
					p.setColor((i % 2 == 0) ? Color.RED : Color.WHITE);
					rect.left = cell.i * xstep + i;
					rect.top = cell.j * ystep + i;
					rect.right = rect.left + xstep - 2 * i;
					rect.bottom = rect.top + ystep - 2 * i;
					canvas.drawRect(rect, p);
				}
			} else {
				int i = 0;
				for (CalendarEvent event : calendarEvents.get(millis)) {
					if (!event.title.equalsIgnoreCase(getContext().getString(
							R.string.calendar_pill_title))) {
						p.setColor(event.color);
						rect.left = cell.i * xstep - xstep + (i + eventCount)
								* xstep / eventCount + 1;
						rect.top = cell.j * ystep + 1;
						rect.right = rect.left + width;
						rect.bottom = rect.top + ystep - 1;
						canvas.drawRect(rect, p);
						i++;
					}
				}
			}
			if (pillCount > 0) {
				p.setColor((eventCount == 0) ? Color.WHITE
						: getForeGroundColorBasedOnBGBrightness(p.getColor()));
				String s = Integer.valueOf(pillCount).toString();
				drawText(canvas, xstep, ystep, s);
			}
		}
	}

	// Adapted from:
	// http://tech.chitgoks.com/2010/07/27/check-if-color-is-dark-or-light-using-java/
	private static int getBrightness(int c) {
		return (int) Math.sqrt(Color.red(c) * Color.red(c) * .241
				+ Color.green(c) * Color.green(c) * .691 + Color.blue(c)
				* Color.blue(c) * .068);
	}

	// From:
	// http://tech.chitgoks.com/2010/07/27/check-if-color-is-dark-or-light-using-java/
	public static int getForeGroundColorBasedOnBGBrightness(int color) {
		if (getBrightness(color) < 130) {
			return Color.WHITE;
		} else {
			return Color.BLACK;
		}
	}

	private void drawMonths(Canvas canvas, int xstep, int ystep) {
		// mark beginnings of month, I want these on top.
		Calendar first = Calendar.getInstance();
		first.set(Calendar.DAY_OF_MONTH, 1);
		// mark first of the month
		for (int m = (int) (12.0 * cc * cr / 365 + 1); m > 0; m--) {
			cell.get(first);

			// fill the cell with ltgray
			p.setColor(Color.LTGRAY);
			p.setAlpha(128); // let other things shine through
			rect.left = cell.i * xstep + 1;
			rect.top = cell.j * ystep + 1;
			rect.right = rect.left + xstep - 1;
			rect.bottom = rect.top + ystep - 1;
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
			drawText(canvas, xstep, ystep, s);
			first.add(Calendar.MONTH, -1);
		}
	}

	private void drawText(Canvas canvas, int xstep, int ystep, String s) {
		p.getTextBounds(s, 0, s.length(), bounds);
		canvas.drawText(s, cell.i * xstep + (xstep - bounds.width()) / 2,
				(cell.j) * ystep + ystep - (ystep - bounds.height()) / 2, p);
	}

	private void setColor(String description, String title) {
		// TODO: Find a way to read the preferences only when they change.
		if (title.equalsIgnoreCase(getContext().getString(
				R.string.calendar_entry_title))) {
			String e = Utils.parseDescription(description, getContext()
					.getString(R.string.ernst));
			int ernst = 0;
			try {
				ernst = Integer.valueOf(e);
			} catch (NumberFormatException nfe) {
				if (e.equalsIgnoreCase(getContext().getString(R.string.laag))) {
					ernst = 0;
				} else if (e.equalsIgnoreCase(getContext().getString(
						R.string.gemiddeld))) {
					ernst = 1;
				} else {
					ernst = 2;
				}
			}
			if (ernst == 0) {
				p.setColor(getContext().getSharedPreferences(
						Utils.GENERAL_PREFS_NAME, 0).getInt("pref_low", 0xffffff00));
			} else if (ernst == 1) {
				p.setColor(getContext().getSharedPreferences(
						Utils.GENERAL_PREFS_NAME, 0).getInt("pref_average", 0xffff00ff));
			} else if (ernst == 2) {
				p.setColor(getContext().getSharedPreferences(
						Utils.GENERAL_PREFS_NAME, 0).getInt("pref_high", 0xffff0000));
			}
		} else if (title.equalsIgnoreCase(getContext().getString(
				R.string.calendar_pill_title))) {
			p.setColor(Color.WHITE);
		} else {
			p.setColor(getContext().getSharedPreferences(
					Utils.GENERAL_PREFS_NAME, 0).getInt("pref_other", 0xff888888));
		}
	}

	@Override
	public boolean onDown(MotionEvent e) {
		Log.i("hcv", "Down from calendar");
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		Log.i("hcv", "Fling from calendar");
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		SetCalendarDisplayDialog scdd = new SetCalendarDisplayDialog();
		Bundle args = new Bundle();
		args.putInt("rows", this.cr);
		args.putInt("columns", this.cc);
		scdd.setArguments(args);
		scdd.show(
				((FragmentActivity) getContext()).getSupportFragmentManager(),
				null);
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		Log.i("hcv", "Scroll from calendar");
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// are there relevant events in the calendar?
		Calendar[] days = new Calendar[1];
		days[0] = this.cell.getDate(e.getX() / this.getWidth(),
				e.getY() / this.getHeight());
		final List<String> hits = Utils.getEvents(
				getContext().getString(R.string.calendar_entry_title), days,
				getContext().getContentResolver());
		if (hits.size() > 0) {
			HeadacheCalendarView.this
					.playSoundEffect(SoundEffectConstants.CLICK);
			final HeadacheAttack attack = ((MainActivity) getContext())
					.getAttack();
			if (hits.size() == 1) {
				showAttack(hits.get(0), attack);
			} else {
				// we need to popup a list dialog
				List<String> summary = Utils.createSummary(hits, getContext());
				AlertDialog.Builder builder = new AlertDialog.Builder(
						getContext());
				builder.setTitle(getContext().getString(R.string.multiple_attacks));
				builder.setAdapter(new ArrayAdapter<String>(getContext(),
						// to remove white on white with froyo, 
						// use select_dialog_item. I had simple_list_item_1 before
						android.R.layout.select_dialog_item, summary), 
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								showAttack(hits.get(which), attack);
							}
						});
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		}
		return true;
	}

	private void showAttack(String description, HeadacheAttack attack) {
		try {
			Utils.readAttack(attack, description, getContext());
			Toast.makeText(
					getContext(),
					Utils.format(attack.start) + " - "
							+ Utils.format(attack.end), Toast.LENGTH_LONG)
					.show();
			((MainActivity) getContext()).setWorkingOnNewHeadache(false);
			((MainActivity) getContext()).repaintTabs();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
