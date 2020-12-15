package com.amin.saazangplayer.modelview;

import android.app.Application;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.amin.saazangplayer.model.AlbumModel;
import com.amin.saazangplayer.model.AudioModel;
import com.amin.saazangplayer.model.FolderModel;
import com.amin.saazangplayer.model.MusicRepository;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

//@Singleton
public class MusicViewModel extends AndroidViewModel {
    private MusicRepository musicRepository;

    @Inject
    public MusicViewModel(@NonNull Application application) {
        super(application);

        musicRepository = new MusicRepository(application);
        musicRepository.loadMusic();

    }

    public MutableLiveData<List<AudioModel>> getMusicLiveData() {
        return musicRepository.getMusicLiveData();
    }

    public MutableLiveData<List<FolderModel>> getFolderLiveData() {
        return musicRepository.getFolderLiveData();
    }

    public MutableLiveData<List<String>> getGenreLiveData() {
        return musicRepository.getGenreLiveData();
    }

    public MutableLiveData<List<AlbumModel>> getAlbumLiveData() {
        return musicRepository.getAlbumLiveData();
    }

    public MutableLiveData<List<AudioModel>> getMusicLiveDataByAlbum(String albumName) {
        return musicRepository.getMusicLiveDataByAlbum(albumName);
    }

    public MutableLiveData<List<AudioModel>> getMusicLiveDataByFolder(String folderName) {
        return musicRepository.getMusicLiveDataByFolder(folderName);
    }

    public Bitmap getAlbumArt(String path) {
        return musicRepository.getAlbumArt(path);
    }

    public void clear() {
        musicRepository.clear();
    }
}
