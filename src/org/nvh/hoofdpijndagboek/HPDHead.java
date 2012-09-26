package org.nvh.hoofdpijndagboek;

import java.util.ArrayList;
import java.util.List;

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
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			HeadView v = new HeadView(getActivity(),
					R.drawable.waar_mirrored);
			return v;
		}
	}

	public static class HurtingFragment extends SherlockFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}
	}

	public static class HeadView extends View {
		Bitmap b;
		Paint paint;
		Rect dest;
		List<Point> points = new ArrayList<Point>();
		List<Integer> colors = new ArrayList<Integer>();
		List<Integer> sizes = new ArrayList<Integer>();

		public HeadView(Context context, int pictureID) {
			super(context);
			Bitmap bImm = BitmapFactory.decodeResource(getResources(),
					pictureID);
			b = bImm.copy(Bitmap.Config.ARGB_8888, true);
			paint = new Paint();
			dest = new Rect(0, 0, b.getWidth(), b.getHeight());
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			canvas.drawBitmap(b, null, dest, paint);
			for (int i = 0; i < points.size(); i++) {
				paint.setColor(colors.get(i));
				canvas.drawCircle(points.get(i).x, points.get(i).y,
						sizes.get(i), paint);
			}
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
				Point p = new Point();
				p.x = (int) event.getX();
				p.y = (int) event.getY();
				List<Integer> toDelete = new ArrayList<Integer>();
				// we want to remove the circle that contains this point
				// if the user has already drawn something
				for (int i = 0; i < points.size(); i++) {
					double square_dist = Math.pow((points.get(i).x - p.x), 2)
							+ Math.pow((points.get(i).y - p.y), 2);
					if (square_dist < Math.pow(sizes.get(i), 2)) {
						// the point is in if its color is red
						if (colors.get(i) == Color.RED) {
							toDelete.add(i);
						}
					}
				}
				for (Integer i : toDelete) {
					// TODO: I do not understand why a remove
					// does not work. The onDraw does not redraw the
					// complete screen. Now the user can erase the bitmap.
					colors.set(i, Color.WHITE);
					// colors.remove(i);
					// sizes.remove(i);
					// points.remove(i);
				}
				if (toDelete.size() == 0) {
					points.add(p);
					colors.add(Color.RED);
					sizes.add(getWidth() / 20);
				}
				this.invalidate();
				return true;
			}

			return super.onTouchEvent(event);
		}
	}
}
