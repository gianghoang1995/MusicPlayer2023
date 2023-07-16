package com.downloadmp3player.visualizer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class VisualizerMiniView extends View implements IVisualizerView {
    private static final String TAG = "MiniEQView";
    private Context mContext = null;
    private int mCountdownToStop = 0;
    private int mDensity = 1;
    private boolean mUpdate = true;
    private ITypeView mViewType;

    public VisualizerMiniView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.mViewType = new TypeGraphBar(context, 1);
        this.mDensity = (int) this.mContext.getResources().getDisplayMetrics().density;
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(TAG, "onSizeChanged(" + w + ", " + h + ")");
        this.mViewType.onSizeChanged(w, h, this.mDensity);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void refreshChanged() {
        this.mViewType.refreshChanged();
    }

    public void update(byte[] bytes) {
        this.mViewType.update(bytes);
        if (bytes != null) {
            this.mUpdate = true;
        } else if (this.mUpdate && this.mCountdownToStop == 0) {
            this.mCountdownToStop = 70;
        }
        if (this.mUpdate) {
            invalidate();
        }
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.mViewType.draw(canvas);
        if (this.mCountdownToStop > 0) {
            int i = this.mCountdownToStop - 1;
            this.mCountdownToStop = i;
            if (i == 0) {
                this.mUpdate = false;
            }
        }
        if (this.mUpdate) {
            invalidate();
        }
    }

    public float plusRatio() {
        return this.mViewType.plusRatio();
    }

    public void setAlpha(int value) {
        this.mViewType.setAlpha(value);
        if (!this.mUpdate) {
            this.mUpdate = true;
            this.mCountdownToStop = 5;
        }
    }

    public void setColorSet(int value) {
        this.mViewType.setColorSet(value);
        if (!this.mUpdate) {
            this.mUpdate = true;
            this.mCountdownToStop = 5;
        }
    }

    public void setBarSize(int value) {
        this.mViewType.setBarSize(value);
        if (!this.mUpdate) {
            this.mUpdate = true;
            this.mCountdownToStop = 5;
        }
    }

    public void setStick(boolean show) {
        this.mViewType.setStick(show);
        if (!this.mUpdate) {
            this.mUpdate = true;
            this.mCountdownToStop = 5;
        }
    }

    public void setUseMic(boolean use) {
        this.mViewType.setUseMic(use);
        if (!this.mUpdate) {
            this.mUpdate = true;
            this.mCountdownToStop = 5;
        }
    }

    public void refresh() {
        if (!this.mUpdate) {
            this.mUpdate = true;
            this.mCountdownToStop = 5;
        }
    }

    public void setMICSensitivity(int value) {
        this.mViewType.setMICSensitivity(value);
        if (!this.mUpdate) {
            this.mUpdate = true;
            this.mCountdownToStop = 5;
        }
    }

    public int getCustomColorSet() {
        return this.mViewType.getCustomColorSet();
    }
}
