package org.nvh.hoofdpijndagboek;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class HPDHeadRight extends SherlockFragmentActivity {
	public static List<PainPoint> points = new ArrayList<PainPoint>();
	protected static int[] ernst = new int[] { Color.YELLOW, Color.MAGENTA,
			Color.RED };
	protected static PainPoint lastPoint;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(MainActivity.THEME); // Used for theme switching in samples
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_stack);
	}

	public static class HurtingFragmentRight extends SherlockFragment {
		static View view = null;

		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			view = new HeadViewRight(getActivity(), R.drawable.waar_mirrored,
					this);
			view.setTag(R.drawable.waar_mirrored);
			return view;
		}

		public static void pleaseUpdate(HeadacheAttack a) {
			points = a.rightPainPoints;
			if (view != null)
				view.invalidate();
		}

		@Override
		public void onResume() {
			points = ((MainActivity) getActivity()).getAttack().rightPainPoints;
			super.onResume();
		}

		// storedPoints are in fractions of the width and height and have no
		// size
		public static void setPainPoints(List<PainPoint> storedPoints) {
			points = storedPoints;
			if (view == null) {
				// the thing has not been created yet.'
				return;
			}
			view.postInvalidate();
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
			gesturedetector = new GestureDetector(activity, this);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			// the trick is to call the onTouchEvent of the detector
			return gesturedetector.onTouchEvent(event);
		}

		@SuppressLint("DrawAllocation")
		@Override
		protected void onDraw(Canvas canvas) {
			Bitmap.createScaledBitmap(b, getWidth(), getHeight(), false);
			dest = new Rect(0, 0, getWidth(), getHeight());
			MainActivity m = (MainActivity) getContext();
			points = m.getPainPoints(1);
			canvas.drawBitmap(b, null, dest, paint);
			if (points == null)
				return;
			int w = getWidth();
			int h = getHeight();
			SharedPreferences sp = HeadacheDiaryApp.getApp()
					.getSharedPreferences(Utils.GENERAL_PREFS_NAME, 0);
			ernst[0] = sp.getInt("pref_low", 0xffffff00);
			ernst[1] = sp.getInt("pref_average", 0xffff00ff);
			ernst[2] = sp.getInt("pref_high", 0xffff0000);
			for (PainPoint p : points) {
				paint.setColor(ernst[p.colorIndex]);
				canvas.drawCircle(p.x * w, p.y * h, w / 10, paint);
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
				lastPoint.x = e2.getX()/getWidth();
				lastPoint.y = e2.getY()/getHeight();
				this.invalidate();
				Log.i("HPD", "Right-Scrolling to " + lastPoint.x + ", " + lastPoint.y);
}
			return true;
		}

		@Override
		public void onShowPress(MotionEvent e) {
			Log.i("HPD", "ShowPress-right");
PainPoint p = new PainPoint();
			p.x = e.getX() / getWidth();
			p.y = e.getY() / getHeight();
			p.colorIndex = 0;
			p.size = getWidth() / 10;
			points.add(p);
			lastPoint = p;
			this.invalidate();
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			Log.i("HPD", "onSingleTapUp-right");
			PainPoint p = new PainPoint();
			p.x = e.getX();
			p.y = e.getY();
			// we want to remove the circle that contains this point
			// if the user has already drawn something
			// light->medium->heavy->gone
			int w = getWidth();
			int h = getHeight();
			boolean newPain = true;
			int i = 0;
			while (i < points.size()) {
				double square_dist = Math.pow((points.get(i).x * w - p.x), 2)
						+ Math.pow((points.get(i).y * h - p.y), 2);
				if (square_dist < Math.pow(points.get(i).size, 2)) {
					newPain = false; // an old point under the finger
					for (int c = 0; c < 3; c++) {
						if (points.get(i).colorIndex == c) {
							if (c == ernst.length - 1) {
								points.remove(points.get(i));
								i--; // fool the loop
							} else {
								points.get(i).colorIndex = c + 1;
								break;
							}
						}
					}
				}
				i++;
			}
			if (newPain) {
				p.colorIndex = 0;
				p.size = w / 10;
				p.x /= w;
				p.y /= h;
				points.add(p);
			}
			lastPoint = null; // no more dragging around after the Up event
			this.postInvalidate();
			return true;
		}
	}
}
