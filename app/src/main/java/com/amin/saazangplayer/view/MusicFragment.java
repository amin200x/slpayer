package com.amin.saazangplayer.view;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amin.saazangplayer.R;
import com.amin.saazangplayer.constants.SettingsConstant;
import com.amin.saazangplayer.interfaces.FavoriteRecently;
import com.amin.saazangplayer.interfaces.PlayListener;
import com.amin.saazangplayer.interfaces.SendFolderAlbumNameListener;
import com.amin.saazangplayer.model.AudioModel;
import com.amin.saazangplayer.modelview.FavoriteRecentlyViewModel;
import com.amin.saazangplayer.modelview.MusicViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class MusicFragment extends Fragment implements PlayListener,
        SendFolderAlbumNameListener, FavoriteRecently, TextWatcher {

    private PlayListener playListener;
    private RecyclerView recyclerView;
    private RecyclerViewAdapterMusic recyclerViewAdapter;
    private MainActivity mainActivity;
    private MutableLiveData<List<AudioModel>> musicLiveData = new MutableLiveData<>();
    private List<AudioModel> audioModels = new ArrayList<>();

    public MusicFragment() {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mainActivity.getSelectedAudioModelList() != null) {
            musicLiveData.postValue(mainActivity.getSelectedAudioModelList());

        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        TimeUnit.SECONDS.sleep(2);
                        MainActivity.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                FavoriteRecentlyViewModel favRecentViewModel = MainActivity.getInstance().favRecenltViewModel;
                                if (favRecentViewModel != null &&
                                        favRecentViewModel.getLatestLiveData().getValue() != null &&
                                        favRecentViewModel.getLatestLiveData().getValue().size() > 0) {
                                    musicLiveData = favRecentViewModel.getLatestLiveData();

                                } else {
                                    if (MainActivity.getInstance().musicViewModel != null) {
                                        musicLiveData = MainActivity.getInstance().musicViewModel.getMusicLiveData();
                                    }
                                }
                                loadMusics(musicLiveData);
                                mainActivity.setMusicIndex(-1);
                                mainActivity.getNextshuffleIndexList().clear();
                                mainActivity.getPreviousShuffleIndexList().clear();
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }


    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.music_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerViewMusic);

        recyclerViewAdapter = new RecyclerViewAdapterMusic(audioModels, getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setPlayListener(MusicFragment.this);


    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mainActivity = (MainActivity) context;
            mainActivity.setSendFolderAlbumNameListener(this);
            mainActivity.setFavoriteRecently(this);
        }
       /* Log.d("AppTag", "Created");

        }*/
    }


    @Override
    public void play(int musicIndex) {
        playListener.play(musicIndex);
    }

    @Override
    public void receive(String favoriteRecently) {
        FavoriteRecentlyViewModel favoriteRecentlyViewModel = MainActivity.getInstance().favRecenltViewModel;
        if (favoriteRecently.equals(SettingsConstant.FAVORITE_MUSIC)) {
            if (favoriteRecentlyViewModel != null)
                musicLiveData = MainActivity.getInstance().favRecenltViewModel.getFavoriteLiveData();
        } else if (favoriteRecently.equals(SettingsConstant.RECENTLY_PLAYED)) {
            if (favoriteRecentlyViewModel != null)
                musicLiveData = favoriteRecentlyViewModel.getRecentlyLiveData();
        }
        loadMusics(musicLiveData);

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        //super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            MainActivity.getInstance().getEditTextSearch().setText("");
            MainActivity.getInstance().getEditTextSearch().addTextChangedListener(this);

            loadMusics(musicLiveData);

        }
    }


    @Override
    public void receiveFolderAlbumName(String folderAlbumName, boolean isAlbum) {
        MusicViewModel musicViewModel = MainActivity.getInstance().musicViewModel;
        if (isAlbum) {
            if (musicViewModel != null) {
                musicLiveData = musicViewModel.getMusicLiveDataByAlbum(folderAlbumName);
                loadMusics(musicLiveData);
            }
        } else {
            if (musicViewModel != null) {
                if (folderAlbumName.equals(getString(R.string.all_musics)))
                    musicLiveData = musicViewModel.getMusicLiveData();

                else
                    musicLiveData = musicViewModel.getMusicLiveDataByFolder(folderAlbumName);
                loadMusics(musicLiveData);
            }

        }
        mainActivity.setMusicIndex(-1);
        mainActivity.getNextshuffleIndexList().clear();
        mainActivity.getPreviousShuffleIndexList().clear();
    }

    private void loadMusics(MutableLiveData<List<AudioModel>> musicLiveData1) {
        musicLiveData.removeObservers(MainActivity.getInstance());
        musicLiveData1.observe(MainActivity.getInstance(), getObserver());

    }

    private Observer<? super List<AudioModel>> getObserver() {
        Observer<List<AudioModel>> observer = new Observer<List<AudioModel>>() {
            @Override
            public void onChanged(List<AudioModel> audioModels) {
                MusicFragment.this.audioModels.clear();
                MusicFragment.this.audioModels.addAll(audioModels);
                recyclerViewAdapter.notifyDataSetChanged();

                mainActivity.setSelectedAudioModelList(audioModels);

                int index = MainActivity.getInstance().getMusicIndex();
                if (audioModels != null && audioModels.size() > 0) {
                    if (index != -1 && audioModels.size() > index) {
                        MainActivity.getInstance().scrollAndSetItemBackground(audioModels.get(index));
                    }

                    // MainActivity.getInstance().favRecenltViewModel.saveCurrentList(audioModels);
                }

            }
        };

        return observer;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable value) {

        if (musicLiveData != null && musicLiveData.getValue() != null) {
            List<AudioModel> tempList = musicLiveData.getValue();
            MutableLiveData<List<AudioModel>> temLiveData = new MutableLiveData<>();
            List<AudioModel> audioModels = new ArrayList<>();

            for (AudioModel audioModel : tempList) {
                Log.d("AppTag", value.toString());
                if (audioModel.getName().toLowerCase().contains(value.toString().toLowerCase()) ||
                        audioModel.getArtist().toLowerCase().contains(value.toString().toLowerCase()))
                    audioModels.add(audioModel);
            }
            temLiveData.setValue(audioModels);
            loadMusics(temLiveData);

        }
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public void setPlayListener(PlayListener playListener) {
        this.playListener = playListener;
    }

}
