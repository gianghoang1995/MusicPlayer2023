package com.musicplayer.mp3player.defaultmusicplayer.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.musicplayer.mp3player.defaultmusicplayer.equalizer.R;
import com.musicplayer.mp3player.defaultmusicplayer.model.FolderItem;
import com.musicplayer.mp3player.defaultmusicplayer.model.MusicItem;
import com.musicplayer.mp3player.defaultmusicplayer.ui.fragment.main.query_folder.db.pin.PinFolderDao;
import com.musicplayer.mp3player.defaultmusicplayer.ui.fragment.main.query_folder.db.pin.PinFolderHelper;
import com.musicplayer.mp3player.defaultmusicplayer.utils.ArtworkUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterQueryFolder extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_FOLDER = 0;
    private static final int TYPE_MUSIC = 1;
    private static final int TYPE_EMPTY = 2;
    private final Context context;
    private final OnQueryFolderClickListener listener;
    private final ArrayList<Object> lstData = new ArrayList<>();
    public boolean showMoreFolder = false;
    private final PinFolderDao mPinFolderDao;

    public AdapterQueryFolder(Context context, OnQueryFolderClickListener listener) {
        this.context = context;
        this.listener = listener;
        PinFolderHelper pinFolderHelper = new PinFolderHelper(context);
        mPinFolderDao = new PinFolderDao(pinFolderHelper);
    }

    @Override
    public int getItemViewType(int position) {
        if (lstData.get(position) instanceof FolderItem) {
            return TYPE_FOLDER;
        } else if (lstData.get(position) instanceof MusicItem) {
            return TYPE_MUSIC;
        } else {
            return TYPE_EMPTY;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        if (viewType == TYPE_FOLDER) {
            View view = layoutInflater.inflate(R.layout.item_query_folder, parent, false);
            return new QueryFolderViewHolder(view);
        } else if (viewType == TYPE_MUSIC) {
            View view = layoutInflater.inflate(R.layout.item_music_folder, parent, false);
            return new MusicFolderViewHolder(view);
        } else  {
            View view = layoutInflater.inflate(R.layout.layout_empty_file, parent, false);
            return new EmptyDataViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_FOLDER) {
            FolderItem item = (FolderItem) lstData.get(position);
            QueryFolderViewHolder folderViewHolder = (QueryFolderViewHolder) holder;
            folderViewHolder.bind(item);
            folderViewHolder.itemView.setOnClickListener(v -> listener.onClickSubFolder(item));
            folderViewHolder.btnMore.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClickMoreSubFolder(item, position, folderViewHolder.btnMore);
                }
            });
        } else if (getItemViewType(position) == TYPE_MUSIC) {
            MusicFolderViewHolder musicFolderViewHolder = (MusicFolderViewHolder) holder;
            MusicItem item = (MusicItem) lstData.get(position);
            musicFolderViewHolder.bind(item);
        }
    }

    @Override
    public int getItemCount() {
        return lstData.size();
    }

    public void addData(ArrayList<Object> lst) {
        Log.e("lst", lst.size() + "");
        lstData.clear();
        if (lst.size() > 0) {
            lstData.addAll(lst);
        } else {
            lstData.add(0);
        }
        notifyDataSetChanged();
    }

    public void updatePinFolderStatus() {
        notifyDataSetChanged();
    }

    public void removeItem(int index) {
        lstData.remove(index);
        notifyItemRemoved(index);
        notifyItemRangeChanged(index, lstData.size());
    }

    public void setShowMoreFolder(boolean isShow) {
        showMoreFolder = isShow;
    }

    @SuppressLint("NonConstantResourceId")
    public class QueryFolderViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imgFolder)
        ImageView imgFolder;

        @BindView(R.id.tvFolder)
        TextView tvFolder;

        @BindView(R.id.btnMore)
        ImageButton btnMore;

        public QueryFolderViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(FolderItem folder) {
            if (showMoreFolder) {
                btnMore.setVisibility(View.VISIBLE);
            } else {
                btnMore.setVisibility(View.GONE);
            }

            if (mPinFolderDao.isPinFolder(folder)) {
                imgFolder.setImageResource(R.drawable.ic_folder_pin);
                btnMore.setVisibility(View.VISIBLE);
            } else {
                imgFolder.setImageResource(R.drawable.ic_folder_query);
            }
            tvFolder.setText(folder.getName());
        }
    }

    public class EmptyDataViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvEmpty)
        TextView tvEmpty;

        public EmptyDataViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    @SuppressLint("NonConstantResourceId")
    public class MusicFolderViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imgThumb)
        CircleImageView imgThumb;

        @BindView(R.id.tvTitle)
        TextView tvTitle;

        @BindView(R.id.btnMore)
        ImageButton btnMore;

        public MusicFolderViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(MusicItem song) {
            tvTitle.setText(song.title);
            Glide.with(context)
                    .load(ArtworkUtils.INSTANCE.getArtworkFromSongID(song.id))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_song)
                    .into(imgThumb);

            btnMore.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClickMoreSong(song, btnMore);
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null)
                    listener.onClickSong(song);
            });
        }
    }

    public interface OnQueryFolderClickListener {
        void onClickSubFolder(FolderItem folder);

        void onClickMoreSubFolder(FolderItem folder, int position, View view);

        void onClickMoreSong(MusicItem song, View view);

        void onClickSong(MusicItem song);
    }
}
