package pedometer;

import upenn.pennapps.R;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;


public class Pedometer extends Activity {
    private int mPaceValue;
    private float mDistanceValue;
    private long appStartTime;
    private SharedPreferences mSettings;
    private PedometerSettings mPedometerSettings;
    private boolean mQuitting = false; // Set when user selected Quit from menu, can be used by onPause, onStop, onDestroy
    private StepService mService;
    private TextView mPaceValueView;
    private TextView mDistanceValueView;
    TextView mDesiredPaceView;

    /**
     * True, when service is running.
     */
    private boolean mIsRunning;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPaceValue = 0;
        setContentView(R.layout.pedometer);
        appStartTime = System.currentTimeMillis();
        new TimeTask().execute();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        mPedometerSettings = new PedometerSettings(mSettings);
        // Read from preferences if the service was running on the last onPause
        mIsRunning = mPedometerSettings.isServiceRunning();

        // Start the service if this is considered to be an application start (last onPause was long ago)
        if (!mIsRunning && mPedometerSettings.isNewStart()) {
            startStepService();
            bindStepService();
        }
        else if (mIsRunning) {
            bindStepService();
        }
        
        mPedometerSettings.clearServiceRunning();

        mPaceValueView     = (TextView) findViewById(R.id.pace_value);
        mDistanceValueView = (TextView) findViewById(R.id.distance_value);
//        mDesiredPaceView   = (TextView) findViewById(R.id.desired_pace_value);
    }
    
    @Override
    protected void onPause() {
        /*if (mIsRunning) {
            unbindStepService();
        }
        if (mQuitting) {
            mPedometerSettings.saveServiceRunningWithNullTimestamp(mIsRunning);
        }
        else {
            mPedometerSettings.saveServiceRunningWithTimestamp(mIsRunning);
        }
		*/
        super.onPause();
    }

    @Override
    protected void onStop() {
    	if (mIsRunning) {
    		stopStepService();
    		unbindStepService();
    		//mService.getPaceUpdater().stopSong();
    	}
    	if (mQuitting) {
            mPedometerSettings.saveServiceRunningWithNullTimestamp(mIsRunning);
        }
        else {
            mPedometerSettings.saveServiceRunningWithTimestamp(mIsRunning);
        }
        super.onStop();
    }

    protected void onDestroy() {
        super.onDestroy();
    }
    
    protected void onRestart() {
        super.onDestroy();
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {
    	@Override
        public void onServiceConnected(ComponentName className, IBinder service) {
    		Log.i("onServiceConnected called", "mConnection method");
            mService = ((StepService.StepBinder)service).getService();
            mService.reloadSettings();
            mService.registerCallback(mCallback);
            
        }

        public void onServiceDisconnected(ComponentName className) {
        	Log.i("mServivce disconnected", "aliza");
            mService = null;
        }
    };
    

    private void startStepService() {
    	Log.i("step service started", "in pedometer");
        if (!mIsRunning) {
            mIsRunning = true;
            startService(new Intent(Pedometer.this,
                    StepService.class));
        }
    }
    
    private void bindStepService() {
        bindService(new Intent(Pedometer.this, 
                StepService.class), mConnection, Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
    }

    private void unbindStepService() {
        unbindService(mConnection);
    }
    
    private void stopStepService() {
        if (mService != null) {
            stopService(new Intent(Pedometer.this,
                  StepService.class));
        }
        mIsRunning = false;
    }
    
    
    //FROM HERE ON: Handles getting relevant info from the StepService class 
    
    private static final int PACE_MSG = 1;
    private static final int DISTANCE_MSG = 2;
 
    // TODO: unite all into 1 type of message
    private StepService.ICallback mCallback = new StepService.ICallback() {
        public void paceChanged(int value) {
        	//Log.i("paceChanged", "intermediate step");
            mHandler.sendMessage(mHandler.obtainMessage(PACE_MSG, value, 0));
        }
        public void distanceChanged(float value) {
        	Log.i("distance", "intermediate step");
            mHandler.sendMessage(mHandler.obtainMessage(DISTANCE_MSG, (int)(value*1000), 0));
        }
    };
    
    private Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case PACE_MSG:
                	//Log.i("pace message received", "true");
                    mPaceValue = msg.arg1;
                    if (mPaceValue <= 0) { 
                        mPaceValueView.setText("0");
                    }
                    else {
                        mPaceValueView.setText("" + (int)mPaceValue);
                    }
                    break;
                case DISTANCE_MSG:
                	Log.i("distance message received", "true");
                    mDistanceValue = ((int)msg.arg1)/1000f;
                    if (mDistanceValue <= 0) { 
                        mDistanceValueView.setText("0");
                    }
                    else {
                        mDistanceValueView.setText(
                                ("" + (mDistanceValue + 0.000001f)).substring(0, 5)
                        );
                    }
                    break;
                default:
                	Log.i("default case", "oli");
                    super.handleMessage(msg);
            }
        }
        
	};

	class TimeTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
		}
		
		@Override 
		protected void onPostExecute(Void v) {
			long secondsElapsed = (System.currentTimeMillis() - appStartTime) / 1000;
			int hoursElapsed = (int) secondsElapsed / 3600;
			secondsElapsed -= (hoursElapsed*3600);
			int minutesElapsed = (int) secondsElapsed / 60;
			secondsElapsed -= (minutesElapsed*60);
			Log.i("Hours: ", "" + hoursElapsed);
			Log.i("Minutes: ", "" + minutesElapsed);
			Log.i("Seconds: ", "" + secondsElapsed);
			new TimeTask().execute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try { 
				Thread.sleep(20); 
			} catch (Exception e) { }			
			return null;
		}
	}
	

}
