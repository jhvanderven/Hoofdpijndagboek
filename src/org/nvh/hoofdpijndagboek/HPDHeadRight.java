package org.nvh.hoofdpijndagboek;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class HPDHeadRight extends SherlockFragmentActivity {
	protected static List<Point> points = new ArrayList<Point>();
	protected static List<Integer> colors = new ArrayList<Integer>();
	protected static List<Integer> sizes = new ArrayList<Integer>();
	protected static int[] ernst = new int[] { Color.YELLOW, Color.MAGENTA,
			Color.RED };
	protected static Point lastPoint;

	public static class HurtingFragmentRight extends SherlockFragment {
		static View view = null;

		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return view = new HeadViewRight(getActivity(),
					R.drawable.waar_mirrored, this);
		}

		public static String getData() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i< points.size(); i++) {
				Point p = points.get(i);
				sb.append("au")
						.append(":")
						.append(String.format("%1.3f",
								(double) p.x / view.getWidth()))
						.append(";")
						.append(String.format("%1.3f",
								(double) p.y / view.getHeight()))
						.append(";")
						.append(colors.get(i))
						.append("\n");
				
			}
			return sb.toString();
		}

		public static int getErnst() {
			int hoog = 0;
			int gemiddeld = 0;
			int laag = 0;
			for (Integer i : colors) {
				if (i.equals(Color.RED)) {
					hoog++;
				} else if (i.equals(Color.MAGENTA)) {
					gemiddeld++;
				} else {
					laag++;
				}
			}

			return Math.round((hoog * 3 + gemiddeld * 2 + laag)
					/ (float) colors.size());
		}
	}

	public static class HeadViewRight extends View implements
			GestureDetector.OnGestureListener {
		Bitmap b;
		Paint paint;
		Rect dest;
		private GestureDetector gesturedetector;

		public HeadViewRight(FragmentActivity activity, int pictureID,
				SherlockFragment hurtingFragment) {
			super(activity);
			Bitmap bImm = BitmapFactory.decodeResource(getResources(),
					pictureID);
			b = bImm.copy(Bitmap.Config.ARGB_8888, true);
			paint = new Paint();
			dest = new Rect(0, 0, b.getWidth(), b.getHeight());
			gesturedetector = new GestureDetector(activity, this);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			// the trick is to call the onTouchEvent of the detector
			return gesturedetector.onTouchEvent(event);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			canvas.drawBitmap(b, null, dest, paint);
			for (int i = 0; i < points.size(); i++) {
				paint.setColor(colors.get(i));
				canvas.drawCircle(points.get(i).x, points.get(i).y,
						sizes.get(i), paint);
			}
		}

		@Override
		public boolean onDown(MotionEvent e) {
			// handle this one, otherwise we can never draw
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			if (lastPoint != null) {
				lastPoint.x = (int) e2.getX();
				lastPoint.y = (int) e2.getY();
				this.postInvalidate();
			}
			return true;
		}

		@Override
		public void onShowPress(MotionEvent e) {
			Point p = new Point();
			p.x = (int) e.getX();
			p.y = (int) e.getY();
			points.add(p);
			colors.add(ernst[0]);
			sizes.add(getWidth() / 10);
			lastPoint = p;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			Point p = new Point();
			p.x = (int) e.getX();
			p.y = (int) e.getY();
			// we want to remove the circle that contains this point
			// if the user has already drawn something
			// light->medium->heavy->gone
			boolean newPain = true;
			int i = 0;
			while (i < points.size()) {
				double square_dist = Math.pow((points.get(i).x - p.x), 2)
						+ Math.pow((points.get(i).y - p.y), 2);
				if (square_dist < Math.pow(sizes.get(i), 2)) {
					newPain = false; // an old point under the finger
					for (int c = 0; c < 3; c++) {
						if (colors.get(i) == ernst[c]) {
							if (c == ernst.length - 1) {
								colors.remove(colors.get(i));
								sizes.remove(sizes.get(i));
								points.remove(points.get(i));
								i--; // fool the loop
							} else {
								colors.set(i, ernst[c + 1]);
								break;
							}
						}
					}
				}
				i++;
			}
			if (newPain) {
				points.add(p);
				colors.add(ernst[0]);
				sizes.add(getWidth() / 10);
			}
			lastPoint = null; // no more dragging around after the Up event
			this.postInvalidate();
			return true;
		}
	}
}
