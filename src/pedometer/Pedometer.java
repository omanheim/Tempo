package pedometer;

import upenn.pennapps.R;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

public class Pedometer extends Activity {
	
	private int mStepValue;
    private int mPaceValue;
    private float mDistanceValue;
    private SharedPreferences mSettings;
    private PedometerSettings mPedometerSettings;
    private int mDesiredPace;
    private boolean mQuitting = false; // Set when user selected Quit from menu, can be used by onPause, onStop, onDestroy
    private StepService mService;
    private TextView mStepValueView;
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
        mStepValue = 0;
        mPaceValue = 0;
        setContentView(R.layout.pedometer);
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

        mStepValueView     = (TextView) findViewById(R.id.step_value);
        mPaceValueView     = (TextView) findViewById(R.id.pace_value);
        mDistanceValueView = (TextView) findViewById(R.id.distance_value);
        mDesiredPaceView   = (TextView) findViewById(R.id.desired_pace_value);
    }
    
    @Override
    protected void onPause() {
        if (mIsRunning) {
            unbindStepService();
        }
        if (mQuitting) {
            mPedometerSettings.saveServiceRunningWithNullTimestamp(mIsRunning);
        }
        else {
            mPedometerSettings.saveServiceRunningWithTimestamp(mIsRunning);
        }

        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    protected void onDestroy() {
        super.onDestroy();
    }
    
    protected void onRestart() {
        super.onDestroy();
    }
    
    private void setDesiredPace(int desiredPace) {
        if (mService != null) {
                mService.setDesiredPace(desiredPace);
        }
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
        boolean b = bindService(new Intent(Pedometer.this, 
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
    
    private static final int STEPS_MSG = 1;
    private static final int PACE_MSG = 2;
    private static final int DISTANCE_MSG = 3;
    private static final int SPEED_MSG = 4;
 
    // TODO: unite all into 1 type of message
    private StepService.ICallback mCallback = new StepService.ICallback() {
        public void stepsChanged(int value) {
            mHandler.sendMessage(mHandler.obtainMessage(STEPS_MSG, value, 0));
        }
        public void paceChanged(int value) {
        	//Log.i("paceChanged", "intermediate step");
            mHandler.sendMessage(mHandler.obtainMessage(PACE_MSG, value, 0));
        }
        public void distanceChanged(float value) {
            mHandler.sendMessage(mHandler.obtainMessage(DISTANCE_MSG, (int)(value*1000), 0));
        }
        public void speedChanged(float value) {
            mHandler.sendMessage(mHandler.obtainMessage(SPEED_MSG, (int)(value*1000), 0));
        }
    };
    
    private Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case STEPS_MSG:
                    mStepValue = (int)msg.arg1;
                    mStepValueView.setText("" + mStepValue);
                    break;
                case PACE_MSG:
                	Log.i("pace message received", "true");
                    mPaceValue = msg.arg1;
                    if (mPaceValue <= 0) { 
                        mPaceValueView.setText("0");
                    }
                    else {
                        mPaceValueView.setText("" + (int)mPaceValue);
                    }
                    break;
                case DISTANCE_MSG:
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

  
}
