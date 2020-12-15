package com.amin.saazangplayer.model;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.amin.saazangplayer.constants.SettingsConstant;
import com.amin.saazangplayer.view.MainActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

//@Singleton
public class FavoriteRecentlyRepository {

    private Application application;
    private MutableLiveData<List<AudioModel>> favoriteLiveData = new MutableLiveData<>();
    private MutableLiveData<List<AudioModel>> recentlyLiveData = new MutableLiveData<>();
    private MutableLiveData<List<AudioModel>> latestLiveData = new MutableLiveData<>();
    private SharedPreferences sharedPreferences;

    @Inject
    public FavoriteRecentlyRepository(Application application) {
        this.application = application;
        this.sharedPreferences = MainActivity.getInstance().getSharedPreferences
                (SettingsConstant.RECENTLY_FAVORITE_PREF_NAME, Context.MODE_PRIVATE);

        loadFavoriteMusic();
        loadRecentMusic();
        loadLatestMusicList();
        removeDeletedMusics();


    }

    public void addRemoveFavorite(AudioModel audioModel) {
        List<AudioModel> tempList;
        if (favoriteLiveData.getValue() != null) {
            tempList = favoriteLiveData.getValue();
            if (isExistInFavorites(audioModel))
                tempList.remove(audioModel);
            else
                tempList.add(0, audioModel);

        } else {
            tempList  = new ArrayList<>();
            tempList.add(audioModel);
            //favoriteLiveData.postValue(audioModels);
        }
        favoriteLiveData.postValue(tempList);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String gsonStr = gson.toJson(favoriteLiveData.getValue());
        editor.putString(SettingsConstant.FAVORITE_MUSIC, gsonStr);
        editor.commit();


    }

    private MutableLiveData<List<AudioModel>> loadFavoriteMusic() {

        List<AudioModel> list = new ArrayList<>();
        favoriteLiveData = new MutableLiveData<>();
        String serializedAudios = sharedPreferences.getString(SettingsConstant.FAVORITE_MUSIC, null);

        if (serializedAudios != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<AudioModel>>() {
            }.getType();
            list = gson.fromJson(serializedAudios, type);
        }
        favoriteLiveData.postValue(list);

        return favoriteLiveData;
    }

    public void addRecentlyPlayed(AudioModel audioModel) {

        if (recentlyLiveData.getValue() != null) {
            if (recentlyLiveData.getValue().size() > 200)
                recentlyLiveData.getValue().remove(recentlyLiveData.getValue().size() - 1);

            if (isExistInRecently(audioModel))
                recentlyLiveData.getValue().remove(audioModel);

            recentlyLiveData.getValue().add(0, audioModel);
        } else {
            List<AudioModel> audioModels = new ArrayList<>();
            audioModels.add(audioModel);
            recentlyLiveData.postValue(audioModels);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String gsonStr = gson.toJson(recentlyLiveData.getValue());
        editor.putString(SettingsConstant.RECENTLY_PLAYED, gsonStr);
        editor.commit();

    }

    private MutableLiveData<List<AudioModel>> loadRecentMusic() {
        List<AudioModel> list = new ArrayList<>();
        recentlyLiveData = new MutableLiveData<>();
        String serializedAudios = sharedPreferences.getString(SettingsConstant.RECENTLY_PLAYED, null);

        if (serializedAudios != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<AudioModel>>() {
            }.getType();
            list = gson.fromJson(serializedAudios, type);
        }
        recentlyLiveData.postValue(list);

        return recentlyLiveData;
    }

    public boolean isExistInFavorites(AudioModel audioModel) {
        boolean isExist = false;
        if (favoriteLiveData.getValue() != null && favoriteLiveData.getValue().size() > 0)
            isExist = favoriteLiveData.getValue().contains(audioModel);
        return isExist;
    }

    public boolean isExistInRecently(AudioModel audioModel) {
        boolean isExist = false;
        if (recentlyLiveData.getValue() != null && recentlyLiveData.getValue().size() > 0)
            isExist = recentlyLiveData.getValue().contains(audioModel);
        return isExist;
    }

    public void saveCurrentList(List<AudioModel> audioModelList) {

        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String gsonStr = gson.toJson(audioModelList);

        editor.putString(SettingsConstant.LATEST_PLAYED_LIST, gsonStr);
        editor.commit();
    }

    private MutableLiveData<List<AudioModel>> loadLatestMusicList() {
        List<AudioModel> list = new ArrayList<>();
        latestLiveData = new MutableLiveData<>();
        String serializedAudios = sharedPreferences.getString(SettingsConstant.LATEST_PLAYED_LIST, null);

        if (serializedAudios != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<AudioModel>>() {
            }.getType();
            list = gson.fromJson(serializedAudios, type);
        }
        latestLiveData.postValue(list);

        return latestLiveData;
    }

    public void saveCurrentMusicIndexAndPosition(AudioModel audioModel, int position) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String gsonStr = gson.toJson(audioModel);
        editor.putString(SettingsConstant.LATEST_PLAYED_MUSIC, gsonStr);
        editor.putInt(SettingsConstant.LATEST_PLAYED_MUSIC_POSITION, position);
        editor.commit();


    }


