package upenn.pennapps;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.homepage);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**
	 * Starts a new run.
	 * @param v, the view
	 */
	public void startRun(View v) {
		Intent i = new Intent(this, RunActivity.class);
		startActivity(i);
	}
	
	/**
	 * Exits the app.
	 * @param v, the view
	 */
	public void quitGame(View v) {
		finish();
	}

}
