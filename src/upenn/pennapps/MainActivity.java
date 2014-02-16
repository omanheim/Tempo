package upenn.pennapps;

import java.io.File;
import java.io.IOException;

import android.os.Bundle;
import android.os.Environment;
import pedometer.*;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	public static final int RunActivity_ID = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		try {
		    File filename = new File(Environment.getExternalStorageDirectory()+"/logfile.log"); 
		    filename.createNewFile(); 
		    String cmd = "logcat -d -f "+filename.getAbsolutePath();
		    Runtime.getRuntime().exec(cmd);
		} catch (Exception e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	}
	
	@Override
	public void onPause() {
		try {
			Log.e("shutting down", "song library");
			if (MainView.mSongs != null) MainView.mSongs.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void onRunButtonClick(View v){
		startActivity(new Intent(this, Pedometer.class));
	}

}
