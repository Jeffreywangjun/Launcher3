package com.android.launcher3.music;

public class Music {

	private String title;
	private String singer;
	private String album;
	private String url;
	private long size;
	private long time;
	private long albumid;
	private long songid;
	public long getSongid() {
		return songid;
	}

	public void setSongid(long songid) {
		this.songid = songid;
	}

	private String name;

	public String getName() {
		return name;
	}

	public long getAlbumid() {
		return albumid;
	}

	public void setAlbumid(long albumid) {
		this.albumid = albumid;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSinger() {
		return singer;
	}

	public void setSinger(String singer) {
		this.singer = singer;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

}
