package org.nvh.hoofdpijndagboek;

import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class HPDHead extends SherlockFragmentActivity {
	int mStackLevel = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(MainActivity.THEME); // Used for theme switching in samples
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_stack);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("level", mStackLevel);
	}

	public static class HurtingFragmentLeft extends HurtingFragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			HeadView v = new HeadView(getActivity(), R.drawable.waar);
			return v;
		}
	}

	public static class HurtingFragmentRight extends HurtingFragment {
		// TODO: Improve bitmap quality
		// TODO: Invert the waar bitmap, make it point the other way
		// TODO: Left has a red background, Right blue
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			HeadView v = new HeadView(getActivity(),
					R.drawable.ic_action_search);
			return v;
		}
	}

	public static class HurtingFragment extends SherlockFragment {
		int pictureID;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}
	}

	public static class HeadView extends View {
		Bitmap b;
		Bitmap m;
		Paint paint;
		Rect dest;
		long start;
		int oldms;

		public HeadView(Context context, int pictureID) {
			super(context);
			Bitmap bImm = BitmapFactory.decodeResource(getResources(),
					pictureID);
			b = bImm.copy(Bitmap.Config.ARGB_8888, true);
			paint = new Paint();
			dest = new Rect(0, 0, b.getWidth(), b.getHeight());
			oldms = 0;
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			canvas.drawBitmap(b, null, dest, paint);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
				// start of the coloring
				System.out.println("Starting");
				start = event.getEventTime();
				System.out.println(start);
				return true;
			}

			if (event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
				start = -1;
			}

			if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
				System.out.println("Moving");
				if (start == -1) {
					System.out.println("Not started");
					return false;
				}
				// the color gets more mean as the user presses longer
				int ms = (int) (event.getEventTime() - start)/10;
				if (ms > 255)
					ms = 255;
				System.out.println(ms);
				int newColor = Color.argb(255, ms, 0, 0);
				int oldColor = b.getPixel((int) event.getX(),
						(int) event.getY());
				Point p = new Point();
				p.x = (int) event.getX();
				p.y = (int) event.getY();

				FloodFill(b, p, oldColor, newColor);
				this.invalidate();
				return true;
			}

			if (event.getActionMasked() == MotionEvent.ACTION_UP) {
				System.out.println("Stopping");
				if (start != -1) {
					return true;
				}
			}

			return super.onTouchEvent(event);
		}

		// TODO: Think of a way to store this, perhaps coordinates?
		private static void FloodFill(Bitmap bmp, Point pt, int targetColor,
				int replacementColor) {
			Queue<Point> q = new LinkedList<Point>();
			q.add(pt);
			while (q.size() > 0) {
				Point n = q.poll();
				if (bmp.getPixel(n.x, n.y) != targetColor)
					continue;

				Point w = n, e = new Point(n.x + 1, n.y);
				while ((w.x > 0) && (bmp.getPixel(w.x, w.y) == targetColor)) {
					bmp.setPixel(w.x, w.y, replacementColor);
					if ((w.y > 0)
							&& (bmp.getPixel(w.x, w.y - 1) == targetColor))
						q.add(new Point(w.x, w.y - 1));
					if ((w.y < bmp.getHeight() - 1)
							&& (bmp.getPixel(w.x, w.y + 1) == targetColor))
						q.add(new Point(w.x, w.y + 1));
					w.x--;
				}
				while ((e.x < bmp.getWidth() - 1)
						&& (bmp.getPixel(e.x, e.y) == targetColor)) {
					bmp.setPixel(e.x, e.y, replacementColor);

					if ((e.y > 0)
							&& (bmp.getPixel(e.x, e.y - 1) == targetColor))
						q.add(new Point(e.x, e.y - 1));
					if ((e.y < bmp.getHeight() - 1)
							&& (bmp.getPixel(e.x, e.y + 1) == targetColor))
						q.add(new Point(e.x, e.y + 1));
					e.x++;
				}
			}
		}
	}
}
