package com.musicplayer.mp3player.playermusic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.musicplayer.mp3player.playermusic.equalizer.R;
import com.musicplayer.mp3player.playermusic.model.CustomPresetItem;

import java.util.ArrayList;

public class SpinnerAdapterRV extends BaseAdapter {
    Context context;
    ArrayList<CustomPresetItem> item;
    LayoutInflater inflter;

    public SpinnerAdapterRV(Context applicationContext, ArrayList<CustomPresetItem> mitem) {
        this.context = applicationContext;
        this.item = mitem;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return item.size();
    }

    @Override
    public Object getItem(int i) {
        return item.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.custom_spinner_items, null);
        TextView names =view.findViewById(R.id.textView);
        names.setText(item.get(i).presetName);
        return view;
    }
}
