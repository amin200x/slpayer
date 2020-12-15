package com.amin.saazangplayer.model;


public class FolderModel implements Comparable<FolderModel> {
    private String path;
    private String name;
    private int fileCounts;

    public FolderModel() {
    }

    public FolderModel(String path, String name, int fileCounts) {
        this.path = path;
        this.name = name;
        this.fileCounts = fileCounts;
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

    public int getFileCounts() {
        return fileCounts;
    }

    public void setFileCounts(int fileCounts) {
        this.fileCounts = fileCounts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FolderModel that = (FolderModel) o;

        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "AudioModel{" +
                "path='" + path + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public int compareTo(FolderModel other) {
        return this.name.compareToIgnoreCase(other.name);
    }
}
