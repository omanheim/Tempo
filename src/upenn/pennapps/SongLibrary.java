package upenn.pennapps;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import android.util.Log;

public class SongLibrary {
	
	private ConcurrentHashMap<Integer, ArrayList<Song>> mSongs;
	
	/**
	 * Constructs a SongLibrary with an empty collection of songs
	 * for bpm's of 0 to 200.
	 */
	public SongLibrary() {
		mSongs = new ConcurrentHashMap<Integer, ArrayList<Song>>();
		
		for (int i = 0; i < 200; i++) {
			mSongs.put(i, new ArrayList<Song>());
		}
	}
	
	/**
	 * Enters a song into the library.
	 * @param song
	 */
	public void add(Song song) {
		mSongs.get(song.getBPM()).add(song);
	}
	
	public Song getForPace(int bpm) {
		Log.i("[songlibrary] bpm is...", "" + bpm);
		Song s = getForPace(bpm, 0, 0);
		if (s == null) {
			resetSongs();
			return getForPace(bpm);
		}
		else {
			return s;
		}
	}
	
	private void resetSongs() {
		for (ArrayList<Song> list : mSongs.values()) {
			for (Song s : list) {
				s.setLastPlay(0);
			}
		}
	}

	public Song getForPace(int bpm, int direction, int distance) {
		//Log.i("looking for a song with pace...", "" + bpm);
		ArrayList<Song> possibilities = mSongs.get(bpm);
		for (Song s : possibilities) {
			Log.i("time diff", "" + (System.currentTimeMillis() - s.getLastPlay()));
			if (System.currentTimeMillis() - s.getLastPlay() > 3600000) {
				return s;
			}
		}	
		if (bpm >= mSongs.size() - 1 || bpm == 0/* || distance > 40*/) {
			return null;
		}
		
		if (direction == 0) {
			Song s1 = getForPace(bpm-1, -1, ++distance);
			Song s2 = getForPace(bpm+1, 1, ++distance);
			if (s1 == null) {
				return s2;
			}
			if (s2 == null) {
				return s1;
			}
			int diff1 = Math.abs(bpm - s1.getBPM());
			int diff2 = Math.abs(bpm - s2.getBPM());
			return (diff1 < diff2) ? s1 : s2;
		}
		else if (direction == -1) {
			return getForPace(bpm-1, -1, ++distance);
		}
		else {
			return getForPace(bpm+1, 1, ++distance);
		}	
	}
}
