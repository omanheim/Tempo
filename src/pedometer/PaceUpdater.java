/*
 *  Pedometer - Android App
 *  Copyright (C) 2009 Levente Bagi
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pedometer;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.util.ArrayList;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import android.media.MediaPlayer;
import android.util.Log;

/**
 * Calculates and displays pace (steps / minute), handles input of desired pace,
 * notifies user if he/she has to go faster or slower.  
 * @author Levente Bagi
 */
public class PaceUpdater implements StepListener {

    private MediaPlayer mPlayer;
    int mSongBpm;
    
    void playSong(Song song) {
    	try {
    	    FileDescriptor fd = new FileInputStream(song.file).getFD();

    	    if (fd != null) {
    	        mPlayer.setDataSource(fd);
    	        mPlayer.prepare();
    	        mPlayer.start();
    	        mSongBpm = song.getBPM();
    	    }
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}
    }
    
    public interface Listener {
        public void paceChanged(int value);
    }
    private ArrayList<Listener> mListeners = new ArrayList<Listener>();
    
    int mCounter = 0;
    
    private long mLastStepTime = 0;
    private long[] mLastStepDeltas;
    private int mLastStepDeltasIndex = 0;
    private long mPace = 0;
    private Context mContext;
    
    PedometerSettings mSettings;
    Utils mUtils;

    /** Desired pace, adjusted by the user */
    int mDesiredPace;

    /** Should we speak? */
    boolean mShouldTellFasterslower;

    public PaceUpdater(Context aContext, PedometerSettings settings, Utils utils) {
        mUtils = utils;
        mContext = aContext;
        mSettings = settings;
        mDesiredPace = mSettings.getDesiredPace();
        mPlayer = new MediaPlayer();
        reloadSettings();
        mLastStepDeltas = new long[100];
        for (int i = 0; i < 30; i++) {
        	mLastStepDeltas[i] = -1;
        }
    }
    
    public void setPace(int pace) {
        mPace = pace;
        if (!mPlayer.isPlaying() || Math.abs(mSongBpm - pace) > 20) {
        	playSong(null);
        }
        
        int avg = (int)(60*1000.0 / mPace);
        for (int i = 0; i < mLastStepDeltas.length; i++) {
            mLastStepDeltas[i] = avg;
        }
        notifyListener();
    }
    
    public void reloadSettings() {
        mShouldTellFasterslower = 
            mSettings.shouldTellFasterslower();
        notifyListener();
    }
    
    public void addListener(Listener l) {
        mListeners.add(l);
    }

    public void setDesiredPace(int desiredPace) {
        mDesiredPace = desiredPace;
    }

    public void onStep() {
    	Log.i("PaceUpdater onStep called", "true");
        long thisStepTime = System.currentTimeMillis();
        mCounter ++;
        
        // Calculate pace based on last x steps
        if (mLastStepTime > 0) {
            long delta = thisStepTime - mLastStepTime;
            
            mLastStepDeltas[mLastStepDeltasIndex] = delta;
            mLastStepDeltasIndex = (mLastStepDeltasIndex + 1) % mLastStepDeltas.length;
            
            long sum = 0;
            boolean isMeaningful = true;
            for (int i = 0; i < mLastStepDeltas.length; i++) {
                if (mLastStepDeltas[i] < 0) {
                    isMeaningful = false;
                    break;
                }
                sum += mLastStepDeltas[i];
            }
            if (isMeaningful && sum > 0) {
                long avg = sum / mLastStepDeltas.length;
                mPace = 60*1000 / avg;
            }
            else {
                mPace = -1;
            }
        }
        mLastStepTime = thisStepTime;
        notifyListener();
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(mContext, notification);
            r.play();
        } catch (Exception e) {}
    }
    
    private void notifyListener() {
    	Log.e("new pace:", "" + mPace);
        for (Listener listener : mListeners) {
            listener.paceChanged((int)mPace);
        }
    }

	@Override
	public void passValue() {
		// TODO Auto-generated method stub
		
	}
    
}

