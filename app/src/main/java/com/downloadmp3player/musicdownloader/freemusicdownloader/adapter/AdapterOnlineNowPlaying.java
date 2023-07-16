package com.downloadmp3player.musicdownloader.freemusicdownloader.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.downloadmp3player.musicdownloader.freemusicdownloader.R;
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.ItemMusicOnline;
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppConstants;
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.AppUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

public class AdapterOnlineNowPlaying extends RecyclerView.Adapter<AdapterOnlineNowPlaying.ItemOnlineViewHolder> {
    private Context context;
    private OnItemNowPlayingOnlineClick onlineClickListener;
    private ArrayList<ItemMusicOnline> listOnline = new ArrayList<>();

    public AdapterOnlineNowPlaying(Context context, OnItemNowPlayingOnlineClick onlineClick) {
        this.onlineClickListener = onlineClick;
        this.context = context;
    }

    @Override
    public ItemOnlineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemOnlineViewHolder(LayoutInflater.from(context).inflate(R.layout.item_now_playing, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemOnlineViewHolder holder, int position) {
        ItemMusicOnline videoFromSearch = listOnline.get(position);
        holder.bind(videoFromSearch);

        holder.itemView.setOnClickListener(v -> {
            if (onlineClickListener != null) {
                onlineClickListener.onClickItemNowPlayingOnline(videoFromSearch);
            }
        });
    }

    public void setListOnline(ArrayList<ItemMusicOnline> lst) {
        listOnline.clear();
        listOnline.addAll(lst);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return listOnline.size();
    }

    public class ItemOnlineViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView tvName;
        TextView tv_duration;

        public ItemOnlineViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tv_duration = (TextView) itemView.findViewById(R.id.tv_duration);
            imgThumb = (ImageView) itemView.findViewById(R.id.img_thumb);
        }

        void bind(ItemMusicOnline item) {
            tvName.setText(item.title);
            tvName.setSelected(true);
            tv_duration.setSelected(true);
            String duration = !TextUtils.isEmpty(String.valueOf(item.duration)) ? AppUtils.INSTANCE.convertDuration(item.duration) : context.getString(R.string.unknow);
//            String artist = item.channelTitle != null ? item.channelTitle : context.getString(R.string.unknow);
            tv_duration.setText(duration);

            Glide.with(context)
                    .load(AppConstants.INSTANCE.randomThumb())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontTransform()
                    .placeholder(R.drawable.ic_song)
                    .into(imgThumb);
        }
    }

    public interface OnItemNowPlayingOnlineClick {
        void onClickItemNowPlayingOnline(ItemMusicOnline item);
    }
}

