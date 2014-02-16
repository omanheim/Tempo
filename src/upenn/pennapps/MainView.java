package upenn.pennapps;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MainView extends View {

	//public static ConcurrentHashMap<Integer, ArrayList<Song>> mSongs;
	public static SongLibrary mSongs;
	private int songCount;

	private class BPMScannerThread extends AsyncTask<Song, Void, Void> {

		protected Void doInBackground(Song... params) {
			String api_key = "TUKBKAR450KQF8BPA";
			HttpClient client = new DefaultHttpClient();
			for (int songIndex = 0; songIndex < params.length; songIndex++) {
			    Song song = params[songIndex];
				try {
					String url = "http://developer.echonest.com/api/v4/song/search?"
							+ "api_key="
							+ api_key
							+ "&title="
							+ java.net.URLEncoder.encode(song.getTitle(), "UTF-8");

					HttpGet httpget = new HttpGet(url);
					HttpResponse response = client.execute(httpget);
					HttpEntity entity = response.getEntity();

					String songResponse = "";
					JSONObject songJSON = null;
					if (entity != null) {
						InputStream instream = entity.getContent();
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(instream));
						try {
							songResponse = reader.readLine();
							songJSON = new JSONObject(songResponse);
						} finally {
							instream.close();
						}
					}
					if (!songJSON.getJSONObject("response")
							.getJSONObject("status").getString("message")
							.equals("Success")) {
						Log.i("sleepy time", "zzzzzzz");
						Thread.sleep(20000);
						songIndex--;
					} else {
						JSONArray songs = songJSON.getJSONObject("response")
								.getJSONArray("songs");
						String songID = null;
						for (int i = 0; i < songs.length(); i++) {
							if (song.getArtist().equals(songs.getJSONObject(i)
									.getString("artist_name"))) {
								songID = songs.getJSONObject(i).getString("id");
								break;
							}
						}
						if (songID == null)
							continue;
						url = "http://developer.echonest.com/api/v4/song/profile?"
								+ "api_key="
								+ api_key
								+ "&id="
								+ songID
								+ "&bucket=audio_summary;";
						httpget = new HttpGet(url);
						response = client.execute(httpget);
						entity = response.getEntity();

						if (entity != null) {
							InputStream instream = entity.getContent();
							BufferedReader reader = new BufferedReader(
									new InputStreamReader(instream));
							try {
								songResponse = reader.readLine();
								songJSON = new JSONObject(songResponse);
								song.setBPM(songJSON.getJSONObject("response")
										.getJSONArray("songs").getJSONObject(0)
										.getJSONObject("audio_summary")
										.getDouble("tempo"));
								//Log.i("bpm to enter", "" + song.getBPM());
								mSongs.add(song);
								songCount++;
								Log.i("songs loaded", "" + songCount);
							} catch (JSONException e) {
								Log.e("oops", "too bad");
							} finally {
								instream.close();
							}
						}
					}
				} catch (Exception e) {
					Log.e("oli error", e.getMessage());
					e.printStackTrace();
				}
			}
			
			return null;
		}

		protected void onPostExecute(Void result) {
			Log.i("execution complete!", "hooray!");
			/*
			for (Integer i : mSongs.keySet()) {
				Log.i("songs with bpm", "" + i);
				for (Song s : mSongs.get(i)) {
					Log.i("song title, artist", s.title + " - " + s.artist);
				}
			}
			*/
		}
	}

	public MainView(Context context) {
		super(context);
		init();
	}

	public MainView(Context context, AttributeSet as) {
		super(context, as);
		init();
	}

	/**
         */
	private void init() {
		setBackgroundResource(R.drawable.watercolor);
		mSongs = new SongLibrary();

		Cursor c = getContext().getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
				null);

		int index = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);

		ArrayList<Song> songs = new ArrayList<Song>();
		while (c.moveToNext()) {
			songs.add(new Song(
					c.getString(index),
					c.getString(c
							.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
					c.getString(c
							.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))));
		}
		Song[] a = new Song[songs.size()];
		new BPMScannerThread().execute(songs.toArray(a));
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
