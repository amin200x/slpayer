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
import com.amin.saazangplayer.model.AudioModel;
import com.amin.saazangplayer.model.FolderModel;

import java.util.ArrayList;
import java.util.List;

public class FolderFragment extends Fragment implements ItemSelectedListener,
        TextWatcher {

    private MutableLiveData<List<FolderModel>> liveDataFolderModel;
    public interface SendFolderNameListener {
        void sendFolderName(String folderName);
    }

    private RecyclerView recyclerView;
    private SendFolderNameListener sendFolderNameListener;

    public FolderFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.folder_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerViewFolder);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (MainActivity.getInstance().musicViewModel != null) {
            liveDataFolderModel = MainActivity.getInstance().musicViewModel.getFolderLiveData();
                loadFolders(liveDataFolderModel);
        }
    }


    @Override
    public void send(String albumName) {
        sendFolderNameListener.sendFolderName(albumName);
    }

    public void setSendFolderNameListener(SendFolderNameListener sendFolderNameListener) {
        this.sendFolderNameListener = sendFolderNameListener;
    }


    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable value) {

        if (liveDataFolderModel != null && liveDataFolderModel.getValue() != null) {
            List<FolderModel> tempList = liveDataFolderModel.getValue();
            MutableLiveData<List<FolderModel>> temLiveData = new MutableLiveData<>();
            List<FolderModel> folderModels = new ArrayList<>();

            for (FolderModel folderModel : tempList) {
                if (folderModel.getName().toLowerCase().contains(value.toString().toLowerCase()))
                    folderModels.add(folderModel);
            }
            temLiveData.setValue(folderModels);
            loadFolders(temLiveData);

        }
    }

    private void loadFolders(MutableLiveData<List<FolderModel>> liveDataFolderModel) {
        liveDataFolderModel.observe(MainActivity.getInstance(), new Observer<List<FolderModel>>() {
            @Override
            public void onChanged(List<FolderModel> folderModels) {
                RecyclerViewAdapterFolder recyclerViewAdapter = new RecyclerViewAdapterFolder(
                        folderModels, getActivity());
                recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(recyclerViewAdapter);

                recyclerViewAdapter.setItemSelectedListener(FolderFragment.this);
            }
        });
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
