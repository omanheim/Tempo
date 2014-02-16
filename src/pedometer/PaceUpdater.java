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

import upenn.pennapps.MainView;
import upenn.pennapps.Song;



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
       
    public interface Listener {
        public void paceChanged(int value);
    }
    
    private int NUM_STEPS;
    private ArrayList<Listener> mListeners = new ArrayList<Listener>();
    private long mLastStepTime;
    private long lastStartSongTime;
    private long[] mLastStepDeltas;
    private int mLastStepDeltasIndex;
    private long mPace;
    int stepCounter; 
    private int stepsSinceLastChange;
    private Context mContext;
    PedometerSettings mSettings;
    Utils mUtils;
    
    private MediaPlayer mPlayer;
    int mSongBpm;
    
    void playSong(Song song) {
    	try {
    		if (mPlayer.isPlaying()) {
    			mPlayer.stop();
    		}
	    	mPlayer.reset();
	        mPlayer.setDataSource(song.getFile());
	        mPlayer.prepare();
	        mPlayer.start();
	        mSongBpm = song.getBPM();
    	}
    	catch (Exception e) {
    	    e.printStackTrace();
    	}
    	lastStartSongTime = System.currentTimeMillis();
    	stepsSinceLastChange = 0;
    	song.setLastPlay();
    }
    
    void stopSong() {
    	if (mPlayer.isPlaying()) {
			mPlayer.stop();
		}
    }


    public PaceUpdater(Context aContext, PedometerSettings settings, Utils utils) {
    	stepCounter = 0;
    	mPace = 0;
    	stepsSinceLastChange = 0;
        mLastStepTime = 0;
        lastStartSongTime = 0;
        mLastStepDeltasIndex = 0;
        mUtils = utils;
        mContext = aContext;
        mSettings = settings;
        mPlayer = new MediaPlayer();
        reloadSettings();
        NUM_STEPS = 30;
        mLastStepDeltas = new long[NUM_STEPS];
        for (int i = 0; i < NUM_STEPS; i++) {
        	mLastStepDeltas[i] = -1;
        }
    }
    
    public void setPace(int pace) {
        mPace = pace;
        int avg = (int)(60*1000.0 / mPace);
        for (int i = 0; i < mLastStepDeltas.length; i++) {
            mLastStepDeltas[i] = avg;
        }
        notifyListener();
    }
    
    public void reloadSettings() {
        notifyListener();
    }
    
    public void addListener(Listener l) {
        mListeners.add(l);
    }


    public void onStep() {
    	Log.i("PaceUpdater onStep called", "true");
        long thisStepTime = System.currentTimeMillis();
        stepCounter++;
        stepsSinceLastChange++;
        
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
                if (avg == 0) mPace = 0;
                else mPace = 60*1000 / avg;
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
        if ( mPace > 20 
        		&& (!mPlayer.isPlaying() || (Math.abs(mSongBpm - mPace) > 20
        		&& System.currentTimeMillis() - lastStartSongTime > 3000
       		 && stepsSinceLastChange > NUM_STEPS))) {
        	Log.i("[notifyListener] pace is...", "" + (int)mPace);
        	Song s = MainView.mSongs.getForPace((int)mPace);
        	System.out.println("Found song: " + s.getTitle());
        	System.out.println("Filename: " + s.getFile());
        	playSong(s);
        }
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

