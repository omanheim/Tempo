package upenn.pennapps;

public class Song {

	private String file;
	private String title;
	private String artist;
	private long lastPlay;
	private double bpm;

	public Song(String file, String title, String artist) {
		this.file = file;
		this.setTitle(title);
		this.setArtist(artist);
		lastPlay = 0;
	}

	/**
	 * Gets the beats per minute of a song.
	 * @return bpm (as int)
	 */
	public int getBPM() {
		return (int) bpm;
	}

	public void setBPM(double bpm) {
		this.bpm = bpm;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getLastPlay() {
		return lastPlay;
	}

	public void setLastPlay(long lastPlay) {
		this.lastPlay = lastPlay;
	}
	
	public void setLastPlay() {
		this.lastPlay = System.currentTimeMillis();
	}
}