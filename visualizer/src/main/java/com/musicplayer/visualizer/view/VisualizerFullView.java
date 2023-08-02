package com.musicplayer.visualizer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.View;

import androidx.core.view.ViewCompat;

public class VisualizerFullView extends View implements Callback, IVisualizerView {
    private static final String TAG = "VisualizerFullView";
    private final boolean USE_SURFACEVIEW = false;
    private Context mContext = null;
    private int mCountdownToStop = 0;
    private int mDensity = 1;
    /* access modifiers changed from: private */
    public SurfaceHolder mHolder;
    private boolean mUpdate = true;
    /* access modifiers changed from: private */
    public ITypeView mViewType;
    private ViewThread thread = null;

    class ViewThread extends Thread {
        private boolean mRun = false;

        public ViewThread(SurfaceHolder holder, Context context) {
            VisualizerFullView.this.mHolder = holder;
        }

        public void run() {
            Log.d(VisualizerFullView.TAG, "run(" + this.mRun + ")");
            while (this.mRun) {
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                }
                Canvas c = null;
                try {
                    c = VisualizerFullView.this.mHolder.lockCanvas(null);
                    synchronized (VisualizerFullView.this.mHolder) {
                        doDraw(c);
                    }
                    if (c != null) {
                        VisualizerFullView.this.mHolder.unlockCanvasAndPost(c);
                    }
                } catch (Exception ex) {
                    try {
                        Log.e(VisualizerFullView.TAG, "Exception!!:" + ex);
                    } finally {
                        if (c != null) {
                            VisualizerFullView.this.mHolder.unlockCanvasAndPost(c);
                        }
                    }
                }
            }
        }

        public void setRunning(boolean b) {
            this.mRun = b;
        }

        private void doDraw(Canvas canvas) {
            if (this.mRun) {
                canvas.drawColor(ViewCompat.MEASURED_STATE_MASK);
                VisualizerFullView.this.mViewType.draw(canvas);
            }
        }
    }

    public VisualizerFullView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.mDensity = (int) this.mContext.getResources().getDisplayMetrics().density;
        this.mViewType = new TypeGraphBar(context, 0);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated()");
        this.mHolder = holder;
        startViewThread();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed()");
        stopViewThread();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged(" + width + ", " + height + ")");
        this.mHolder = holder;
    }

    private void startViewThread() {
        if (this.thread == null) {
            Log.d(TAG, "startThread()");
            this.thread = new ViewThread(this.mHolder, this.mContext);
            this.thread.setName("SoundFlip ViewThread");
            this.thread.setRunning(true);
            this.thread.start();
        }
    }

    private void stopViewThread() {
        if (this.thread != null) {
            Log.d(TAG, "stopThread()");
            Log.d(TAG, "=>time check1");
            this.thread.setRunning(false);
            this.thread.interrupt();
            boolean retry = true;
            while (retry) {
                try {
                    this.thread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    Log.e(TAG, "InterruptedException");
                }
            }
            this.thread.interrupt();
            this.thread = null;
            Log.d(TAG, "=>time check2");
        }
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
