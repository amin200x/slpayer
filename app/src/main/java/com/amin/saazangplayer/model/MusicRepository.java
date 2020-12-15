package com.amin.saazangplayer.model;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.amin.saazangplayer.R;
import com.amin.saazangplayer.constants.SettingsConstant;
import com.amin.saazangplayer.view.MainActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

//@Singleton
public class MusicRepository {

    private Application application;
    private MutableLiveData<List<String>> genreLiveData = new MutableLiveData<>();
    private MutableLiveData<List<AudioModel>> musicLiveData = new MutableLiveData<>();
    private MutableLiveData<List<AlbumModel>> albumLiveData = new MutableLiveData<>();
    private MutableLiveData<List<FolderModel>> folderLiveData = new MutableLiveData<>();
    private MutableLiveData<List<AudioModel>> favoriteLiveData = null;
    private MutableLiveData<List<AudioModel>> recentlyLiveData = null;

    private SharedPreferences sharedPreferences;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    public MusicRepository(Application application) {

        this.application = application;
    }

    public void loadMusic() {


        List<String> genreList = new ArrayList<>();
        List<AudioModel> musicList = new ArrayList<>();
        List<AlbumModel> albumList = new ArrayList<>();
        List<FolderModel> folderList = new ArrayList<>();

        FileFilter fileFilter = getFileFilter();
        Cursor cursorAudio = getCursor();

        Observable<Cursor> observable = Observable.just(cursorAudio);
        compositeDisposable.add(observable
                .subscribeOn(Schedulers.computation())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Cursor>() {
                    @Override
                    public void onNext(@NonNull Cursor cursor) {
                        AudioModel audioModel;
                        AlbumModel albumModel;
                        FolderModel folderModel;

                        if (cursor != null) {
                            while (cursorAudio.moveToNext()) {
                                audioModel = new AudioModel();
                                albumModel = new AlbumModel();
                                folderModel = new FolderModel();

                                String path = cursorAudio.getString(0);
                                String album = cursorAudio.getString(1);
                                String artist = cursorAudio.getString(2);
                                String name = cursorAudio.getString(3);
                                String duration = cursorAudio.getString(4);
                                long id = cursorAudio.getLong(5);
                                // String genre = getGenre(id);

                                audioModel.setPath(path);
                                audioModel.setAlbum(album);
                                audioModel.setArtist(artist);
                                audioModel.setName(name);
                                audioModel.setDuration(duration);
                                audioModel.setId(id);
                                musicList.add(audioModel);

                                albumModel.setAlbum(album);
                                if (!albumList.contains(albumModel)) {
                                    albumModel.setAlbumArt(getAlbumArt(path));
                                    albumModel.setArtist(artist);
                                    albumList.add(albumModel);
                                }
                                try {
                                    File file = new File(audioModel.getPath());
                                    String folderName = file.getParentFile().getName();
                                    folderModel.setName(folderName);
                                    if (!folderList.contains(folderModel)) {
                                        folderModel.setFileCounts(file.getParentFile().listFiles(fileFilter).length);
                                        folderList.add(folderModel);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.d("AppTag", e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Collections.sort(albumList);
                        Collections.sort(folderList);
                        cursorAudio.close();
                        folderList.add(0, new FolderModel("", MainActivity.getInstance().getString(R.string.all_musics), musicList.size()));

                        musicLiveData.postValue(musicList);
                        genreLiveData.postValue(genreList);
                        folderLiveData.postValue(folderList);
                        albumLiveData.postValue(albumList);
                    }
                })
        );


    }

    private Cursor getCursor() {

        Uri uriAudios = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        String[] projectionAudio = {
                MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.AudioColumns.ARTIST,
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.DURATION,
                MediaStore.Audio.Media._ID

        };
        Cursor cursorAudio = application.getContentResolver().query(uriAudios, projectionAudio, selection, null, sortOrder);
        return cursorAudio;

    }

    private FileFilter getFileFilter() {
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                String[] suffixes = {".mp3", ".mp4", ".wav", ".wma", ".mp2", ".ogg", ".aac", ".oga", ".aa", ".amr", ".m4a", ".3ga", ".flac"};
                boolean isAudio = false;

                for (String suffix : suffixes) {
                    if (file.getName().toLowerCase().endsWith(suffix)) {
                        isAudio = true;
                        break;
                    }
                }

                return isAudio;
            }
        };
        return fileFilter;

    }

    public Bitmap getAlbumArt(String path) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        Bitmap bitmapAlbumArt = null;
        try {

            mmr.setDataSource(application, Uri.parse(path));
            byte[] data = mmr.getEmbeddedPicture();

            if (data != null) {
                bitmapAlbumArt = BitmapFactory.decodeByteArray(data, 0, data.length);

            }
        } catch (Exception e) {
            //e.printStackTrace();
        } finally {
            if (bitmapAlbumArt == null)
                bitmapAlbumArt = BitmapFactory.decodeResource(MainActivity.getInstance().getResources(), R.drawable.album_art);

            return bitmapAlbumArt;
        }

    }


    public MutableLiveData<List<AudioModel>> getMusicLiveDataByAlbum(final String albumeName) {
        List<AudioModel> audioModelList = new ArrayList<>();
        MutableLiveData<List<AudioModel>> audioModelLiveData = new MutableLiveData<>();
        Observable<AudioModel> observable = Observable.create(new ObservableOnSubscribe<AudioModel>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<AudioModel> emitter) throws Throwable {
                List<AudioModel> list = musicLiveData.getValue();
                for (AudioModel audioModel : list) {
                    if (audioModel.getAlbum().equals(albumeName)) {
                        emitter.onNext(audioModel);
                    }

                }
                emitter.onComplete();
            }
        });

        compositeDisposable.add(observable
                .subscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<AudioModel>() {
                    @Override
                    public void onNext(@NonNull AudioModel audioModel) {
                        audioModelList.add(audioModel);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        audioModelLiveData.postValue(audioModelList);
                    }
                })


        );


        return audioModelLiveData;
    }


    public MutableLiveData<List<AudioModel>> getMusicLiveDataByFolder(final String folderName) {
        List<AudioModel> audioModelList = new ArrayList<>();
        MutableLiveData<List<AudioModel>> audioModelLiveData = new MutableLiveData<>();

        Observable<AudioModel> observable = Observable.create(new ObservableOnSubscribe<AudioModel>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<AudioModel> emitter) throws Throwable {
                List<AudioModel> list = musicLiveData.getValue();
                for (AudioModel audioModel : list) {
                    File file = new File(audioModel.getPath());
                    String musicFolder = file.getParentFile().getName();
                    if (musicFolder.equals(folderName)) {
                        emitter.onNext(audioModel);
                    }

                }
                emitter.onComplete();
            }
        });

        compositeDisposable.add(observable
                .subscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<AudioModel>() {
                    @Override
                    public void onNext(@NonNull AudioModel audioModel) {
                        Log.d("AppTag", audioModel.getName());
                        audioModelList.add(audioModel);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        audioModelLiveData.postValue(audioModelList);

                    }
                })
        );

        return audioModelLiveData;
    }





    public MutableLiveData<List<String>> getGenreLiveData() {
        return genreLiveData;
    }

    public MutableLiveData<List<AudioModel>> getMusicLiveData() {
        return musicLiveData;
    }

    public MutableLiveData<List<AlbumModel>> getAlbumLiveData() {
        return albumLiveData;
    }

    public MutableLiveData<List<FolderModel>> getFolderLiveData() {
        return folderLiveData;
    }


    public void clear() {
        compositeDisposable.clear();
    }
}


