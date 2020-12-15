package com.amin.saazangplayer.model;

import android.graphics.Bitmap;

public class AudioModel {
    private String path;
    private String name;
    private String album;
    private String artist;
    private Bitmap albumArt;
    private String duration;
    private long id;
    private String genre;

    public AudioModel() {
    }

    public AudioModel(String path, String name, String album,
                      String artist, String duration, long id, String genre) {
        this.path = path;
        this.name = name;
        this.album = album;
        this.artist = artist;
        this.id = id;
        this.duration = duration;
        this.genre = genre;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public Bitmap getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(Bitmap albumArt) {
        this.albumArt = albumArt;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AudioModel that = (AudioModel) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return artist != null ? artist.equals(that.artist) : that.artist == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (artist != null ? artist.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AudioModel{" +
                "path='" + path + '\'' +
                ", name='" + name + '\'' +
                ", album='" + album + '\'' +
                ", artist='" + artist + '\'' +
                ", albumArt='" + albumArt + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }
}
