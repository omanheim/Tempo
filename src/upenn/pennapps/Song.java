package upenn.pennapps;

public class Song {

	public String file;
	public String title;
	public String artist;
	private double bpm;

	public Song(String file, String title, String artist) {
		this.title = title;
		this.artist = artist;
	}

	public int getBPM() {
		return (int) bpm;
	}

	public void setBPM(double bpm) {
		this.bpm = bpm;
	}
}