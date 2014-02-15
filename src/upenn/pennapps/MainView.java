package upenn.pennapps;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class MainView extends View {
	
	public MainView(Context context) {
		super(context);
		init();
	}
	
	public MainView(Context context, AttributeSet as) {
		super(context, as);
		init();
	}

	private void init() {
		
		setBackgroundResource(R.drawable.watercolor);
	}
}
