package com.amin.saazangplayer.modelview;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.amin.saazangplayer.model.AudioModel;
import com.amin.saazangplayer.model.FavoriteRecentlyRepository;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

//@Singleton
public class FavoriteRecentlyViewModel extends AndroidViewModel {
    private FavoriteRecentlyRepository favoriteRecentlyRepository;

    @Inject
    public FavoriteRecentlyViewModel(@NonNull Application application) {
        super(application);

        favoriteRecentlyRepository = new FavoriteRecentlyRepository(application);

    }

    public void addRemoveFavorite(AudioModel audioModel) {
        favoriteRecentlyRepository.addRemoveFavorite(audioModel);
    }

    public MutableLiveData<List<AudioModel>> getFavoriteLiveData() {
        return favoriteRecentlyRepository.getFavoriteLiveData();
    }

    public void addRecentlyPlayed(AudioModel audioModel) {
        favoriteRecentlyRepository.addRecentlyPlayed(audioModel);
    }

    public MutableLiveData<List<AudioModel>> getRecentlyLiveData() {
        return favoriteRecentlyRepository.getRecentlyLiveData();
    }

    public boolean isExistInFavorites(AudioModel audioModel) {
        return favoriteRecentlyRepository.isExistInFavorites(audioModel);
    }

    public boolean isExistInRecently(AudioModel audioModel) {
        return favoriteRecentlyRepository.isExistInRecently(audioModel);
    }

    public void saveCurrentList(List<AudioModel> audioModelList) {
        favoriteRecentlyRepository.saveCurrentList(audioModelList);
    }
        public MutableLiveData<List<AudioModel>> getLatestLiveData() {
        return favoriteRecentlyRepository.getLatestLiveData();
    }
    public void saveCurrentMusicIndexAndPosition(AudioModel audioModel, int position) {
        favoriteRecentlyRepository.saveCurrentMusicIndexAndPosition(audioModel, position);
    }
    public AudioModel getLatestMusic() {
        return favoriteRecentlyRepository.getLatestMusicIndex();
    }
    public int getLatestMusicPosition() {
        return favoriteRecentlyRepository.getLatestMusicPosition();
    }

    public void clear(){
        favoriteRecentlyRepository.clear();
       // favoriteRecentlyRepository = null;

    }
   /* public void removeDeletedMusics(MutableLiveData<List<AudioModel>> listMutableLiveData) {
         favoriteRecentlyRepository.removeDeletedMusics(listMutableLiveData);
    }*/
}
