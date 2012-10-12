package org.nvh.hoofdpijndagboek;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class HeadacheCalendarView extends View {

	int w, h;
	Paint p;
	SimpleDateFormat f = new SimpleDateFormat("MMM");
	SimpleDateFormat df = new SimpleDateFormat("d");
	Rect rect = new Rect(); // lint says to create up front and reuse
	Cell cell = new Cell(14,25);

	public HeadacheCalendarView(Context context) {
		super(context);
		p = new Paint();
	}

	public HeadacheCalendarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		p = new Paint();
	}

	private class Cell {
		public int i;
		public int j;
		public int N = 14;
		public int M = 25;

		public Cell(int N, int M){
			this.N=N;
			this.M=M;
		}
		
		public Cell get(Calendar c) {
			Calendar now = Calendar.getInstance();
			long days = (now.getTimeInMillis() - c.getTimeInMillis())
					/ (1000 * 60 * 60 * 24);
			int d = (int) days;
			cell.i = cell.N - ((d) / cell.M) - 1;
			cell.j = (cell.M * cell.N - d - 1) % cell.M;
			return cell;
		}

		public Calendar getDate(Cell c) {
			// untested!
			Calendar now = Calendar.getInstance();
			now.roll(Calendar.DAY_OF_YEAR, (c.i % c.N) * c.M + c.j);
			return now;
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
		int xstep = (int) (w / 14.33);
		int ystep = (int) (h / 25.47);
		int xend = (int) (14 * xstep);
		int yend = (int) (25 * ystep);
		for (double i = 0; i <= xend; i += xstep) {
			for (double j = 0; j <= yend; j += ystep) {
				canvas.drawLine((int) i, 0, (int) i, yend, p);
				canvas.drawLine(0, (int) j, xend, (int) j, p);
			}
		}

		// bottom-right is today
		// mark all Mondays
		p.setColor(Color.CYAN);
		Calendar cal = Calendar.getInstance();
//		Rect rect;
		int day = cal.get(Calendar.DAY_OF_WEEK);
		int yDayOffset = day - Calendar.MONDAY;
		Calendar monday = Calendar.getInstance();
		monday.add(Calendar.DAY_OF_YEAR, -1 * yDayOffset);
		for (int w = 50; w > 0; w--) {
			cell.get(monday);
			rect.left = cell.i * xstep;
			rect.top = cell.j * ystep;
			rect.right = rect.left + xstep/5;
			rect.bottom = rect.top + ystep;
//			rect = new Rect(c.i * xstep, c.j * ystep,
//					(c.i * xstep + xstep / 5), (c.j * ystep + ystep));
			canvas.drawRect(rect, p);
			monday.add(Calendar.DAY_OF_YEAR, -7);
		}

		// and beginnings of month
		p.setTextSize(15);
		cal = Calendar.getInstance();
		Calendar first = Calendar.getInstance();
		first.set(Calendar.DAY_OF_MONTH, 1);
		// mark first of the month
		for (int m = 12; m > 0; m--) {
			cell.get(first);
			p.setColor(Color.CYAN);
			canvas.drawText(f.format(first.getTime()), cell.i * xstep + 7, -3
					+ (cell.j+1) * ystep, p);
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
	}
}
