package org.nvh.hoofdpijndagboek;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class Utils {
	public static final String PREFS_NAME = "PillsFile";

	public static List<String> getPills(FragmentActivity activity) {
		SharedPreferences prefs = activity.getSharedPreferences(
				Utils.PREFS_NAME, 0);
		if (prefs.getString("pill0", null) == null) {
			Editor e = prefs.edit();
			e.putString("pill0", activity.getString(R.string.new_pill));
			e.commit();
		}

		List<String> pillNames = new ArrayList<String>();
		for (int n = 0; n < 100; n++) {
			String key = String.format("pill%d", n);
			String value = prefs.getString(key, null);
			if (value != null)
				pillNames.add(value);
		}

		Collections.sort(pillNames);
		return pillNames;
	}

	public static void saveMedication(String medication,
			FragmentActivity activity) {
		SharedPreferences prefs = activity.getSharedPreferences(
				Utils.PREFS_NAME, 0);
		int counter = 1;
		String key = String.format("pill%d", counter);
		while (prefs.contains(key)) {
			counter++;
			key = String.format("pill%d", counter);
		}
		prefs.edit().putString(key, medication).commit();
	}

	public static void saveAllMedication(List<String> pills,
			FragmentActivity activity) {
		SharedPreferences prefs = activity.getSharedPreferences(
				Utils.PREFS_NAME, 0);
		prefs.edit().clear().commit();
		prefs.edit().putString("pill0", activity.getString(R.string.new_pill))
				.commit();
		int counter = 1;
		for (String s : pills) {
			if (!s.equalsIgnoreCase(activity.getString(R.string.new_pill))) {
				String key = String.format("pill%d", counter);
				prefs.edit().putString(key, s).commit();
				counter++;
			}
		}
	}

	public static String parseDescription(String description, String what) {
		if (description == null)
			return "";
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
			return answer;
		}
		return "";
	}

	public static int getArrayIndex(String[] stringArray, String string) {
		int i = 0;
		while (!stringArray[i].equalsIgnoreCase(string)) {
			i++;
			if (i >= stringArray.length) {
				return -1;
			}
		}

		return i;
	}

	public static Date parse(String in, String format) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		Date date = dateFormat.parse(in);
		return date;
	}

	public static void deleteFromCalendar(ContentResolver contentResolver,
			String title, Date when) {
		String uriBase = Utils.getCalendarUriBase(contentResolver);
		Uri.Builder builder = Uri.parse(uriBase + "instances/when/")
				.buildUpon();
		Calendar before = Calendar.getInstance();
		before.setTimeInMillis(when.getTime());
		before.add(Calendar.MINUTE, -5);
		ContentUris.appendId(builder, before.getTimeInMillis());
		before.add(Calendar.MINUTE, 10);
		ContentUris.appendId(builder, before.getTimeInMillis());

		Cursor eventCursor = contentResolver.query(builder.build(),
				new String[] { "title", "begin", "event_id" }, null, null,
				"startDay ASC, startMinute ASC");

		long id = 0;
		if (eventCursor.getCount() > 0) {
			while (eventCursor.moveToNext()) {
				if (eventCursor.getString(0).equalsIgnoreCase(title)
						&& when.getTime() == eventCursor.getLong(1)) {
					id = eventCursor.getLong(2);
				}
			}
		}
		eventCursor.close();
		Uri eventsUri = Uri.parse(uriBase + "events");
		Uri eventUri = ContentUris.withAppendedId(eventsUri, id);
		int rows = contentResolver.delete(eventUri, null, null);
		if (rows == 0) {
			Log.e("Utils", eventUri.toString());
		}
	}

	/*
	 * Determines if it's a pre 2.1 or a 2.2 calendar Uri, and returns the Uri
	 */
	public static String getCalendarUriBase(ContentResolver cr) {
		String calendarUriBase = null;
		Uri calendars = Uri.parse("content://calendar/calendars");
		Cursor managedCursor = null;
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

	public static int getNumberOfAttacks(Calendar start, Calendar stop,
			ContentResolver contentResolver, String title) {
		Uri.Builder builder = Uri.parse(
				Utils.getCalendarUriBase(contentResolver) + "instances/when/")
				.buildUpon();
		ContentUris.appendId(builder, start.getTimeInMillis());
		ContentUris.appendId(builder, stop.getTimeInMillis());

		Cursor eventCursor = contentResolver.query(builder.build(),
				new String[] { "title", "begin", "end", "description" },
				"title='"+ title +"'", null,
				"startDay ASC, startMinute ASC");
		return eventCursor.getCount();
	}

	public static List<String> getEvents(String title, Calendar[] days,
			ContentResolver contentResolver) {
		List<String> result = new ArrayList<String>();
		// TODO: find the min and max days from the days array
		// TODO: for events spanning multiple days check if the days are in it.
		Calendar min = (Calendar) days[0].clone();
		Calendar max = (Calendar) days[0].clone();
		min.add(Calendar.DAY_OF_MONTH, -1);
		max.add(Calendar.DAY_OF_MONTH, 1);
		Uri.Builder builder = Uri.parse(
				Utils.getCalendarUriBase(contentResolver) + "instances/when/")
				.buildUpon();
		ContentUris.appendId(builder, min.getTimeInMillis());
		ContentUris.appendId(builder, max.getTimeInMillis());

		Cursor eventCursor = contentResolver.query(builder.build(),
				new String[] { "title", "begin", "end", "description" }, null,
				null, "startDay ASC, startMinute ASC");
		if (eventCursor.getCount() > 0) {
			Calendar c = Calendar.getInstance();
			while (eventCursor.moveToNext()) {
				if (eventCursor.getString(0).equalsIgnoreCase(title)) {
					// Is the begin within one of the days?
					// then put in the result.
					Calendar event = Calendar.getInstance();
					event.setTimeInMillis(eventCursor.getLong(1));
					if (happensIn(days, event)) {
						String description = eventCursor.getString(3);
						if (!description.endsWith("\n")) {
							description += "\n";// my phone will not save this
												// whitespace
						}
						c.setTimeInMillis(eventCursor.getLong(1));
						description += "begin:" + Utils.format(c) + "\n";
						c.setTimeInMillis(eventCursor.getLong(2));
						description += "end:" + Utils.format(c);
						result.add(description);
					}
				}
			}
			eventCursor.close();
		}
		return result;
	}

	private static String format(Calendar c) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return sdf.format(c.getTime());
	}

	private static boolean happensIn(Calendar[] days, Calendar event) {
		Calendar dayOnly = Calendar.getInstance();
		for (Calendar day : days) {
			dayOnly.set(day.get(Calendar.YEAR), day.get(Calendar.MONTH),
					day.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
			Calendar nextDay = (Calendar) dayOnly.clone();
			nextDay.add(Calendar.DAY_OF_MONTH, 1);
			if (event.after(dayOnly) && event.before(nextDay)) {
				return true;
			}
		}
		return false;
	}

	public static List<PainPoint> parseDescriptionOuch(String description,
			String side, String ouch) {
		if (description == null)
			return null;
		if (description.contains(side)) {
			// find the newline and read until
			// the string does not start with ouch
			int p1 = description.indexOf(side) + side.length() + 1;
			int p2 = description.indexOf('\n', p1);
			if (p2 == -1) {
				// no pain on this side
				return new ArrayList<PainPoint>(0);
			} else {
				List<PainPoint> points = new ArrayList<PainPoint>();
				String pain = "", split;
				do {
					p1 = description.indexOf('\n', p2 + 1);
					pain = description.substring(p2 + 1, p1);
					p2 = p1;
					if (pain.startsWith(ouch)) {
						p1 = pain.indexOf(':');
						split = pain.substring(p1 + 1);
						String[] splitted = split.split(";");
						PainPoint p = new PainPoint();
						p.x = Float.parseFloat(splitted[0]);
						p.y = Float.parseFloat(splitted[1]);
						p.color = Integer.parseInt(splitted[2]);
						points.add(p);
					}
				} while (pain.startsWith(ouch));
				return points;
			}
		}
		return null;
	}
}
