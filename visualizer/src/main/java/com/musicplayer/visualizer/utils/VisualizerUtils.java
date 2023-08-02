package com.musicplayer.visualizer.utils;

import android.media.AudioManager;
import android.media.MediaPlayer;

import com.musicplayer.visualizer.view.VisualizerFullView;
import com.musicplayer.visualizer.view.VisualizerManager;
import com.musicplayer.visualizer.view.VisualizerMiniView;

public class VisualizerUtils {
    private static VisualizerUtils mVisualizerUtil = new VisualizerUtils();

    public static VisualizerUtils getInstance() {
        return mVisualizerUtil;
    }

    public void initVisualizerFullView(VisualizerFullView visualizerFullView, MediaPlayer player, AudioManager audio) {
        VisualizerManager.getInstance().release();
        VisualizerManager.getInstance().releaseSession();
        VisualizerManager.getInstance().setupView(visualizerFullView);
        VisualizerManager.getInstance().setupSession(player.getAudioSessionId());
        VisualizerManager.getInstance().setAudioManager(audio, player.getAudioSessionId());
        if (visualizerFullView != null) {
            visualizerFullView.refreshChanged();
        }
    }

    public void initVisualizerFullView(VisualizerFullView visualizerFullView, int audioSeasion) {
        VisualizerManager.getInstance().release();
        VisualizerManager.getInstance().releaseSession();
        VisualizerManager.getInstance().setupView(visualizerFullView);
        VisualizerManager.getInstance().setupSession(audioSeasion);
        if (visualizerFullView != null) {
            visualizerFullView.refreshChanged();
        }
    }

    public void initVisualizerFullView(VisualizerFullView visualizerFullView, int audioSeasion, AudioManager audio) {
        VisualizerManager.getInstance().release();
        VisualizerManager.getInstance().releaseSession();
        VisualizerManager.getInstance().setupView(visualizerFullView);
        VisualizerManager.getInstance().setupSession(audioSeasion);
        VisualizerManager.getInstance().setAudioManager(audio, audioSeasion);
        if (visualizerFullView != null) {
            visualizerFullView.refreshChanged();
        }
    }

    public void initVisualizerSmallView(VisualizerMiniView visualizerMiniView, MediaPlayer player, AudioManager audio) {
        VisualizerManager.getInstance().release();
        VisualizerManager.getInstance().releaseSession();
        VisualizerManager.getInstance().setupView(visualizerMiniView);
        VisualizerManager.getInstance().setupSession(player.getAudioSessionId());
        VisualizerManager.getInstance().setAudioManager(audio, player.getAudioSessionId());
        if (visualizerMiniView != null) {
            visualizerMiniView.refreshChanged();
        }
    }
}
