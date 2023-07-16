package com.downloadmp3player.musicdownloader.freemusicdownloader.ui.fragment.main.artist.adapter;

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
import com.downloadmp3player.musicdownloader.freemusicdownloader.R;
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.ArtistItem;
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.ArtworkUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AdapterUnblockArtist extends RecyclerView.Adapter<AdapterUnblockArtist.BlockFolderViewHolder> {

    private Context mContext;
    private OnBlockArtistChangeSize listener;
    private ArrayList<ArtistItem> listArtist = new ArrayList<>();
    private ArrayList<ArtistItem> listSelect = new ArrayList<>();

    public AdapterUnblockArtist(Context context, OnBlockArtistChangeSize listener) {
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
        ArtistItem folder = listArtist.get(position);
        holder.bind(folder);
    }

    @Override
    public int getItemCount() {
        return listArtist.size();
    }

    public void setData(ArrayList<ArtistItem> list) {
        listArtist.clear();
        listArtist.addAll(list);
        notifyDataSetChanged();
    }

    public ArrayList<ArtistItem> getListDelete() {
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

        public void bind(ArtistItem artist) {
            tvFolder.setText(artist.getName());
            ckbUnlock.setChecked(listSelect.contains(artist));
            itemView.setOnClickListener(v -> {
                boolean isChecked = !ckbUnlock.isChecked();
                ckbUnlock.setChecked(isChecked);
                if (isChecked) {
                    listSelect.add(artist);
                } else {
                    listSelect.remove(artist);
                }
                listener.onEnableButtonUnBlock(listSelect.size() > 0);
            });
            Glide.with(mContext)
                    .load(ArtworkUtils.INSTANCE.uri(artist.getId()))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_artists)
                    .into(imgThumb);
        }
    }

    public interface OnBlockArtistChangeSize {
        void onEnableButtonUnBlock(boolean isEnable);
    }
}
