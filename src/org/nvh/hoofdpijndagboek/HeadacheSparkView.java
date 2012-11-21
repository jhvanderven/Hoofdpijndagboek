package org.nvh.hoofdpijndagboek;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class HeadacheSparkView extends View {
	int w, h;
	Paint p;
	private String calendarURI;

	public HeadacheSparkView(Context context) {
		super(context);
		init();
	}

	public HeadacheSparkView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		p = new Paint();
		calendarURI = Utils.getCalendarUriBase(getContext()
				.getContentResolver());
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int xoff = 1;
		int yoff =1;
		w = getWidth();
		h = getHeight();
		int h5 = h/5;
		p.setColor(Color.BLACK);
		p.setStyle(Style.FILL);
		canvas.drawPaint(p);
		int[] colors = new int[] { Color.WHITE, Color.MAGENTA, Color.YELLOW,
				Color.RED };
		// somehow I am for green axes
		p.setColor(Color.LTGRAY);
		canvas.drawLine(xoff, yoff, xoff, h - yoff, p);
		canvas.drawLine(xoff, h - yoff - h5, w - xoff, h - yoff - h5, p);
		if (h > 20) {
			p.setStyle(Style.STROKE);
			p.setStrokeWidth(1);
			p.setPathEffect(new DashPathEffect(new float[] { 5, 5 }, 1));
			for (int i = 2; i < 5; i++) {
				Path path = new Path();
				path.moveTo(xoff, h - yoff - i * (h - 2 * yoff) / 4);
				path.lineTo(w - 2 * xoff, h - yoff - i * (h - 2 * yoff) / 4);
				canvas.drawPath(path, p);
			}
		}
		p.setPathEffect(null);
		p.setStrokeWidth(1);
		p.setColor(Color.YELLOW);
		p.setStyle(Style.FILL);
		ContentResolver contentResolver = getContext().getContentResolver();
		Calendar cal = Calendar.getInstance();
		Uri.Builder builder = Uri.parse(calendarURI + "instances/when/")
				.buildUpon();
		cal.add(Calendar.YEAR, -1);
		ContentUris.appendId(builder, cal.getTimeInMillis());
		ContentUris.appendId(builder, Calendar.getInstance().getTimeInMillis());

		Cursor eventCursor = contentResolver
				.query(builder.build(), new String[] { "title", "begin", "end",
						"allDay", "description" }, null, null, "dtstart ASC");

		long xmin = cal.getTimeInMillis();
		long xmax = Calendar.getInstance().getTimeInMillis();
		if (eventCursor.getCount() > 0) {
			while (eventCursor.moveToNext()) {
				if (eventCursor.getString(0).equals(
						getContext().getString(R.string.calendar_entry_title))) {
					Calendar begin = Calendar.getInstance();
					begin.setTimeInMillis(eventCursor.getLong(1));
					Calendar end = Calendar.getInstance();
					end.setTimeInMillis(eventCursor.getLong(2));
					String ernst = Utils.parseDescription(
							eventCursor.getString(4),
							getContext().getString(R.string.ernst));
					int ernstIndex = Utils.getArrayIndex(getContext()
							.getResources().getStringArray(R.array.lgh_array),
							ernst) + 1;
					p.setColor(colors[ernstIndex]);
					long x1 = (begin.getTimeInMillis() - xmin) * (w - 2*xoff)
							/ (xmax - xmin);
					int y = h - yoff - h5- ernstIndex * (h - 2*yoff -h5) / 3;
					long x2 = (end.getTimeInMillis() - xmin) * (w - 2*xoff)
							/ (xmax - xmin);
					canvas.drawRect(x1, y, x2, h - yoff -h5, p);
				}else if(eventCursor.getString(0).equals(
						getContext().getString(R.string.calendar_pill_title))) {
					Calendar begin = Calendar.getInstance();
					begin.setTimeInMillis(eventCursor.getLong(1));
					p.setColor(Color.WHITE);
					long x1 = (begin.getTimeInMillis() - xmin) * (w - 2*xoff)
							/ (xmax - xmin);
					int y = h-h5-yoff;
					canvas.drawLine(x1, y, x1, h - yoff, p);
					
				}
			}
		}
		eventCursor.close();
	}
}
