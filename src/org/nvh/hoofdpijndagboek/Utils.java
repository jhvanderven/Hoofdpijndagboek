package org.nvh.hoofdpijndagboek;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class Utils {
	public static final String PREFS_NAME = "PillsFile";
	public static final String GENERAL_PREFS_NAME = "org.nvh.hoofdpijndagboek_preferences";

	public static void readAttack(HeadacheAttack attack, String description,
			Context context) throws Exception {
		attack.start = handleDateAndTime(description, "begin", context);
		attack.end = handleDateAndTime(description, "end", context);
		attack.ernst = Utils.parseDescription(description,
				context.getString(R.string.ernst));
		attack.leftPainPoints = Utils.parseDescriptionOuch(description,
				context.getString(R.string.left),
				context.getString(R.string.ouch));
		attack.rightPainPoints = Utils.parseDescriptionOuch(description,
				context.getString(R.string.right),
				context.getString(R.string.ouch));
		attack.misselijk = Utils.parseDescription(description,
				context.getString(R.string.misselijk)).equalsIgnoreCase(
				context.getString(R.string.ja));
		attack.menstruatie = Utils.parseDescription(description,
				context.getString(R.string.menstruatie)).equalsIgnoreCase(
				context.getString(R.string.ja));
		attack.doorslapen = Utils.parseDescription(description,
				context.getString(R.string.doorslapen)).equalsIgnoreCase(
				context.getString(R.string.ja));
		attack.duizelig = Utils.parseDescription(description,
				context.getString(R.string.duizelig)).equalsIgnoreCase(
				context.getString(R.string.ja));
		attack.geur = Utils.parseDescription(description,
				context.getString(R.string.geur)).equalsIgnoreCase(
				context.getString(R.string.ja));
		attack.inslapen = Utils.parseDescription(description,
				context.getString(R.string.inslapen)).equalsIgnoreCase(
				context.getString(R.string.ja));
		attack.licht = Utils.parseDescription(description,
				context.getString(R.string.licht)).equalsIgnoreCase(
				context.getString(R.string.ja));
		attack.stoelgang = Utils.parseDescription(description,
				context.getString(R.string.stoelgang)).equalsIgnoreCase(
				context.getString(R.string.ja));
		attack.humeur = Utils.parseDescription(description,
				context.getString(R.string.humeur));
		attack.weer = Utils.parseDescription(description,
				context.getString(R.string.weer));
	}

	private static Calendar handleDateAndTime(String description, String field,
			Context context) {
		String start = Utils.parseDescription(description, field);
		Date aDate;
		try {
			aDate = Utils.parse(start,
					context.getString(R.string.very_long_date_time));
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(aDate.getTime());
			return c;
		} catch (ParseException e1) {
		}
		return null;
	}

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
		String lDescription = description.toLowerCase();
		String lWhat = what.toLowerCase();
		if (lDescription.contains(lWhat)) {
			// find the entry and its :
			int p1 = lDescription.indexOf(lWhat) + lWhat.length() + 1;
			int p2 = lDescription.indexOf('\n', p1);
			String answer = null;
			if (p2 == -1) {
				answer = lDescription.substring(p1);
			} else {
				answer = lDescription.substring(p1, p2);
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
				"title='" + title + "'", null, "startDay ASC, startMinute ASC");
		return eventCursor.getCount();
	}

	public static List<String> getEvents(String title, Calendar[] days,
			ContentResolver contentResolver) {
		List<String> result = new ArrayList<String>();
		Calendar min = (Calendar) days[0].clone();
		Calendar max = (Calendar) days[0].clone();

		// TODO: How much time are we allowing here?
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
					Calendar eventStart = Calendar.getInstance();
					eventStart.setTimeInMillis(eventCursor.getLong(1));
					Calendar eventEnd = Calendar.getInstance();
					eventEnd.setTimeInMillis(eventCursor.getLong(2));
					if (happensIn(days, eventStart, eventEnd)) {
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

	public static int getColor(String ernst, Context context) {
		if (ernst.equalsIgnoreCase(context.getString(R.string.laag))) {
			return HeadacheDiaryApp.getApp()
					.getSharedPreferences(Utils.GENERAL_PREFS_NAME, 0)
					.getInt("pref_low", 0);
		} else if (ernst
				.equalsIgnoreCase(context.getString(R.string.gemiddeld))) {
			return HeadacheDiaryApp.getApp()
					.getSharedPreferences(Utils.GENERAL_PREFS_NAME, 0)
					.getInt("pref_average", 0);
		} else if (ernst.equalsIgnoreCase(context.getString(R.string.hoog))) {
			return HeadacheDiaryApp.getApp()
					.getSharedPreferences(Utils.GENERAL_PREFS_NAME, 0)
					.getInt("pref_high", 0);
		}
		return 0;
	}

	public static String format(Calendar c) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return sdf.format(c.getTime());
	}

	public static String formatNoYear(Calendar c) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM HH:mm");
		return sdf.format(c.getTime());
	}

	private static boolean happensIn(Calendar[] days, Calendar s2, Calendar e2) {
		Calendar s1 = Calendar.getInstance();
		for (Calendar day : days) {
			s1.set(day.get(Calendar.YEAR), day.get(Calendar.MONTH),
					day.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
			Calendar e1 = (Calendar) s1.clone();
			e1.add(Calendar.DAY_OF_MONTH, 1);
			if (e2.before(s1) || s2.after(e1)) {
				return false;
			}
		}
		return true;
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
						p.colorIndex = Integer.parseInt(splitted[2]);
						// translate old colors to indices
						if (p.colorIndex == -256) {
							p.colorIndex = 0;
						} else if (p.colorIndex == -65536) {
							p.colorIndex = 2;
						} else if (p.colorIndex == -65281) {
							p.colorIndex = 1;
						}
						points.add(p);
					}
				} while (pain.startsWith(ouch));
				return points;
			}
		}
		return null;
	}

	public static List<String> createSummary(List<String> hits, Context context) {
		List<String> s = new ArrayList<String>();
		HeadacheAttack a = new HeadacheAttack();
		for (String hit : hits) {
			try {
				Utils.readAttack(a, hit, context);
				// now put in what we can show in a summary
				String item = Utils.formatNoYear(a.start) + " - "
						+ Utils.formatNoYear(a.end) + " : " + a.ernst;
				s.add(item);
			} catch (Exception e) {
			}
		}
		return s;
	}

	public static List<String> getEventsAsOf(String title, Calendar start,
			ContentResolver contentResolver) {
		List<String> result = new ArrayList<String>();
		Calendar min = start;
		Calendar max = Calendar.getInstance();
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
			eventCursor.close();
		}
		return result;
	}

	public static void saveHeadacheEvents(File file, Activity activity) {
		// how long should we go back? 10 years.
		Calendar start = Calendar.getInstance();
		start.add(Calendar.YEAR, -10);
		List<String> events = Utils.getEventsAsOf(
				activity.getString(R.string.calendar_entry_title), start,
				activity.getContentResolver());
		if (events.size() == 0) {
			return;
		}
		BufferedWriter bw;
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "UTF8"));
			StringBuilder sb = new StringBuilder();
			// columns
			sb.append("\"").append(activity.getString(R.string.start))
					.append("\",").append("\"")
					.append(activity.getString(R.string.end)).append("\",")
					.append("\"").append(activity.getString(R.string.ernst))
					.append("\",").append("\"")
					.append(activity.getString(R.string.menstruatie))
					.append("\",").append("\"")
					.append(activity.getString(R.string.misselijk))
					.append("\",").append("\"")
					.append(activity.getString(R.string.licht)).append("\",")
					.append("\"").append(activity.getString(R.string.duizelig))
					.append("\",").append("\"")
					.append(activity.getString(R.string.geur)).append("\",")
					.append("\"").append(activity.getString(R.string.inslapen))
					.append("\",").append("\"")
					.append(activity.getString(R.string.doorslapen))
					.append("\",").append("\"")
					.append(activity.getString(R.string.stoelgang))
					.append("\",").append("\"")
					.append(activity.getString(R.string.weer)).append("\",")
					.append("\"").append(activity.getString(R.string.humeur))
					.append("\",");
			for (int i = 0; i < 10; i++) {
				sb.append("\"")
						.append(activity.getString(R.string.left) + ".x[")
						.append(i).append("]\",");
				sb.append("\"")
						.append(activity.getString(R.string.left) + ".y[")
						.append(i).append("]\",");
				sb.append("\"")
						.append(activity.getString(R.string.left) + ".ci[")
						.append(i).append("]\",");
			}
			for (int i = 0; i < 10; i++) {
				sb.append("\"")
						.append(activity.getString(R.string.right) + ".x[")
						.append(i).append("]\",");
				sb.append("\"")
						.append(activity.getString(R.string.right) + ".y[")
						.append(i).append("]\",");
				sb.append("\"")
						.append(activity.getString(R.string.right) + ".ci[")
						.append(i).append("]\",");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append("\n");
			for (String event : events) {
				HeadacheAttack attack = new HeadacheAttack();
				try {
					readAttack(attack, event, activity);
					// now write to a csv file
					sb.append("\"").append(Utils.format(attack.start))
							.append("\",").append("\"")
							.append(Utils.format(attack.end)).append("\",")
							.append("\"").append(attack.ernst).append("\",")
							.append("\"").append(attack.menstruatie)
							.append("\",").append("\"")
							.append(attack.misselijk).append("\",")
							.append("\"").append(attack.licht).append("\",")
							.append("\"").append(attack.duizelig).append("\",")
							.append("\"").append(attack.geur).append("\",")
							.append("\"").append(attack.inslapen).append("\",")
							.append("\"").append(attack.doorslapen)
							.append("\",").append("\"")
							.append(attack.stoelgang).append("\",")
							.append("\"").append(attack.weer).append("\",")
							.append("\"").append(attack.humeur).append("\",");
					for (int i = 0; i < Math.min(10,
							attack.leftPainPoints.size()); i++) {
						if (attack.leftPainPoints.size() > i) {
							sb.append(attack.leftPainPoints.get(i).x)
									.append(",")
									.append(attack.leftPainPoints.get(i).y)
									.append(",")
									.append(attack.leftPainPoints.get(i).colorIndex)
									.append(",");
						} else {
							sb.append(",").append(",").append(",");
						}
					}
					for (int i = 0; i < Math.min(10,
							attack.rightPainPoints.size()); i++) {
						if (attack.rightPainPoints.size() > i) {
							sb.append(attack.rightPainPoints.get(i).x)
									.append(",")
									.append(attack.rightPainPoints.get(i).y)
									.append(",")
									.append(attack.rightPainPoints.get(i).colorIndex)
									.append(",");
						} else {
							sb.append(",").append(",").append(",");
						}
					}
					sb.deleteCharAt(sb.length() - 1);
					sb.append("\n");
				} catch (Exception e) {
				}
			}
			bw.write(sb.toString());
			bw.close();
		} catch (FileNotFoundException e) {
			Toast.makeText(activity, e.getLocalizedMessage(), Toast.LENGTH_LONG)
					.show();
		} catch (IOException e) {
			Toast.makeText(activity, e.getLocalizedMessage(), Toast.LENGTH_LONG)
					.show();
		}
	}

	public static Object formatNoSeparators(Calendar c) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
		return sdf.format(c.getTime());
	}

	public static void savePillEvents(File file, Activity activity) {
		// how long should we go back? 10 years.
		Calendar start = Calendar.getInstance();
		start.add(Calendar.YEAR, -10);
		List<String> events = Utils.getEventsAsOf(
				activity.getString(R.string.calendar_pill_title), start,
				activity.getContentResolver());
		if (events.size() == 0) {
			return;
		}
		BufferedWriter bw;
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			// FileWriter fw = new FileWriter(file.getAbsoluteFile());
			bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "UTF8"));
			StringBuilder sb = new StringBuilder();
			// columns
			sb.append("\"").append(activity.getString(R.string.start))
					.append("\",").append("\"")
					.append(activity.getString(R.string.end)).append("\",")
					.append("\"")
					.append(activity.getString(R.string.pill_name))
					.append("\",").append("\"")
					.append(activity.getString(R.string.pill_number))
					.append("\",").append("\"")
					.append(activity.getString(R.string.pill_effect))
					.append("\"\n");
			for (String event : events) {
				try {
					Calendar begin = handleDateAndTime(event, "begin", activity);
					Calendar end = handleDateAndTime(event, "end", activity);
					String medicin = Utils.parseDescription(event,
							activity.getString(R.string.pill_name));
					String number = Utils.parseDescription(event,
							activity.getString(R.string.pill_number));
					String effect = Utils.parseDescription(event,
							activity.getString(R.string.pill_effect));
					sb.append(Utils.format(begin)).append(",")
							.append(Utils.format(end)).append(",")
							.append(medicin).append(",").append(number)
							.append(",").append(effect).append("\n");
				} catch (Exception e) {

				}
			}
			bw.write(sb.toString());
			bw.close();
		} catch (FileNotFoundException e) {
			Toast.makeText(activity, e.getLocalizedMessage(), Toast.LENGTH_LONG)
					.show();
		} catch (IOException e) {
			Toast.makeText(activity, e.getLocalizedMessage(), Toast.LENGTH_LONG)
					.show();
		}
	}

	public static void importHeadaches(Uri fileNameUri, Activity activity) {
		StringBuilder sb = readFile(fileNameUri, activity);
		if (sb == null) {
			return;
		}

		if(sb.length()==0){
			return; // nothing to do
		}
		String[] lines = sb.toString().split("\n");

		// lines[0] is special
		String[] columns = lines[0].split(",");
		for (int i = 0; i < columns.length; i++) {
			columns[i] = columns[i].replaceAll("\"", "");
		}

		// check date format...
		if (lines.length > 1) {
			String[] firstItem = lines[1].split(",");
			// the start date is the first element...
			Calendar start = Calendar.getInstance();
			// TODO: Spreadsheet apps change the format...
			try {
				start.setTimeInMillis(Utils.parse(firstItem[0],
						activity.getString(R.string.very_long_date_time))
						.getTime());
			} catch (ParseException e) {
				Log.e("import", String.format("Cannot import dates formatted like this %s it needs to be %s", firstItem[0],activity.getString(R.string.very_long_date_time)));
				Toast t = Toast.makeText(activity, "App needs dates likes this: " + activity.getString(R.string.very_long_date_time) + " and not like this: " + firstItem[0], Toast.LENGTH_LONG);
				t.getView().setBackgroundColor(Color.RED);
				TextView v = (TextView) t.getView().findViewById(android.R.id.message);
				v.setTextColor(Color.WHITE);
				t.show();
				return;
			}
			
			
		}else{
			return; // nothing to do
		}
		if (columns[2].equalsIgnoreCase(activity.getString(R.string.pill_name))) {
			// TODO: Import a pill file
		} else {
			for (int i = 1; i < lines.length; i++) {
				HeadacheAttack a = Utils.attackFromCSV(activity,
						lines[i].split(","), columns);

				// is there an entry for the given start and end date?
				List<Long> ids = Utils.findEvent(a.start, a.end,
						activity.getString(R.string.calendar_entry_title),
						activity.getContentResolver());
				if (ids.size() == 1) {
					// one event. delete it. replace it.
					Utils.deleteFromCalendar(ids.get(0),
							activity.getContentResolver());
					Utils.addToCalendar(a, activity);
				} else if (ids.size() > 1) {
					// TODO: more than one event...
				} else if (ids.size() == 0) {
					Utils.addToCalendar(a, activity);
				}
			}
		}
	}

	private static void addToCalendar(HeadacheAttack a, Activity activity) {
		ContentResolver cr = activity.getContentResolver();
		ContentValues values = new ContentValues();
		values.put("calendar_id", 1);
		values.put("dtstart", a.start.getTimeInMillis());
		values.put("dtend", a.end.getTimeInMillis());
		values.put("title", activity.getString(R.string.calendar_entry_title));
		values.put("description", a.toString());
		values.put("eventTimezone", TimeZone.getDefault().getID());
		values.put("eventLocation", ((MainActivity) activity).getLocation());
		values.put("hasAlarm", 0); // no alarm
		values.put("eventStatus", 1); // confirmed
		if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.FROYO) {
			// the tablet does not understand these columns
			values.put("availability", 1); // not blocking? TODO: might be a
											// preference
			values.put("accessLevel", 2); // private
		}
		cr.insert(Uri.parse(Utils.getCalendarUriBase(cr) + "events"), values);
	}

	private static void deleteFromCalendar(Long id,
			ContentResolver contentResolver) {
		String uriBase = Utils.getCalendarUriBase(contentResolver);
		Uri eventsUri = Uri.parse(uriBase + "events");
		Uri eventUri = ContentUris.withAppendedId(eventsUri, id);
		int rows = contentResolver.delete(eventUri, null, null);
		if (rows == 0) {
			Log.e("Utils", eventUri.toString());
		}
	}

	private static List<Long> findEvent(Calendar start, Calendar end,
			String title, ContentResolver contentResolver) {
		String uriBase = Utils.getCalendarUriBase(contentResolver);
		Uri.Builder builder = Uri.parse(uriBase + "instances/when/")
				.buildUpon();
		ContentUris.appendId(builder, start.getTimeInMillis() - 1000);
		ContentUris.appendId(builder, end.getTimeInMillis() + 1000);

		Cursor eventCursor = contentResolver.query(builder.build(),
				new String[] { "title", "begin", "event_id", "dtend" }, null,
				null, "startDay ASC, startMinute ASC");

		List<Long> ids = new ArrayList<Long>();
		if (eventCursor.getCount() > 0) {
			while (eventCursor.moveToNext()) {
				if (eventCursor.getString(0).equalsIgnoreCase(title)
				// && end.getTimeInMillis() == eventCursor.getLong(3)
						&& start.getTimeInMillis() == eventCursor.getLong(1)) {
					ids.add(eventCursor.getLong(2));
				}
			}
		}
		eventCursor.close();
		return ids;
	}

	private static HeadacheAttack attackFromCSV(Activity activity,
			String[] items, String[] columns) {
		HeadacheAttack a = new HeadacheAttack();
		for (int i = 0; i < items.length; i++) {
			items[i] = items[i].replaceAll("\"", "");
		}

		// loop over the items, because there are always less items than or as
		// much items as there are columns
		for (int i = 0; i < items.length; i++) {
			if (columns[i].equalsIgnoreCase(activity.getString(R.string.ernst))) {
				a.ernst = items[i];
			} else if (columns[i].equalsIgnoreCase(activity
					.getString(R.string.menstruatie))) {
				a.menstruatie = items[i].equalsIgnoreCase("true");
			} else if (columns[i].equalsIgnoreCase(activity
					.getString(R.string.misselijk))) {
				a.misselijk = items[i].equalsIgnoreCase("true");
			} else if (columns[i].equalsIgnoreCase(activity
					.getString(R.string.geur))) {
				a.geur = items[i].equalsIgnoreCase("true");
			} else if (columns[i].equalsIgnoreCase(activity
					.getString(R.string.licht))) {
				a.licht = items[i].equalsIgnoreCase("true");
			} else if (columns[i].equalsIgnoreCase(activity
					.getString(R.string.inslapen))) {
				a.inslapen = items[i].equalsIgnoreCase("true");
			} else if (columns[i].equalsIgnoreCase(activity
					.getString(R.string.doorslapen))) {
				a.doorslapen = items[i].equalsIgnoreCase("true");
			} else if (columns[i].equalsIgnoreCase(activity
					.getString(R.string.duizelig))) {
				a.duizelig = items[i].equalsIgnoreCase("true");
			} else if (columns[i].equalsIgnoreCase(activity
					.getString(R.string.stoelgang))) {
				a.stoelgang = items[i].equalsIgnoreCase("true");
			} else if (columns[i].equalsIgnoreCase(activity
					.getString(R.string.weer))) {
				a.weer = items[i];
			} else if (columns[i].equalsIgnoreCase(activity
					.getString(R.string.humeur))) {
				a.humeur = items[i];
			} else if (columns[i].equalsIgnoreCase(activity
					.getString(R.string.begin))) {
				a.start = Calendar.getInstance();
				// TODO: Spreadsheet apps change the format...
				try {
					a.start.setTimeInMillis(Utils.parse(items[i],
							activity.getString(R.string.very_long_date_time))
							.getTime());
				} catch (ParseException e) {
					// TODO: events end up on today... Is that better than
					// ignoring?
				}
			} else if (columns[i].equalsIgnoreCase(activity
					.getString(R.string.end))) {
				a.end = Calendar.getInstance();
				try {
					a.end.setTimeInMillis(Utils.parse(items[i],
							activity.getString(R.string.very_long_date_time))
							.getTime());
				} catch (ParseException e) {
					// TODO: events end up on today... Is that better than
					// ignoring?
				}
			} else if (columns[i].startsWith(activity.getString(R.string.left)
					+ ".x")) {
				// here we need a bit of a hack, because we have three cells
				// that make up the PainPoint
				if (a.leftPainPoints == null) {
					a.leftPainPoints = new ArrayList<PainPoint>();
				}
				PainPoint p = new PainPoint();
				p.x = Float.parseFloat(items[i]);
				p.y = Float.parseFloat(items[i + 1]);
				p.colorIndex = Integer.parseInt(items[i + 2]);
				a.leftPainPoints.add(p);
			} else if (columns[i].startsWith(activity.getString(R.string.right)
					+ ".x")) {
				// here we need a bit of a hack, because we have three cells
				// that make up the PainPoint
				if (a.rightPainPoints == null) {
					a.rightPainPoints = new ArrayList<PainPoint>();
				}
				PainPoint p = new PainPoint();
				p.x = Float.parseFloat(items[i]);
				p.y = Float.parseFloat(items[i + 1]);
				p.colorIndex = Integer.parseInt(items[i + 2]);
				a.rightPainPoints.add(p);
			}
		}
		return a;
	}

	private static StringBuilder readFile(Uri fileNameUri, Activity activity) {
		File file = null;
		file = new File(fileNameUri.getPath());
		// Read text from file
		StringBuilder text = new StringBuilder();

		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;

			while ((line = br.readLine()) != null) {
				text.append(line);
				text.append('\n');
			}

			br.close();
		} catch (IOException e) {
			Toast.makeText(activity, e.getLocalizedMessage(), Toast.LENGTH_LONG)
					.show();
			return null;
		}

		return text;
	}
}
