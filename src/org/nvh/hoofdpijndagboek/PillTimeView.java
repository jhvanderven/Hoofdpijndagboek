package org.nvh.hoofdpijndagboek;

import java.util.Calendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public class PillTimeView extends View {

	private Paint p;
	private Calendar start;
	private Calendar end;
	private long pill_time = -1;

	private final int xOff = 5;
	private final int hTick = 10;
	private final int hMinorTick = 5;
	private final int hoursExtra = 24;
	private Rect r;

	public PillTimeView(Context context) {
		super(context);
		init();
	}

	public PillTimeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public PillTimeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		p = new Paint();
		start = ((MainActivity) getContext()).getAttack().start;
		end = ((MainActivity) getContext()).getAttack().end;
		r = new Rect();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		float x;
		int w = getWidth();
		int h = getHeight();
		
		// determine how many minor ticks to show
		int hourStep = 0;
		float hourDiff;
		do {
			hourStep++;
			hourDiff = getXPosition(w - 2 * xOff, hourStep * 60 * 60 * 1000)
					- getXPosition(w - 2 * xOff, 0);
		} while (hourDiff < 5);
		// draw a green horizontal axis
		p.setColor(Color.GREEN);
		canvas.drawLine(xOff, h / 2, w - 2 * xOff, h / 2, p);
		// minor tick marks for every hourStep
		Calendar c = (Calendar) start.clone();
		c.add(Calendar.HOUR_OF_DAY, -1 * hoursExtra);
		c.set(Calendar.MINUTE, 0);
		Calendar c2 = (Calendar) end.clone();
		c2.add(Calendar.HOUR_OF_DAY, hoursExtra);
		while (c.getTimeInMillis() < c2.getTimeInMillis()) {
			x = getXPosition(w - 2 * xOff, c.getTimeInMillis()) + xOff;
			if (x > xOff && x < (w - 2 * xOff)) {
				canvas.drawLine(x, h / 2 + hMinorTick, x, h / 2 - hMinorTick, p);
			}
			c.add(Calendar.HOUR_OF_DAY, hourStep);
		}
		p.setColor(Color.RED);
		x = getXPosition(w - 2 * xOff, start.getTimeInMillis()) + xOff;
		float x2 = getXPosition(w - 2 * xOff, end.getTimeInMillis()) + xOff;
		p.setStyle(Style.FILL);
		p.setAlpha(128);
		r.top = h / 2 + hTick;
		r.left = (int) x;
		r.bottom = h / 2 - hTick;
		r.right = (int) x2;
		canvas.drawRect(r, p);
		if (pill_time != -1) {
			p.setColor(Color.WHITE);
			p.setAlpha(255);
			p.setStrokeWidth(3);
			x = getXPosition(w - 2 * xOff, pill_time) + xOff;
			canvas.drawLine(x, xOff, x, h - 2 * xOff, p);
		}
	}

	private float getXPosition(int widthInPixels, long timeInMillis) {
		// complete length of graph in milliseconds
		long length = end.getTimeInMillis() + 2 * hoursExtra * 60 * 60 * 1000
				- start.getTimeInMillis();
		long posInMillis = timeInMillis - start.getTimeInMillis() + hoursExtra
				* 60 * 60 * 1000;
		return posInMillis * (float) widthInPixels / length;
	}

	public void setTimeInMillis(long time) {
		pill_time = time;
	}

}
