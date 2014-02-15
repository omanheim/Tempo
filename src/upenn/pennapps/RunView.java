package upenn.pennapps;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import beats.BPM2SampleProcessor;
import beats.EnergyOutputAudioDevice;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
	
	public class RunView extends View {
		
		private ConcurrentHashMap<Integer, ArrayList<File>> mSongs;
		
		// This thread sleeps and moves the unicorn
		private class BPMScannerThread extends AsyncTask<String, Void, Void> {

			protected Void doInBackground(String... params)
			{
				BPM2SampleProcessor processor = new BPM2SampleProcessor();
			    processor.setSampleSize(1024);
			    EnergyOutputAudioDevice output = new EnergyOutputAudioDevice(processor);
			    output.setAverageLength(1024);
	            Player player = null;
				try {
					player = new Player(new FileInputStream(params[0]), output);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        try {
					player.play();
				} catch (JavaLayerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			        
			    int bpm = (processor.getBPM() / 5) * 5;
		        mSongs.get(bpm).add(new File(params[0]));
		        return null;
			}
			
			protected void onPostExecute(Void result) {
				
			}
		}
		
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
			mSongs = new ConcurrentHashMap<Integer, ArrayList<File>>();
			for (int i = 0; i < 200; i += 5) {
				mSongs.put(i, new ArrayList<File>());
			}
			
			Cursor c =
		      getContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					                                  null, null, null, null);
			
			int index = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
			
			while (c.moveToNext()) {
				new BPMScannerThread().execute(c.getString(index));
			}
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