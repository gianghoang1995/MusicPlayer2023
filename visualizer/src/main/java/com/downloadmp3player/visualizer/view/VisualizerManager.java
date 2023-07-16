package com.downloadmp3player.visualizer.view;

import android.media.AudioManager;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.media.audiofx.Visualizer.OnDataCaptureListener;
import android.util.Log;


public class VisualizerManager {
    public static final String PACKAGE_NAME = "noh.jinil.app.anytime";
    private static final String TAG = "VisualizerManager";
    private static VisualizerManager mVisualizerUtil = new VisualizerManager();
    boolean CANCELLED_FLAG = false;
    private boolean bLocal = false;
    int blockSize = 256;
    private AudioManager mAudioManager;
    byte[] mBytes;
    private Equalizer mEqualizer;
    private int mSessionID = 0;
    IVisualizerView mView;
    private Visualizer mVisualizer;
    /* access modifiers changed from: private */

    public static VisualizerManager getInstance() {
        return mVisualizerUtil;
    }

    public void setupView(IVisualizerView view) {
        Log.d(TAG, "setupView()");
        this.mView = view;
    }

    public void setAudioManager(AudioManager am, int sessionid) {
        this.mAudioManager = am;
        this.mSessionID = sessionid;
    }

    public boolean toggleSessionID() {
        Log.d(TAG, "toggleSessionID()");
        if (this.bLocal) {
            this.bLocal = false;
            releaseSession();
            setupSession(0);
        } else {
            this.bLocal = true;
            releaseSession();
            setupSession(this.mSessionID);
        }
        return this.bLocal;
    }

    public boolean isMusicActive() {
        if (this.mAudioManager == null || !this.mAudioManager.isMusicActive()) {
            return false;
        }
        return true;
    }

    public boolean isVisualizerActive() {
        if (this.mAudioManager != null && this.mAudioManager.getStreamVolume(3) == 0) {
            return true;
        }
        if (this.mBytes == null) {
            return false;
        }
        for (byte b : this.mBytes) {
            if (b != 0) {
                return true;
            }
        }
        return false;
    }

    public void setupSession() {
        this.bLocal = false;
        releaseSession();
        setupSession(0);
    }

    public void setupSession(int sessionId) {
        if (this.mVisualizer == null) {
            try {
                this.mVisualizer = new Visualizer(sessionId);
                this.mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
                this.mVisualizer.setDataCaptureListener(new OnDataCaptureListener() {
                    public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                    }

                    public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                        if (!VisualizerManager.this.isMusicActive()) {
                            bytes = null;
                        }
                        if (VisualizerManager.this.mView != null) {
                            VisualizerManager.this.mView.update(bytes);
                        }
                        VisualizerManager.this.mBytes = bytes;
                    }
                }, Visualizer.getMaxCaptureRate() / 2, false, true);
                Log.d(TAG, "=>CaptureSizeRangeMin:" + Visualizer.getCaptureSizeRange()[0]);
                Log.d(TAG, "=>CaptureSizeRangeMax:" + Visualizer.getCaptureSizeRange()[1]);
                Log.d(TAG, "=>MaxCaptureRate:" + Visualizer.getMaxCaptureRate());
                this.mVisualizer.setEnabled(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void releaseSession() {
        Log.d(TAG, "release()");
        if (this.mVisualizer != null) {
            this.mVisualizer.setEnabled(false);
            this.mVisualizer.release();
            this.mVisualizer = null;
        }
        if (this.mEqualizer != null) {
            Log.d(TAG, "=>equalizer for visualizer is released!!");
            this.mEqualizer.release();
            this.mEqualizer = null;
        }
    }

    public void release() {
        releaseSession();
    }

//    private void turnoffLPASystemProperty() {
//        try {
//            Object obj = Class.forName("android.os.SystemProperties").getConstructor(new Class[0]).newInstance(new Object[0]);
//            Method get = obj.getClass().getMethod(MediaPlaybackService.CMDGET, new Class[]{String.class});
//            Method set = obj.getClass().getMethod(MediaPlaybackService.CMDSET, new Class[]{String.class, String.class});
//            boolean disable = Boolean.valueOf((String) get.invoke(obj, new Object[]{"audio.offload.disable"})).booleanValue();
//            Log.w(TAG, "disable!!:" + disable);
//            if (!disable) {
//                Log.w(TAG, "turnoffOffloadSystemProperty()");
//                set.invoke(obj, new Object[]{"audio.offload.disable", "true"});
//            }
//            if (Boolean.valueOf((String) get.invoke(obj, new Object[]{"lpa.decode"})).booleanValue()) {
//                Log.w(TAG, "turnoffLPASystemProperty()");
//                set.invoke(obj, new Object[]{"lpa.decode", "false"});
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
