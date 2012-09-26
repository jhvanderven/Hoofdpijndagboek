package org.nvh.hoofdpijndagboek;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public class HeadacheCalendarView extends View {

	int w, h;
	Paint p;
	
	public HeadacheCalendarView(Context context) {
		super(context);
		p= new Paint();
	}

	public HeadacheCalendarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		p=new Paint();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		w=getWidth();
		h=getHeight();
		p.setColor(Color.BLACK);
		p.setStyle(Style.FILL);
		canvas.drawPaint(p);

		p.setColor(Color.WHITE);
		for (int i = 0; i < w; i += 100) {
			for (int j = 0; j < h; j += 50) {
				canvas.drawLine(i, 0, i, getHeight() - 10, p);
				canvas.drawLine(0, j, getWidth() - 10, j, p);
			}
		}
		p.setTextSize(20);
		canvas.drawText("Hatseflats", 10, 25, p);
	}
}
