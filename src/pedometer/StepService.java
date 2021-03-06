package pedometer;

import upenn.pennapps.R;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class StepService extends Service {
	
	private SharedPreferences mSettings;
    private PedometerSettings mPedometerSettings;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private StepDetector mStepDetector;
    private PaceUpdater mPaceUpdater;
    private DistanceUpdater mDistanceUpdater;
    private Utils mUtils;
    private int mDesiredPace;
    private SharedPreferences mState;
    private SharedPreferences.Editor mStateEditor;


    private int mSteps;
    private int mPace;
    private float mDistance;
    
    private final IBinder mBinder = new StepBinder();
    private ICallback mCallback;
    
    /**
     * Forwards pace values from PaceUpdater to the Pedometer activity. 
     */
    PaceUpdater.Listener mPaceListener = new PaceUpdater.Listener() {
        public void paceChanged(int value) {
        	//Log.i("paceChanged in pace listener", "true");
            mPace = value;
            passValue();
        }
        public void passValue() {
            if (mCallback != null) {
                mCallback.paceChanged(mPace);
            }
        }
    };
    
    public PaceUpdater getPaceUpdater() {
    	return mPaceUpdater;
    }
    
    /**
     * Forwards distance values from DistanceNotifier to the activity. 
     */
    private DistanceUpdater.Listener mDistanceListener = new DistanceUpdater.Listener() {
        public void distanceChanged(float value) {
            mDistance = value;
            passValue();
        }
        public void passValue() {
            if (mCallback != null) {
                mCallback.distanceChanged(mDistance);
            }
        }
    };
    
    @Override
    public void onCreate() {
        //Log.i("creating step service", "in step");
        super.onCreate();
       
        // Load settings
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        mPedometerSettings = new PedometerSettings(mSettings);
        mState = getSharedPreferences("state", 0);
        mUtils = Utils.getInstance();
        mUtils.setService(this);
        mUtils.initTTS();

        /* START STEP DETECTOR, ADD PaceUpdater and DistanceUpdater
        PaceUpdater's onStep() method is called by the StepDetector*/

        mStepDetector = new StepDetector();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        registerDetector();

        mPaceUpdater = new PaceUpdater(getApplicationContext(), mPedometerSettings, mUtils);
        mPaceUpdater.setPace(mPace = mState.getInt("pace", 0));
        mPaceUpdater.addListener(mPaceListener);
        mStepDetector.addStepListener(mPaceUpdater);
        
        mDistanceUpdater = new DistanceUpdater(mDistanceListener, mPedometerSettings, mUtils);
        mDistanceUpdater.setDistance(mDistance = mState.getFloat("distance", 0));
        mStepDetector.addStepListener(mDistanceUpdater);
        
        /*
        mStepDisplayer = new StepDisplayer(mPedometerSettings, mUtils);
        mStepDisplayer.setSteps(mSteps = mState.getInt("steps", 0));
        mStepDisplayer.addListener(mStepListener);
        mStepDetector.addStepListener(mStepDisplayer);
*/

        // Used when debugging:
        // mStepBuzzer = new StepBuzzer(this);
        // mStepDetector.addStepListener(mStepBuzzer);

        // Tell the user we started.
        Toast.makeText(this, getText(R.string.started), Toast.LENGTH_SHORT).show();
    }
    
    public int onStartCommand(Intent intent, int flags, int startId) {
    	//Log.i("starting step s", "onStart");
        return super.onStartCommand(intent, flags, startId);
    }
    
    @Override
    public void onDestroy() {
        mUtils.shutdownTTS();
        unregisterDetector();
        
        mStateEditor = mState.edit();
        mStateEditor.putInt("steps", mSteps);
        mStateEditor.putInt("pace", mPace);
        mStateEditor.putFloat("distance", mDistance);
        mStateEditor.commit();
        
        super.onDestroy();
        
        // Stop detecting
        mSensorManager.unregisterListener(mStepDetector);

        // Tell the user we stopped.
        Toast.makeText(this, getText(R.string.stopped), Toast.LENGTH_SHORT).show();
    }
    
    private void registerDetector() {
        mSensor = mSensorManager.getDefaultSensor(
            Sensor.TYPE_ACCELEROMETER /*| 
            Sensor.TYPE_MAGNETIC_FIELD | 
            Sensor.TYPE_ORIENTATION*/);
        mSensorManager.registerListener(mStepDetector,
            mSensor,
            SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void unregisterDetector() {
        mSensorManager.unregisterListener(mStepDetector);
    }
     
    public void reloadSettings() {
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        if (mStepDetector != null) { 
            mStepDetector.setSensitivity(
                    Float.valueOf(mSettings.getString("sensitivity", "10"))
            );
        }
        if (mPaceUpdater!= null) {
        	mPaceUpdater.reloadSettings();
        }
    }
	
	 /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class StepBinder extends Binder {
        StepService getService() {
            return StepService.this;
        }
    }
    
	public IBinder onBind(Intent intent) {
	    return mBinder;
	}

    public interface ICallback {
        public void paceChanged(int value);
        public void distanceChanged(float value);
    }

    public void registerCallback(ICallback cb) {
        mCallback = cb;
        //mStepDisplayer.passValue();
        //mPaceListener.passValue();
    }
    

}
