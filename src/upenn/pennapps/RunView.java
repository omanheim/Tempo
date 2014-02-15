package upenn.pennapps;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
	
	public class RunView extends View {
		
		public RunView(Context context) {
			super(context);
			init();
		}
	
		public RunView(Context context, AttributeSet as) {
			super(context, as);
			init();
		}
	
		/**
		 */
		private void init() {
		}
	
		/**
		 */
		public void onDraw(Canvas c) {
		
		}
		
		/**
		 */
		public boolean onTouchEvent(MotionEvent e) {
			return false;
		}
		
	}