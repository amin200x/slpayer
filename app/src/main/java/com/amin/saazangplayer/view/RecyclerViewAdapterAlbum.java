package com.amin.saazangplayer.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amin.saazangplayer.R;
import com.amin.saazangplayer.interfaces.ItemSelectedListener;
import com.amin.saazangplayer.model.AlbumModel;

import java.util.List;

public class RecyclerViewAdapterAlbum extends RecyclerView.Adapter<RecyclerViewAdapterAlbum.ViewHolder> {

    private List<AlbumModel> albumModelList;
    private Context context;
    private ItemSelectedListener itemSelectedListener;

    public RecyclerViewAdapterAlbum(List<AlbumModel> albumModelList, Context context) {
        this.albumModelList = albumModelList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AlbumModel albumModel = albumModelList.get(position);

        holder.textViewAlbumName.setText(albumModel.getAlbum() + " | " + albumModel.getArtist());
      //  holder.textViewSongCount.setText(String.valueOf(albumModel.getCount()));
        if (albumModel.getAlbumArt() != null)
            holder.imageViewAlbumArt.setImageBitmap(albumModel.getAlbumArt());

    }

    @Override
    public int getItemCount() {
        return albumModelList.size();
    }

    public class  ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewAlbumName;
       // TextView textViewSongCount;
        ImageView imageViewAlbumArt;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewAlbumName = itemView.findViewById(R.id.textviewAlbumName);
           // textViewSongCount = itemView.findViewById(R.id.textviewSongCount);
            imageViewAlbumArt = itemView.findViewById(R.id.imageviewAlbumArt);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String albumName = albumModelList.get(getAdapterPosition()).getAlbum();
                    itemSelectedListener.send(albumName);
                }
            });
        }
    }

    public void setItemSelectedListener(ItemSelectedListener itemSelectedListener) {
        this.itemSelectedListener = itemSelectedListener;
    }
}
