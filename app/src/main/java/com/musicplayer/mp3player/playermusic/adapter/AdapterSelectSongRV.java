package com.musicplayer.mp3player.playermusic.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.musicplayer.mp3player.playermusic.equalizer.R;
import com.musicplayer.mp3player.playermusic.database.thumnail.AppDatabase;
import com.musicplayer.mp3player.playermusic.database.thumnail.ThumbnailMusic;
import com.musicplayer.mp3player.playermusic.model.MusicItem;
import com.musicplayer.mp3player.playermusic.utils.AppUtils;
import com.musicplayer.mp3player.playermusic.utils.ArtworkUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AdapterSelectSongRV extends RecyclerView.Adapter<AdapterSelectSongRV.SelectSongViewHolder> {
    private final Context context;
    private final OnClickSelectSongListener mListener;
    private final ArrayList<MusicItem> listSong = new ArrayList<>();
    private final ArrayList<MusicItem> listSongSelect = new ArrayList<>();
    private int maxCount = 0;
    private AppDatabase dbThumbnail;

    public AdapterSelectSongRV(Context context, OnClickSelectSongListener listener) {
        this.context = context;
        mListener = listener;
        dbThumbnail = AppDatabase.Companion.buildDatabase(context);
    }

    @NonNull
    @Override
    public SelectSongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.item_add_song, parent, false);
        return new SelectSongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectSongViewHolder holder, int position) {
        MusicItem item = listSong.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return listSong.size();
    }

    public ArrayList<MusicItem> getListSongSelect() {
        return listSongSelect;
    }

    public ArrayList<MusicItem> getListSong() {
        return listSong;
    }

    public void setData(ArrayList<MusicItem> lstSong, ArrayList<MusicItem> songDB) {
        listSong.clear();
        listSong.addAll(lstSong);
        for (MusicItem song : songDB) {
            for (int i = 0; i < listSong.size(); i++) {
                if (song.id == listSong.get(i).id) {
                    listSong.get(i).setDefaultSelected(true);
                    break;
                }
            }
        }
        maxCount = listSong.size() - songDB.size();
        if (listSong.size() == songDB.size()) {
            mListener.onSelectAllSong(true);
        }
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void selectSong(MusicItem song) {
        for (int i = 0; i < listSong.size(); i++) {
            MusicItem itemCompare = listSong.get(i);
            if (itemCompare.id == song.id) {
                if (song.isSelected) {
                    listSong.get(i).setSelected(true);
                    listSongSelect.add(song);
                } else {
                    listSong.get(i).setSelected(false);
                    listSongSelect.remove(song);
                }
            }
        }
        Log.e("Size", maxCount + " - " + listSongSelect.size());
        mListener.onSelectAllSong(maxCount == listSongSelect.size());
        notifyDataSetChanged();
    }

    public void setSelectAll(boolean isAll) {
        if (isAll) {
            listSongSelect.clear();
            for (int i = 0; i < listSong.size(); i++) {
                MusicItem item = listSong.get(i);
                if (!item.isDefaultSelected()) {
                    item.isSelected = true;
                    listSong.get(i).setSelected(true);
                    listSongSelect.add(item);
                }
            }
        } else {
            listSongSelect.clear();
            for (int i = 0; i < listSong.size(); i++) {
                MusicItem item = listSong.get(i);
                if (!item.isDefaultSelected()) {
                    listSong.get(i).setSelected(false);
                }
            }
        }
        notifyDataSetChanged();
    }

    @SuppressLint("NonConstantResourceId")
    public class SelectSongViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imgThumb)
        ShapeableImageView imgThumb;

        @BindView(R.id.tvTitle)
        TextView tvTitle;

        @BindView(R.id.tv_duration)
        TextView tv_duration;

        @BindView(R.id.ckbSelectSong)
        CheckBox ckbSelectSong;

        @BindView(R.id.parent)
        ConstraintLayout parentLayout;

        public SelectSongViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(MusicItem song, int position) {
            if (position == listSong.size() - 1) {
                parentLayout.setPadding(0, 0, 0, 170);
            } else {
                parentLayout.setPadding(0, 0, 0, 0);
            }
            if (song.isDefaultSelected()) {
                ckbSelectSong.setChecked(true);
                itemView.setEnabled(false);
                tvTitle.setTextColor(context.getResources().getColor(R.color.white));
                tv_duration.setTextColor(context.getResources().getColor(R.color.white));
                itemView.setAlpha(0.5f);
            } else {
                tvTitle.setTextColor(context.getResources().getColor(R.color.white));
                tv_duration.setTextColor(context.getResources().getColor(R.color.white));
                itemView.setAlpha(1f);
                itemView.setEnabled(true);
                ckbSelectSong.setChecked(song.isSelected);
            }

            ThumbnailMusic thumbnailStore = dbThumbnail.thumbDao().findThumbnailByID(song.id);
            if (thumbnailStore != null) {
                Glide.with(context)
                        .load(thumbnailStore.getThumbPath())
                        .placeholder(R.drawable.ic_song_transparent)
                        .into(imgThumb);
            } else {
                Glide.with(context)
                        .load(ArtworkUtils.INSTANCE.getArtworkFromSongID(song.id))
                        .placeholder(R.drawable.ic_song_transparent)
                        .into(imgThumb);
            }

            tvTitle.setText(song.title);
            String about = "";
            if (song.duration != null) {
                about = AppUtils.INSTANCE.convertDuration(Integer.parseInt(song.duration)) + " - " + song.artist;
            } else {
                about = song.artist;
            }
            tv_duration.setText(about);

            itemView.setOnClickListener(v -> {
                ckbSelectSong.setChecked(!ckbSelectSong.isChecked());
                if (ckbSelectSong.isChecked()) {
                    song.isSelected = true;
                    listSongSelect.add(song);
                    listSong.get(position).setSelected(true);
                } else {
                    listSongSelect.remove(song);
                    listSong.get(position).setSelected(false);
                }
                Log.e("Size", listSongSelect.size() + " - " + maxCount);
                if (mListener != null) {
                    mListener.onSelectAllSong(listSongSelect.size() == maxCount);
                }
            });
        }
    }

    public interface OnClickSelectSongListener {
        void onSelectAllSong(boolean isSelectAll);
    }
}
