package com.amin.saazangplayer.view;

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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amin.saazangplayer.R;
import com.amin.saazangplayer.interfaces.ItemSelectedListener;
import com.amin.saazangplayer.model.AlbumModel;

import java.util.ArrayList;
import java.util.List;

public class AlbumFragment extends Fragment implements ItemSelectedListener,
        TextWatcher {
    public interface SendAlbumNameListener {
        void sendAlbumName(String albumName);
    }

    private SendAlbumNameListener sendAlbumNameListener;

    private MutableLiveData<List<AlbumModel>> liveDataAlbumModel;

    private RecyclerView recyclerView;

    public AlbumFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.album_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerViewAlbum);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (MainActivity.getInstance().musicViewModel != null)
            liveDataAlbumModel = MainActivity.getInstance().musicViewModel.getAlbumLiveData();
        loadAlbums(liveDataAlbumModel);
    }

    private void loadAlbums(MutableLiveData<List<AlbumModel>> liveDataAlbumModel) {
        liveDataAlbumModel.observe(MainActivity.getInstance(), new Observer<List<AlbumModel>>() {
            @Override
            public void onChanged(List<AlbumModel> albumModels) {
                RecyclerViewAdapterAlbum adapterAlbum = new RecyclerViewAdapterAlbum(albumModels, getActivity());
                recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(adapterAlbum);

                adapterAlbum.setItemSelectedListener(AlbumFragment.this);
            }
        });
    }

    @Override
    public void send(String albumName) {
        sendAlbumNameListener.sendAlbumName(albumName);
    }

    public void setSendAlbumNameListener(SendAlbumNameListener sendAlbumNameListener) {
        this.sendAlbumNameListener = sendAlbumNameListener;
    }


    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable value) {

        if (liveDataAlbumModel != null && liveDataAlbumModel.getValue() != null) {
            List<AlbumModel> tempList = liveDataAlbumModel.getValue();
            MutableLiveData<List<AlbumModel>> temLiveData = new MutableLiveData<>();
            List<AlbumModel> folderModels = new ArrayList<>();

            for (AlbumModel albumModel : tempList) {
                if (albumModel.getAlbum().toLowerCase().contains(value.toString().toLowerCase()) ||
                        albumModel.getArtist().toLowerCase().contains(value.toString().toLowerCase()))
                    folderModels.add(albumModel);
            }
            temLiveData.setValue(folderModels);
            loadAlbums(temLiveData);

        }
    }



    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        //super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            MainActivity.getInstance().getEditTextSearch().setText("");
            MainActivity.getInstance().getEditTextSearch().addTextChangedListener(this);
        }
    }
}
