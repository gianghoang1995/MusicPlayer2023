package com.musicplayer.mp3player.defaultmusicplayer.ui.fragment.main.query_folder.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.musicplayer.mp3player.defaultmusicplayer.equalizer.R;
import com.musicplayer.mp3player.defaultmusicplayer.model.FolderItem;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AdapterUnblockFolder extends RecyclerView.Adapter<AdapterUnblockFolder.BlockFolderViewHolder> {

    private Context mContext;
    private OnBlockFolderChangeSize listener;
    private ArrayList<FolderItem> listFolder = new ArrayList<>();
    private ArrayList<FolderItem> listSelect = new ArrayList<>();
    private ArrayList<String> listPathSelect = new ArrayList<>();

    public AdapterUnblockFolder(Context context, OnBlockFolderChangeSize listener) {
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
        FolderItem folder = listFolder.get(position);
        holder.bind(folder);
    }

    @Override
    public int getItemCount() {
        return listFolder.size();
    }

    public void setData(ArrayList<FolderItem> list) {
        listFolder.clear();
        listFolder.addAll(list);
        notifyDataSetChanged();
    }

    public ArrayList<FolderItem> getListDelete() {
        return listSelect;
    }

    public class BlockFolderViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvFolder)
        TextView tvFolder;

        @BindView(R.id.ckbUnlock)
        CheckBox ckbUnlock;

        public BlockFolderViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(FolderItem folder) {
            tvFolder.setText(folder.getName());
            ckbUnlock.setChecked(listPathSelect.contains(folder.getPath()));
            itemView.setOnClickListener(v -> {
                boolean isChecked = !ckbUnlock.isChecked();
                ckbUnlock.setChecked(isChecked);
                if (isChecked) {
                    listSelect.add(folder);
                    listPathSelect.add(folder.getPath());
                } else {
                    listSelect.remove(folder);
                    listPathSelect.remove(folder.getPath());
                }
                listener.onEnableButtonUnBlock(listSelect.size() > 0);
            });
        }
    }

    public interface OnBlockFolderChangeSize {
        void onEnableButtonUnBlock(boolean isEnable);
    }
}
