package com.amin.saazangplayer.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amin.saazangplayer.R;
import com.amin.saazangplayer.interfaces.PlayListener;
import com.amin.saazangplayer.model.AudioModel;

import java.util.List;

public class RecyclerViewAdapterMusic extends RecyclerView.Adapter<RecyclerViewAdapterMusic.ViewHolder> {

    private PlayListener playListener;

    private List<AudioModel> audioModelList;
    private Context context;
    private String tagName;

    public RecyclerViewAdapterMusic(List<AudioModel> audioModelList, Context context) {
        this.audioModelList = audioModelList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AudioModel audioModel = audioModelList.get(position);

        holder.textViewMusic.setText(audioModel.getName());
        holder.textViewMusicArtist.setText(audioModel.getArtist());
        holder.textViewDuration.setText(MainActivity.timeConverter(Long.parseLong(audioModel.getDuration())));
        holder.linearLayout.setTag(audioModel.getName());
        holder.setIsRecyclable(false);
    }


    @Override
    public int getItemCount() {
        return audioModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMusic;
        TextView textViewMusicArtist;
        TextView textViewDuration;
        LinearLayout linearLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewMusic = itemView.findViewById(R.id.textviewMusic);
            textViewMusicArtist = itemView.findViewById(R.id.textviewMusicArtist);
            textViewDuration = itemView.findViewById(R.id.textviewMusicDuration);
            linearLayout = itemView.findViewById(R.id.linearLayoutMusicItem);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int musicIndex = getAdapterPosition();
                    //AudioModel audioModel = audioModelList.get(musicIndex);
                    playListener.play(musicIndex);
                    ViewHolder.this.linearLayout.setBackgroundColor(MainActivity.getInstance().getResources().getColor(R.color.selectedItemBackgroundColor));
                }
            });
        }


    }

    public void setPlayListener(PlayListener playListener) {
        this.playListener = playListener;
    }

    public String getSetTagName() {
        return tagName;
    }
}
