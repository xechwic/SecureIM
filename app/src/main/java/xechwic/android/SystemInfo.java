package xechwic.android;

public class SystemInfo {
	private String server_addr;
	private int server_port;
	private int audio_volume;
	private int record_save_days;
	public int getRecord_save_days() {
		return record_save_days;
	}
	public void setRecord_save_days(int record_save_days) {
		this.record_save_days = record_save_days;
	}
	public String getServer_addr() {
		return server_addr;
	}
	public void setServer_addr(String server_addr) {
		this.server_addr = server_addr;
	}
	public int getServer_port() {
		return server_port;
	}
	public void setServer_port(int server_port) {
		this.server_port = server_port;
	}
	public int getAudio_volume() {
		return audio_volume;
	}
	public void setAudio_volume(int audio_volume) {
		this.audio_volume = audio_volume;
	}
}
