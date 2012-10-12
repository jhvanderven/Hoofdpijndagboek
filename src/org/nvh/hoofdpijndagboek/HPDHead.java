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
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class HPDHead extends SherlockFragmentActivity{
	public static HurtingFragmentLeft left;
	public static HurtingFragmentRight right;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(MainActivity.THEME); // Used for theme switching in samples
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_stack);
	}

	public static class HurtingFragmentLeft extends HurtingFragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			HeadView v = new HeadView(getActivity(), R.drawable.waar, this);
			left = this;
			return v;
		}
		
		
	}

	public static class HurtingFragmentRight extends HurtingFragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			HeadView v = new HeadView(getActivity(),
					R.drawable.waar_mirrored, this);
			right = this;
			return v;
		}

	}

	public static class HurtingFragment extends SherlockFragment{
		protected List<Point> points = new ArrayList<Point>();
		
		// TODO: Remove colors/sizes unless we want to change the color/size based on the ernst
		protected List<Integer> colors = new ArrayList<Integer>();
		protected List<Integer> sizes = new ArrayList<Integer>();

		public String getData(){
			StringBuilder sb = new StringBuilder();
			for(Point p:points){
				sb.append("au").append(":").append(p.x).append(";").append(p.y).append("\n");
			}
			return sb.toString();
		}
	}

	public static class HeadView extends View implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{
		Bitmap b;
		Paint paint;
		Rect dest;
		private GestureDetector gesturedetector;
		private HurtingFragment sideOfHead;

		public HeadView(Context context, int pictureID, HurtingFragment x) {
			super(context);
			Bitmap bImm = BitmapFactory.decodeResource(getResources(),
					pictureID);
			b = bImm.copy(Bitmap.Config.ARGB_8888, true);
			paint = new Paint();
			dest = new Rect(0, 0, b.getWidth(), b.getHeight());
			sideOfHead=x;
	        gesturedetector = new GestureDetector(context, this);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			// the trick is to call the onTouchEvent of the detector, not of the super.
			return gesturedetector.onTouchEvent(event);
		}

		@Override
		protected void onDraw(Canvas canvas) {
//			super.onDraw(canvas);
			canvas.drawBitmap(b, null, dest, paint);
			for (int i = 0; i < sideOfHead.points.size(); i++) {
				paint.setColor(sideOfHead.colors.get(i));
				canvas.drawCircle(sideOfHead.points.get(i).x, sideOfHead.points.get(i).y,
						sideOfHead.sizes.get(i), paint);
			}
		}
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			return false;
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent e) {
			return false;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			Point p = new Point();
			p.x = (int) e.getX();
			p.y = (int) e.getY();
			List<Integer> toDelete = new ArrayList<Integer>();
			// we want to remove the circle that contains this point
			// if the user has already drawn something
			for (int i = 0; i < sideOfHead.points.size(); i++) {
				double square_dist = Math.pow((sideOfHead.points.get(i).x - p.x), 2)
						+ Math.pow((sideOfHead.points.get(i).y - p.y), 2);
				if (square_dist < Math.pow(sideOfHead.sizes.get(i), 2)) {
					// the point is in if its color is red
					if (sideOfHead.colors.get(i) == Color.RED) {
						toDelete.add(i);
					}
				}
			}
			for (Integer i : toDelete) {
				// objects, not integers... and ints are not Integers
				sideOfHead.colors.remove(sideOfHead.colors.get(i));
				sideOfHead.sizes.remove(sideOfHead.sizes.get(i));
				sideOfHead.points.remove(sideOfHead.points.get(i));
			}
			if (toDelete.size() == 0) {
				sideOfHead.points.add(p);
				sideOfHead.colors.add(Color.RED);
				sideOfHead.sizes.add(30);
			}
			this.invalidate();
			return true;
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
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
				float distanceY) {
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}
	}
}
