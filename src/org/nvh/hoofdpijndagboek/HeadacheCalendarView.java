package org.nvh.hoofdpijndagboek;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
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
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

// TODO: Cleanup unused code

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
									HeadacheCalendarView.this.invalidate();
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
	SimpleDateFormat df = new SimpleDateFormat("d");
	Rect rect = new Rect(); // lint says to create up front and reuse
	Rect bounds = new Rect();
	Cell cell = new Cell(cc, cr);
	HashMap<Long, List<CalendarEvent>> calendarEvents = new HashMap<Long, List<CalendarEvent>>();
	String calendarURI = null;
	private int renderingMode;

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
		renderingMode = 1;
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

		public Calendar getDate(double x, double y) {
			Calendar now = Calendar.getInstance();
			int daysFromOrigin = (int) (Math.floor(x * N) * M + Math.floor(y
					* M));
			int daysFromNow = M * N - daysFromOrigin - 1;
			now.roll(Calendar.DAY_OF_YEAR, -1 * daysFromNow);
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

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		w = getWidth();
		h = getHeight();
		p.setColor(Color.BLACK);
		p.setStyle(Style.FILL);
		canvas.drawPaint(p);
		renderingMode = 1;
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
		p.setTextSize(Math.min(ystep - 5, xstep - 2));

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
		calendarEvents.clear();
		Calendar calStart = Calendar.getInstance();
		calStart.add(Calendar.DAY_OF_YEAR, -cc * cr);
		Calendar calEnd = Calendar.getInstance();

		// http://android-agenda-widget.googlecode.com/svn-history/r17/android-calendar-provider/trunk/src/com/everybodyallthetime/android/provider/calendar/CalendarProvider.java
		Uri.Builder builder = Uri.parse(calendarURI + "instances/when/")
				.buildUpon();
		ContentUris.appendId(builder, calStart.getTimeInMillis());
		ContentUris.appendId(builder, calEnd.getTimeInMillis());

		Cursor eventCursor = contentResolver
				.query(builder.build(), new String[] { "title", "begin", "end",
						"allDay", "description" }, null, null,
						"startDay ASC, startMinute ASC");
		if (eventCursor.getCount() > 0) {
			String[] lgh = new String[] {
					getContext().getString(R.string.laag),
					getContext().getString(R.string.gemiddeld),
					getContext().getString(R.string.hoog) };

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
				setColor(lgh, event.text, event.title);
				event.color = p.getColor();
				// handle events that last multiple days
				do {
					List<CalendarEvent> c = calendarEvents.get(date
							.getTimeInMillis());
					if (c == null) {
						c = new ArrayList<CalendarEvent>();
					}
					c.add(event);
					calendarEvents.put(date.getTimeInMillis(), c);
					date.add(Calendar.DAY_OF_MONTH, 1);
				} while (date.before(calEnd));
			}

			// show the events as coloured rects in the grid
			int width, eventCount, pillCount;
			for (Long millis : calendarEvents.keySet()) {
				Calendar d = Calendar.getInstance();
				d.setTimeInMillis(millis);
				cell.get(d);
				if (renderingMode == 0) {
					width = (int) ((float) (xstep - 2) / calendarEvents.get(
							millis).size());
					eventCount = 0;
					for (CalendarEvent event : calendarEvents.get(millis)) {
						p.setColor(event.color);
						p.setAlpha(255);
						rect.left = cell.i * xstep + 3
								+ ((eventCount > 0) ? 1 : 0) + eventCount
								* width;
						rect.top = cell.j * ystep + 5;
						rect.right = rect.left + width - 6;
						rect.bottom = rect.top + ystep - 10;
						canvas.drawRect(rect, p);
						eventCount++;
					}
				} else if (renderingMode == 1 || renderingMode == 2) {
					// we do nearly the same but use a number for the pills
					eventCount = 0;
					pillCount = 0;
					for (CalendarEvent event : calendarEvents.get(millis)) {
						if (event.title.equalsIgnoreCase(getContext()
								.getString(R.string.calendar_pill_title))) {
							pillCount++;
						} else {
							eventCount++;
						}
					}
					if (renderingMode == 1) {
						width = (int) ((float) (xstep - 2) / eventCount);
						eventCount = 0;
						for (CalendarEvent event : calendarEvents.get(millis)) {
							if (!event.title.equalsIgnoreCase(getContext()
									.getString(R.string.calendar_pill_title))) {
								p.setColor(event.color);
								p.setAlpha(255);
								rect.left = cell.i * xstep + 3
										+ ((eventCount > 0) ? 1 : 0)
										+ eventCount * width;
								rect.top = cell.j * ystep + 5;
								rect.right = rect.left + width - 6;
								rect.bottom = rect.top + ystep - 10;
								canvas.drawRect(rect, p);
								eventCount++;
							}
						}
					} else {
						// we try to show smaller rectangles at the correct time
						// using transparency we handle overlaps
						// I am not for this, they are too small, and
						// if we keep them from overlapping (later) they
						// will only get smaller.
						width = xstep - 2;
						for (CalendarEvent event : calendarEvents.get(millis)) {
							if (!event.title.equalsIgnoreCase(getContext()
									.getString(R.string.calendar_pill_title))) {
								p.setColor(event.color);
								p.setAlpha(128);
								rect.left = cell.i * xstep + 3;
								rect.right = rect.left + width - 6;
								rect.top = cell.j
										* ystep
										+ 5
										+ getYPosition(ystep - 10,
												event.start.getTimeInMillis(),
												millis);
								rect.bottom = cell.j
										* ystep
										+ 5
										+ getYPosition(ystep - 10,
												event.end.getTimeInMillis(),
												millis);
								canvas.drawRect(rect, p);
							}
						}
					}
					if (pillCount > 0) {
						p.setColor(Color.WHITE);
						p.setAlpha(128);
						String s = Integer.valueOf(pillCount).toString();
						drawText(canvas, xstep, ystep, s);
					}
				}
			}
			eventCursor.close();

			// mark beginnings of month, I want these on top.
			Calendar first = Calendar.getInstance();
			first.set(Calendar.DAY_OF_MONTH, 1);
			// mark first of the month
			for (int m = (int) (12.0 * cc * cr / 365 + 1); m > 0; m--) {
				cell.get(first);

				// fill the cell with ltgray
				p.setColor(Color.LTGRAY);
				rect.left = cell.i * xstep + 3;
				rect.top = cell.j * ystep + 5;
				rect.right = rect.left + xstep - 6;
				rect.bottom = rect.top + ystep - 10;
				canvas.drawRect(rect, p);

				if (first.get(Calendar.MONTH) == Calendar.JANUARY) {
					// new year
					p.setColor(Color.YELLOW);
				} else {
					// a red top
					p.setColor(Color.RED);
				}
				rect.bottom = rect.top + ystep / 10;
				canvas.drawRect(rect, p);

				// the name or number of the month
				p.setColor(Color.BLACK);
				String s = f.format(first.getTime());
				drawText(canvas, xstep, ystep, s);
				first.add(Calendar.MONTH, -1);
			}
		}
	}

	private int getYPosition(int heightOfCellInPixels, long timeInMillis,
			long calendarCellInMillis) {
		if (timeInMillis < calendarCellInMillis) {
			// multiple day event started earlier
			return 0;
		}
		if (timeInMillis >= (calendarCellInMillis + 24 * 60 * 60 * 1000)) {
			// multiple day event continues on next day
			return heightOfCellInPixels;
		}
		float y = (float) heightOfCellInPixels
				* (timeInMillis - calendarCellInMillis) / (24 * 60 * 60 * 1000);
		return (int) y;
	}

	private void drawText(Canvas canvas, int xstep, int ystep, String s) {
		p.getTextBounds(s, 0, s.length(), bounds);
		canvas.drawText(s, cell.i * xstep + (xstep - bounds.width()) / 2,
				(cell.j) * ystep + ystep - (ystep - bounds.height()) / 2, p);
	}

	private void setColor(String[] lgh, String description, String title) {
		if (title.equalsIgnoreCase(getContext().getString(
				R.string.calendar_entry_title))) {
			int ernst = Utils.getArrayIndex(
					lgh,
					Utils.parseDescription(description,
							getContext().getString(R.string.ernst)));
			if (ernst == 0) {
				p.setColor(Color.MAGENTA);
			} else if (ernst == 1) {
				p.setColor(Color.YELLOW);
			} else if (ernst == 2) {
				p.setColor(Color.RED);
			}
		} else if (title.equalsIgnoreCase(getContext().getString(
				R.string.calendar_pill_title))) {
			p.setColor(Color.WHITE);
		} else {
			p.setColor(Color.LTGRAY);
		}
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
		// TODO: Depending on the number of days we are displaying
		// we may want to extend the days with neighbouring dates.
		Calendar[] days = new Calendar[1];
		days[0] = this.cell.getDate(e.getX() / this.getWidth(),
				e.getY() / this.getHeight());
		List<String> hits = Utils.getEvents(
				getContext().getString(R.string.calendar_entry_title), days,
				getContext().getContentResolver());
		if (hits.size() > 0) {
			// Get instance of Vibrator from current Context
			// Vibrator v = (Vibrator)
			// getContext().getSystemService(Context.VIBRATOR_SERVICE);
			// v.vibrate(50);
			HeadacheCalendarView.this
					.playSoundEffect(SoundEffectConstants.CLICK);
			HeadacheAttack attack = ((MainActivity) getContext()).getAttack();
			String description = hits.get(0);

			// this should go into utils
			attack.start = handleDateAndTime(description, "begin");
			attack.end = handleDateAndTime(description, "end");
			attack.ernst = Utils.parseDescription(description, getContext()
					.getString(R.string.ernst));
			attack.leftPainPoints = Utils.parseDescriptionOuch(description,
					getContext().getString(R.string.left), getContext()
							.getString(R.string.ouch));
			attack.rightPainPoints = Utils.parseDescriptionOuch(description,
					getContext().getString(R.string.right), getContext()
							.getString(R.string.ouch));
			attack.misselijk = Utils.parseDescription(description,
					getContext().getString(R.string.misselijk))
					.equalsIgnoreCase(getContext().getString(R.string.ja));
			attack.menstruatie = Utils.parseDescription(description,
					getContext().getString(R.string.menstruatie))
					.equalsIgnoreCase(getContext().getString(R.string.ja));
			attack.doorslapen = Utils.parseDescription(description,
					getContext().getString(R.string.doorslapen))
					.equalsIgnoreCase(getContext().getString(R.string.ja));
			attack.duizelig = Utils.parseDescription(description,
					getContext().getString(R.string.duizelig))
					.equalsIgnoreCase(getContext().getString(R.string.ja));
			attack.geur = Utils.parseDescription(description,
					getContext().getString(R.string.geur)).equalsIgnoreCase(
					getContext().getString(R.string.ja));
			attack.inslapen = Utils.parseDescription(description,
					getContext().getString(R.string.inslapen))
					.equalsIgnoreCase(getContext().getString(R.string.ja));
			attack.licht = Utils.parseDescription(description,
					getContext().getString(R.string.licht)).equalsIgnoreCase(
					getContext().getString(R.string.ja));
			attack.stoelgang = Utils.parseDescription(description,
					getContext().getString(R.string.stoelgang))
					.equalsIgnoreCase(getContext().getString(R.string.ja));
			attack.humeur = Utils.parseDescription(description, getContext()
					.getString(R.string.humeur));
			attack.weer = Utils.parseDescription(description, getContext()
					.getString(R.string.weer));
			((MainActivity) getContext()).setWorkingOnNewHeadache(false);
			((MainActivity) getContext()).repaintTabs();
		}

		// TODO: Handle multiple hits by showing a list
		return true;
	}

	private Calendar handleDateAndTime(String description, String field) {
		String start = Utils.parseDescription(description, field);
		Date aDate;
		try {
			aDate = Utils.parse(start,
					getContext().getString(R.string.very_long_date_time));
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(aDate.getTime());
			return c;
		} catch (ParseException e1) {
		}
		return null;
	}
}