    public void removeDeletedMusics() {
        //If a music remove from device should to be deleted from recently favorite and latest.


        MainActivity.getInstance().musicViewModel.getMusicLiveData().observe(MainActivity.getInstance(), new Observer<List<AudioModel>>() {
            @Override
            public void onChanged(List<AudioModel> audioModels) {

                removeFromCurrentIfDeleted(audioModels);
                removeFromRecentIfDeleted(audioModels);
                removeFromFavoritesIfDeleted(audioModels);
            }
        });

    }

    private void removeFromFavoritesIfDeleted(List<AudioModel> audioModels) {

        List<AudioModel> temList = favoriteLiveData.getValue();

        if (temList != null) {
            for (int i = 0; i < temList.size(); i++) {
                if (!audioModels.contains(temList.get(i))) {
                    temList.remove(i);
                }
            }
            favoriteLiveData.postValue(temList);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();
            String gsonStr = gson.toJson(temList);
            editor.putString(SettingsConstant.FAVORITE_MUSIC, gsonStr);
            editor.commit();
        }


    }

    private void removeFromRecentIfDeleted(List<AudioModel> audioModels) {
        List<AudioModel> temList = recentlyLiveData.getValue();
        if (temList != null) {
            for (int i = 0; i < temList.size(); i++) {
                if (!audioModels.contains(temList.get(i))) {
                    temList.remove(i);

                }
            }
            recentlyLiveData.postValue(temList);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();
            String gsonStr = gson.toJson(temList);
            editor.putString(SettingsConstant.RECENTLY_PLAYED, gsonStr);
            editor.commit();
        }
    }

    private void removeFromCurrentIfDeleted(List<AudioModel> audioModels) {
        List<AudioModel> temList = latestLiveData.getValue();

        if (temList != null) {
            for (int i = 0; i < latestLiveData.getValue().size(); i++) {
                if (!audioModels.contains(latestLiveData.getValue().get(i))) {
                    latestLiveData.getValue().remove(i);
                }
            }
            latestLiveData.postValue(temList);
            saveCurrentList(temList);
        }
    }


    public AudioModel getLatestMusicIndex() {
        String serializedAudios = sharedPreferences.getString(SettingsConstant.LATEST_PLAYED_MUSIC, null);
        AudioModel audioModel = null;
        if (serializedAudios != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<AudioModel>() {
            }.getType();
            audioModel = gson.fromJson(serializedAudios, type);
        }
        // int index = sharedPreferences.getInt(SettingsConstant.LATEST_PLAYED_MUSIC, -1);

        return audioModel;
    }

    public int getLatestMusicPosition() {
        int position = sharedPreferences.getInt(SettingsConstant.LATEST_PLAYED_MUSIC_POSITION, 0);

        return position;
    }

    public MutableLiveData<List<AudioModel>> getFavoriteLiveData() {
        return favoriteLiveData;
    }

    public MutableLiveData<List<AudioModel>> getRecentlyLiveData() {
        return recentlyLiveData;
    }

    public MutableLiveData<List<AudioModel>> getLatestLiveData() {
        Log.d("AppTag", "Latest3");
        return latestLiveData;
    }

    public void clear() {
        latestLiveData.getValue().clear();
        favoriteLiveData.getValue().clear();
        recentlyLiveData.getValue().clear();


    }
}


