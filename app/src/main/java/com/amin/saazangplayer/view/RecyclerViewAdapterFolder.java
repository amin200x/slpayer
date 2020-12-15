package com.amin.saazangplayer.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amin.saazangplayer.R;
import com.amin.saazangplayer.interfaces.ItemSelectedListener;
import com.amin.saazangplayer.model.FolderModel;

import java.util.List;

public class RecyclerViewAdapterFolder extends RecyclerView.Adapter<RecyclerViewAdapterFolder.ViewHolder> {


    private ItemSelectedListener itemSelectedListener;
    private List<FolderModel> folderModelList;
    private Context context;

    public RecyclerViewAdapterFolder(List<FolderModel> folderModelList, Context context) {
        this.folderModelList = folderModelList;
        this.context = context;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.folder_item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FolderModel folderModel = folderModelList.get(position);

        holder.textViewFolderName.setText(folderModel.getName());
        holder.textViewFileCount.setText(String.valueOf(folderModel.getFileCounts()));

    }

    @Override
    public int getItemCount() {
        return folderModelList.size();
    }

    public class  ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewFolderName;
        TextView textViewFileCount;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewFolderName = itemView.findViewById(R.id.textviewFolderName);
            textViewFileCount = itemView.findViewById(R.id.textviewFileCount);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String folderName = folderModelList.get(getAdapterPosition()).getName();
                    itemSelectedListener.send(folderName);
                }
            });
        }
    }

    public void setItemSelectedListener(ItemSelectedListener itemSelectedListener) {
        this.itemSelectedListener = itemSelectedListener;
    }
}
