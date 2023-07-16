package com.downloadmp3player.musicdownloader.freemusicdownloader.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.downloadmp3player.musicdownloader.freemusicdownloader.R;
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.FolderItem;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AdapterQuickFolder extends RecyclerView.Adapter<AdapterQuickFolder.QuickFolderViewHolder> {
    private Context mContext;
    private OnQuickFolderClick listener;
    private ArrayList<FolderItem> lstQuickFolder = new ArrayList<>();


    public AdapterQuickFolder(Context context, OnQuickFolderClick listener) {
        mContext = context;
        this.listener = listener;
    }

    public void addData(FolderItem folder) {
        lstQuickFolder.add(folder);
        notifyDataSetChanged();
    }

    public void addListData( ArrayList<FolderItem>  listFolder) {
        lstQuickFolder.clear();
        lstQuickFolder.addAll(listFolder);
        notifyDataSetChanged();
    }

    public void clearData() {
        lstQuickFolder.clear();
        notifyDataSetChanged();
    }

    public void addFolderPin(FolderItem folder) {
        lstQuickFolder.clear();
        String currentString = folder.getPath();
        String[] separated = currentString.split("/");
        StringBuilder path = new StringBuilder();
        for (int i = 0; i < separated.length; i++) {
            if (!TextUtils.isEmpty(separated[i])) {
                path.append(separated[i]).append("/");
                File file = new File(path.toString());
                if (file.exists() && file.canRead()) {
                    lstQuickFolder.add(new FolderItem(separated[i], 0, path.toString(), 0));
                }
            }
        }
        notifyDataSetChanged();
    }

    public void removeQuickItem(int position) {
        ArrayList<FolderItem> lst = new ArrayList<>();
        for (int i = 0; i < lstQuickFolder.size(); i++) {
            if (i <= position) {
                lst.add(lstQuickFolder.get(i));
            }
        }
        lstQuickFolder.clear();
        lstQuickFolder.addAll(lst);
        notifyDataSetChanged();
    }

    public ArrayList<FolderItem> removeLastItem() {
        lstQuickFolder.remove(lstQuickFolder.size() - 1);
        notifyDataSetChanged();
        return lstQuickFolder;
    }

    @NonNull
    @Override
    public QuickFolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.item_quick_folder, parent, false);
        return new QuickFolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuickFolderViewHolder holder, int position) {
        holder.bind(position, lstQuickFolder.get(position));
    }

    @Override
    public int getItemCount() {
        return lstQuickFolder.size();
    }

    public class QuickFolderViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvQuickItem)
        TextView tvQuickItem;

        public QuickFolderViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(int position, FolderItem folder) {
            if (position == getItemCount() - 1) {
                tvQuickItem.setTextColor(Color.WHITE);
            } else {
                tvQuickItem.setTextColor(Color.GRAY);
            }
            tvQuickItem.setText(folder.getName());
            tvQuickItem.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClickQuickFolder(position, folder);
                }
            });
        }
    }

    public interface OnQuickFolderClick {
        void onClickQuickFolder(int position, FolderItem folder);
    }
}
