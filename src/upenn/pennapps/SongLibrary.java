package upenn.pennapps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.util.Log;

public class SongLibrary {
	
	private ConcurrentHashMap<Integer, ArrayList<Song>> mSongs;
	
	/**
	 * Constructs a SongLibrary with an empty collection of songs
	 * for bpm's of 0 to 200.
	 * @throws IOException 
	 * @throws StreamCorruptedException 
	 * @throws ClassNotFoundException 
	 */
	
	private Context mContext;
	
	@SuppressWarnings("unchecked")
	public SongLibrary(Context c) throws StreamCorruptedException, IOException, ClassNotFoundException {
		mContext = c;
		File f = new File("song_library");
		if (f.exists()) {
			System.err.println("found file!");
			FileInputStream s = c.openFileInput("song_library");
			ObjectInputStream ois = new ObjectInputStream(s);
			mSongs =  (ConcurrentHashMap<Integer, ArrayList<Song>>) ois.readObject();
			ois.close();
			s.close();
		} else {
			System.err.println("didn't found file!");
			mSongs = new ConcurrentHashMap<Integer, ArrayList<Song>>();
			for (int i = 0; i < 200; i++) {
				mSongs.put(i, new ArrayList<Song>());
			}
		}
	}
	
	void close() throws IOException {
		FileOutputStream s = mContext.openFileOutput("song_library", Context.MODE_PRIVATE);
		ObjectOutputStream ois = new ObjectOutputStream(s);
		ois.writeObject(mSongs);
		ois.close();
		s.close();
	}
	
	/**
	 * Enters a song into the library.
	 * @param song
	 */
	public void add(Song song) {
		mSongs.get(song.getBPM()).add(song);
	}
	
	public Song getForPace(int bpm) {
		//Log.i("[songlibrary] bpm is...", "" + bpm);
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
	
	public Set<Integer> keySet() {
		return mSongs.keySet();
	}
	
	public ArrayList<Song> get(Integer i) {
		return mSongs.get(i);
	}
}
