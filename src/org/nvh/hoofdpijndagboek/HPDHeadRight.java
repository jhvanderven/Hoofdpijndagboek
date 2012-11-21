package org.nvh.hoofdpijndagboek;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
			view = new HeadViewRight(getActivity(),
					R.drawable.waar_mirrored, this);
			view.setTag(R.drawable.waar_mirrored);
			return view;
		}
		public static void pleaseUpdate(HeadacheAttack a){
			points = a.rightPainPoints;
			if(view!=null)view.invalidate();
		}

		@Override
		public void onResume() {
			points = ((MainActivity)getActivity()).getAttack().rightPainPoints;
			super.onResume();
		}

		public static String getData() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < points.size(); i++) {
				PainPoint p = points.get(i);
				sb.append("au")
						.append(":")
						.append(String.format(Locale.US, "%1.3f",
								p.x / view.getWidth()))
						.append(";")
						.append(String.format(Locale.US, "%1.3f",
								p.y / view.getHeight())).append(";")
						.append(p.color).append("\n");

			}
			return sb.toString();
		}

		public static int getErnst() {
			int hoog = 0;
			int gemiddeld = 0;
			int laag = 0;
			for (PainPoint p : points) {
				if (p.color == Color.RED) {
					hoog++;
				} else if (p.color == Color.MAGENTA) {
					gemiddeld++;
				} else {
					laag++;
				}
			}

			return Math.round((hoog * 3 + gemiddeld * 2 + laag)
					/ (float) points.size());
		}

		// storedPoints are in fractions of the width and height and have no
		// size
		public static void setPainPoints(List<PainPoint> storedPoints) {
			points = storedPoints;
			if (view == null) {
				// the thing has not been created yet.'
				return;
			}
			int w = view.getWidth();
			int h = view.getHeight();
			for (PainPoint p : points) {
				p.x = p.x * w;
				p.y = p.y * h;
				p.size = w / 10;
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
			MainActivity m = (MainActivity) getContext();
			points = m.getPainPoints(1);
			canvas.drawBitmap(b, null, dest, paint);
			if(points==null)return;
			int w = getWidth();
			int h = getHeight();
			for (PainPoint p : points) {
				paint.setColor(p.color);
				if (p.x < 1 && p.y < 1) {
					// not multiplied by width and height
					p.x *= w;
					p.y *= h;
					p.size = w/10;
				}
				canvas.drawCircle(p.x, p.y, p.size, paint);
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
				lastPoint.x = e2.getX();
				lastPoint.y = e2.getY();
				this.postInvalidate();
			}
			return true;
		}

		@Override
		public void onShowPress(MotionEvent e) {
			PainPoint p = new PainPoint();
			p.x = e.getX();
			p.y = e.getY();
			p.color = ernst[0];
			p.size = getWidth() / 10;
			points.add(p);
			lastPoint = p;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			PainPoint p = new PainPoint();
			p.x = e.getX();
			p.y = e.getY();
			// we want to remove the circle that contains this point
			// if the user has already drawn something
			// light->medium->heavy->gone
			boolean newPain = true;
			int i = 0;
			while (i < points.size()) {
				double square_dist = Math.pow((points.get(i).x - p.x), 2)
						+ Math.pow((points.get(i).y - p.y), 2);
				if (square_dist < Math.pow(points.get(i).size, 2)) {
					newPain = false; // an old point under the finger
					for (int c = 0; c < 3; c++) {
						if (points.get(i).color == ernst[c]) {
							if (c == ernst.length - 1) {
								points.remove(points.get(i));
								i--; // fool the loop
							} else {
								points.get(i).color = ernst[c + 1];
								break;
							}
						}
					}
				}
				i++;
			}
			if (newPain) {
				p.color = ernst[0];
				p.size = getWidth() / 10;
				points.add(p);
			}
			lastPoint = null; // no more dragging around after the Up event
			this.postInvalidate();
			return true;
		}
	}
}
