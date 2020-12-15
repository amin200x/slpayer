package com.amin.saazangplayer.model;

import android.graphics.Bitmap;

public class AlbumModel implements Comparable<AlbumModel> {
    private String album;
    private String artist;
    private int count;
    private Bitmap albumArt;

    public AlbumModel() {
    }

    public AlbumModel(String name, String album, String artist) {
        this.album = album;
        this.artist = artist;
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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Bitmap getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(Bitmap albumArt) {
        this.albumArt = albumArt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AlbumModel that = (AlbumModel) o;

        return album != null ? album.equals(that.album) : that.album == null;
    }

    @Override
    public int hashCode() {
        return album != null ? album.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "AudioModel{" +
                ", album='" + album + '\'' +
                ", artist='" + artist + '\'' +
                ", albumArt='" + albumArt + '\'' +
                '}';
    }

    @Override
    public int compareTo(AlbumModel other) {
        return this.getAlbum().compareToIgnoreCase(other.getAlbum());
    }
}
