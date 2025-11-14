package com.example.musicplayer;

import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable {
    private String title;
    private String artist;
    private String path;
    private long duration; // milliseconds
    private String albumArt;

    public Song(String title, String artist, String path, long duration, String albumArt) {
        this.title = title;
        this.artist = artist;
        this.path = path;
        this.duration = duration;
        this.albumArt = albumArt;
    }

    protected Song(Parcel in) {
        title = in.readString();
        artist = in.readString();
        path = in.readString();
        duration = in.readLong();
        albumArt = in.readString();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getPath() {
        return path;
    }

    public long getDuration() {
        return duration;
    }

    public String getAlbumArt() {
        return albumArt;
    }

    public String getFormattedDuration() {
        int seconds = (int) (duration / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(path);
        dest.writeLong(duration);
        dest.writeString(albumArt);
    }
}