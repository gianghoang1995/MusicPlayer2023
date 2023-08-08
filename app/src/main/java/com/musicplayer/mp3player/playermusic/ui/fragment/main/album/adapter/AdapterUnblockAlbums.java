package com.musicplayer.mp3player.playermusic.ui.fragment.main.album.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.musicplayer.mp3player.playermusic.equalizer.R;
import com.musicplayer.mp3player.playermusic.model.AlbumItem;
import com.musicplayer.mp3player.playermusic.utils.ArtworkUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AdapterUnblockAlbums extends RecyclerView.Adapter<AdapterUnblockAlbums.BlockFolderViewHolder> {

    private Context mContext;
    private OnBlockAlbumChangeSize listener;
    private ArrayList<AlbumItem> listAlbum = new ArrayList<>();
    private ArrayList<AlbumItem> listSelect = new ArrayList<>();

    public AdapterUnblockAlbums(Context context, OnBlockAlbumChangeSize listener) {
        mContext = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BlockFolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.item_holder_unblock_folder, parent, false);
        return new BlockFolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlockFolderViewHolder holder, int position) {
        AlbumItem folder = listAlbum.get(position);
        holder.bind(folder);
    }

    @Override
    public int getItemCount() {
        return listAlbum.size();
    }

    public void setData(ArrayList<AlbumItem> list) {
        listAlbum.clear();
        listAlbum.addAll(list);
        notifyDataSetChanged();
    }

    public ArrayList<AlbumItem> getListDelete() {
        return listSelect;
    }

    public class BlockFolderViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvFolder)
        TextView tvFolder;

        @BindView(R.id.ckbUnlock)
        CheckBox ckbUnlock;

        @BindView(R.id.imgThumb)
        ImageView imgThumb;

        public BlockFolderViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(AlbumItem album) {
            tvFolder.setText(album.getAlbumName());
            ckbUnlock.setChecked(listSelect.contains(album));
            itemView.setOnClickListener(v -> {
                boolean isChecked = !ckbUnlock.isChecked();
                ckbUnlock.setChecked(isChecked);
                if (isChecked) {
                    listSelect.add(album);
                } else {
                    listSelect.remove(album);
                }
                listener.onEnableButtonUnBlock(listSelect.size() > 0);
            });
            Glide.with(mContext)
                    .load(ArtworkUtils.INSTANCE.uri(album.getId()))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_albums)
                    .into(imgThumb);
        }
    }

    public interface OnBlockAlbumChangeSize {
        void onEnableButtonUnBlock(boolean isEnable);
    }
}
