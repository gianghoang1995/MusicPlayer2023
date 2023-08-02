package com.musicplayer.visualizer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;

import androidx.core.internal.view.SupportMenu;

import com.musicplayer.visualizer.utils.PrefVisualizerFullView;

public class TypeGraphBar implements ITypeView {
    private final int CUSTOM_COLORSET = 8;
    private final int GRAPHBAR_BASE = 5;
    private final int GRAPHBAR_GAP = 1;
    private final int GRAPHBAR_SHOW_MAX = 40;
    private int GRAPH_TUNING_END = 600;
    private int GRAPH_TUNING_GAP = ((this.GRAPH_TUNING_END - this.GRAPH_TUNING_START) / 45);
    private final int GRAPH_TUNING_MAX = 250;
    private final int GRAPH_TUNING_MIN = 1;
    private int GRAPH_TUNING_START = 80;
    /* access modifiers changed from: private */
    public int mAlphaValue = 255;
    /* access modifiers changed from: private */
    public final int[] mBlueColors = {Color.argb(255, 80, 190, 230), Color.argb(255, 50, 180, 227), Color.argb(255, 44, 177, 222), Color.argb(255, 35, 173, 219), Color.argb(255, 28, 170, 217), Color.argb(255, 21, 164, 212), Color.argb(255, 15, 160, 209), Color.argb(255, 6, 156, 207), Color.argb(255, 0, 151, 201), Color.argb(255, 6, 156, 207), Color.argb(255, 15, 160, 209), Color.argb(255, 21, 164, 212), Color.argb(255, 28, 170, 217), Color.argb(255, 35, 173, 219), Color.argb(255, 44, 177, 222), Color.argb(255, 50, 180, 227), Color.argb(255, 80, 190, 230)};
    /* access modifiers changed from: private */
    public int mColorValue = 0;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public int mGRAPHBAR_HEIGHT = 0;
    /* access modifiers changed from: private */
    public int mGRAPHBAR_SHOW_COUNT = 0;
    /* access modifiers changed from: private */
    public int mGRAPHBAR_WIDTH = 0;
    private int mGRAPH_HEIGHT = 0;
    private int mGRAPH_WIDTH = 0;
    /* access modifiers changed from: private */
    public int mGRAPH_X = 0;
    /* access modifiers changed from: private */
    public int mGRAPH_Y = 0;
    private GraphBar[] mGraphBar;
    /* access modifiers changed from: private */
    public final int[] mGreenColorSet = {Color.argb(255, 208, 230, 145), Color.argb(255, 197, 224, 108), Color.argb(255, 181, 217, 74), Color.argb(255, 169, 209, 36), Color.argb(255, 151, 201, 0), Color.argb(255, 145, 194, 0), Color.argb(255, 137, 186, 0), Color.argb(255, 131, 179, 0), Color.argb(255, 124, 173, 0), Color.argb(255, 116, 166, 0), Color.argb(255, 108, 158, 0), Color.argb(255, 100, 150, 0), Color.argb(255, 108, 158, 0), Color.argb(255, 116, 166, 0), Color.argb(255, 124, 173, 0), Color.argb(255, 131, 179, 0), Color.argb(255, 137, 186, 0), Color.argb(255, 145, 194, 0), Color.argb(255, 151, 201, 0), Color.argb(255, 169, 209, 36), Color.argb(255, 181, 217, 74), Color.argb(255, 197, 224, 108), Color.argb(255, 208, 230, 145)};
    private float mHeightRatio = 0.4f;
    private int mMICSensitivity = 70;
    /* access modifiers changed from: private */
    public int mMaxStep = 5;
    private Paint mPaintBarLine;
    /* access modifiers changed from: private */
    public final int[] mPurpleColorSet = {Color.argb(255, 217, 185, 235), Color.argb(255, 211, 172, 232), Color.argb(255, 206, 158, 230), Color.argb(255, 201, 150, 227), Color.argb(255, 196, 139, 224), Color.argb(255, 191, 129, 222), Color.argb(255, 183, 115, 217), Color.argb(255, 175, 103, 214), Color.argb(255, 169, 89, 212), Color.argb(255, 164, 79, 209), Color.argb(255, 157, 64, 207), Color.argb(255, 151, 50, 201), Color.argb(255, 157, 64, 207), Color.argb(255, 164, 79, 209), Color.argb(255, 169, 89, 212), Color.argb(255, 175, 103, 214), Color.argb(255, 183, 115, 217), Color.argb(255, 191, 129, 222), Color.argb(255, 196, 139, 224), Color.argb(255, 201, 150, 227), Color.argb(255, 206, 158, 230), Color.argb(255, 211, 172, 232), Color.argb(255, 217, 185, 235)};
    /* access modifiers changed from: private */
    public float mREFLECTION_RATIO = 0.25f;
    /* access modifiers changed from: private */
    public final int[] mRedColorSet = {Color.argb(255, 252, 174, 174), Color.argb(255, 252, 146, 146), Color.argb(255, 252, 121, 121), Color.argb(255, 252, 96, 96), Color.argb(255, 252, 68, 68), Color.argb(255, 245, 59, 59), Color.argb(255, 237, 50, 50), Color.argb(255, 230, 39, 39), Color.argb(255, 224, 29, 29), Color.argb(255, 217, 20, 20), Color.argb(255, 209, 10, 10), Color.argb(255, 201, 0, 0), Color.argb(255, 209, 10, 10), Color.argb(255, 217, 20, 20), Color.argb(255, 224, 29, 29), Color.argb(255, 230, 39, 39), Color.argb(255, 237, 50, 50), Color.argb(255, 245, 59, 59), Color.argb(255, 252, 68, 68), Color.argb(255, 252, 96, 96), Color.argb(255, 252, 121, 121), Color.argb(255, 252, 146, 146), Color.argb(255, 252, 174, 174)};
    private int mSCREEN_DENSITY = 0;
    /* access modifiers changed from: private */
    public int mSCREEN_HEIGHT = 0;
    /* access modifiers changed from: private */
    public int mSCREEN_WIDTH = 0;
    /* access modifiers changed from: private */
    public boolean mShowStick = false;
    /* access modifiers changed from: private */
//    public final int[] mSpectrumColors = {Color.argb(255, 250, 15, 15), Color.argb(255, 250, 34, 22), Color.argb(255, 247, 49, 27), Color.argb(255, 247, 65, 37), Color.argb(255, 247, 87, 42), Color.argb(255, 247, 107, 52), Color.argb(255, 247, TransportMediator.KEYCODE_MEDIA_RECORD, 47), Color.argb(255, 245, 151, 44), Color.argb(255, 247, 165, 42), Color.argb(255, 247, 188, 37), Color.argb(255, 245, 213, 32), Color.argb(255, 245, 220, 29), Color.argb(255, 242, 239, 24), Color.argb(255, 233, 240, 24), Color.argb(255, 188, 217, 28), Color.argb(255, 153, 204, 33), Color.argb(255, 135, 196, 37), Color.argb(255, 102, 181, 38), Color.argb(255, 73, 168, 44), Color.argb(255, 58, 161, 42), Color.argb(255, 19, 128, 62), Color.argb(255, 16, 102, 78), Color.argb(255, 18, 92, 92)};
    public final int[] mSpectrumColors = {
            Color.argb(255, 236, 64, 122),
            Color.argb(255, 188, 51, 97),
            Color.argb(255, 239, 83, 80),
//            Color.argb(255, 178, 61, 60),
            Color.argb(255, 255, 167, 38),
            Color.argb(255, 212, 225, 87),
            Color.argb(255, 66, 187, 106),
            Color.argb(255, 38, 198, 218),
            Color.argb(255, 36, 185, 205),
            Color.argb(255, 30, 136, 229),
            Color.argb(255, 126, 87, 194),
            Color.argb(255, 106, 73, 163),
            //Center
            Color.argb(255, 126, 87, 194),
            Color.argb(255, 30, 136, 229),
            Color.argb(255, 36, 185, 205),
            Color.argb(255, 38, 198, 218),
            Color.argb(255, 66, 187, 106),
            Color.argb(255, 212, 225, 87),
            Color.argb(255, 255, 167, 38),
//            Color.argb(255, 178, 61, 60),
            Color.argb(255, 239, 83, 80),
            Color.argb(255, 188, 51, 97),
            Color.argb(255, 236, 64, 122)};

//     public final int[] mSpectrumColors = {
//            Color.argb(255, 255, 31, 74),
//            Color.argb(255, 255, 35, 72),
//            Color.argb(255, 255, 40, 69),
//            Color.argb(255, 255, 40, 69),
//            Color.argb(255, 255, 40, 67),
//            Color.argb(255, 255, 50, 65),
//            Color.argb(255, 255, 55, 63),
//            Color.argb(255, 255, 57, 61),
//            Color.argb(255, 255, 64, 59),
//            Color.argb(255, 255, 69, 56),
//            Color.argb(255, 255, 74, 54),
//            Color.argb(255, 255, 77, 53),
//            //Center
//            Color.argb(255, 255, 77, 53),
//            Color.argb(255, 255, 74, 54),
//            Color.argb(255, 255, 69, 56),
//            Color.argb(255, 255, 64, 59),
//            Color.argb(255, 255, 57, 61),
//            Color.argb(255, 255, 55, 63),
//            Color.argb(255, 255, 55, 63),
//            Color.argb(255, 255, 50, 65),
//            Color.argb(255, 255, 40, 67),
//            Color.argb(255, 255, 40, 69),
//            Color.argb(255, 255, 40, 69),
//            Color.argb(255, 255, 35, 72),
//            Color.argb(255, 255, 31, 74)};
    private boolean mUseMic = false;
    /* access modifiers changed from: private */
    public int mViewSize = 0;
    private Woofer[] mWoofer;
    /* access modifiers changed from: private */
    public final int[] mYellowColorSet = {Color.argb(255, 252, 224, 159), Color.argb(255, 252, 215, TransportMediator.KEYCODE_MEDIA_PLAY), Color.argb(255, 252, 205, 96), Color.argb(255, 252, 196, 66), Color.argb(255, 252, 187, 33), Color.argb(255, 252, 178, 28), Color.argb(255, 252, 169, 25), Color.argb(255, 252, 163, 20), Color.argb(255, 252, 158, 15), Color.argb(255, 252, 151, 10), Color.argb(255, 252, 141, 5), Color.argb(255, 252, 135, 0), Color.argb(255, 252, 141, 5), Color.argb(255, 252, 151, 10), Color.argb(255, 252, 158, 15), Color.argb(255, 252, 163, 20), Color.argb(255, 252, 169, 25), Color.argb(255, 252, 178, 28), Color.argb(255, 252, 187, 33), Color.argb(255, 252, 196, 66), Color.argb(255, 252, 205, 96), Color.argb(255, 252, 215, TransportMediator.KEYCODE_MEDIA_PLAY), Color.argb(255, 252, 224, 159)};

    class GraphBar {
        private boolean bAvailable = true;
        private int currPos;
        private int gap;
        private int imgColorDown;
        private int imgColorUp;
        private int linePos;
        private int livePos;
        private int liveStep;
        private Rect mDstRect = new Rect();
        private Paint mOrPaint = new Paint();
        private Paint mRePaint = new Paint();
        private int myIndex;
        private int posX;
        private int posY;
        private int prevPos;
        private int stayCnt;
        private int stickH;
        private float velocity;
        private int width;

        GraphBar(int index) {
            this.myIndex = index;
            updatePosition();
            this.linePos = TypeGraphBar.this.mGRAPH_Y + TypeGraphBar.this.mGRAPHBAR_HEIGHT;
            updateColorSet(TypeGraphBar.this.mColorValue);
            updateAlpha(TypeGraphBar.this.mAlphaValue);
        }

        public void updateColorSet(int value) {
            if (value == 0) {
                this.imgColorUp = TypeGraphBar.this.mSpectrumColors[((this.myIndex * TypeGraphBar.this.mSpectrumColors.length) / TypeGraphBar.this.mGRAPHBAR_SHOW_COUNT) % TypeGraphBar.this.mSpectrumColors.length];
                this.imgColorDown = this.imgColorUp;
                this.mOrPaint.setColor(this.imgColorUp);
                this.mRePaint.setColor(this.imgColorUp);
                this.mOrPaint.setColorFilter(null);
                this.mRePaint.setColorFilter(null);
                this.mOrPaint.setShader(null);
                this.mRePaint.setShader(null);
            } else if (value == 7) {
                this.imgColorUp = TypeGraphBar.this.mSpectrumColors[((this.myIndex * TypeGraphBar.this.mSpectrumColors.length) / TypeGraphBar.this.mGRAPHBAR_SHOW_COUNT) % TypeGraphBar.this.mSpectrumColors.length];
                this.imgColorDown = this.imgColorUp;
                this.mOrPaint.setColor(this.imgColorUp);
                this.mRePaint.setColor(this.imgColorUp);
                this.mOrPaint.setShader(null);
                this.mRePaint.setShader(null);
                ColorMatrix cm = new ColorMatrix();
                cm.setSaturation(0.0f);
                ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
                this.mOrPaint.setColorFilter(f);
                this.mRePaint.setColorFilter(f);
            } else if (value == 8) {
                int[] colorset = new int[PrefVisualizerFullView.loadIntegerValue(TypeGraphBar.this.mContext, PrefVisualizerFullView.KEY_VI_COLORSET_NUM, 20)];
                for (int i = 0; i < colorset.length; i++) {
                    colorset[i] = PrefVisualizerFullView.loadIntegerValue(TypeGraphBar.this.mContext, PrefVisualizerFullView.KEY_VI_COLORSET + (i + 1));
                }
                this.imgColorUp = colorset[((this.myIndex * colorset.length) / TypeGraphBar.this.mGRAPHBAR_SHOW_COUNT) % colorset.length];
                for (int i2 = 0; i2 < colorset.length; i2++) {
                    colorset[i2] = PrefVisualizerFullView.loadIntegerValue(TypeGraphBar.this.mContext, PrefVisualizerFullView.KEY_VI_BOTTOMSET + (i2 + 1));
                }
                this.imgColorDown = colorset[((this.myIndex * colorset.length) / TypeGraphBar.this.mGRAPHBAR_SHOW_COUNT) % colorset.length];
                this.mOrPaint.setColorFilter(null);
                this.mRePaint.setColorFilter(null);
            } else {
                int[] colorset2 = TypeGraphBar.this.mSpectrumColors;
                switch (value) {
                    case 0:
                        colorset2 = TypeGraphBar.this.mSpectrumColors;
                        break;
                    case 2:
                        colorset2 = TypeGraphBar.this.mBlueColors;
                        break;
                    case 3:
                        colorset2 = TypeGraphBar.this.mPurpleColorSet;
                        break;
                    case 4:
                        colorset2 = TypeGraphBar.this.mGreenColorSet;
                        break;
                    case 5:
                        colorset2 = TypeGraphBar.this.mYellowColorSet;
                        break;
                    case 6:
                        colorset2 = TypeGraphBar.this.mRedColorSet;
                        break;
                }
                this.imgColorUp = colorset2[((this.myIndex * colorset2.length) / TypeGraphBar.this.mGRAPHBAR_SHOW_COUNT) % colorset2.length];
                this.imgColorDown = -1;
                this.mOrPaint.setColor(this.imgColorUp);
                this.mRePaint.setColor(this.imgColorUp);
                this.mOrPaint.setColorFilter(null);
                this.mRePaint.setColorFilter(null);
            }
        }

        public void updateAlpha(int value) {
            TypeGraphBar.this.mAlphaValue = value;
            this.mOrPaint.setAlpha(value);
            this.mRePaint.setAlpha((value * 2) / 5);
        }

        public void updatePosition() {
            this.width = TypeGraphBar.this.mGRAPHBAR_WIDTH;
            this.posX = TypeGraphBar.this.mGRAPH_X + (this.myIndex * this.width);
            this.stickH = (this.width / 16) + 1;
            this.gap = (this.width / 20) + 1;
            if (TypeGraphBar.this.mViewSize == 1) {
                this.posY = TypeGraphBar.this.mSCREEN_HEIGHT;
            } else {
                this.posY = (int) (((float) TypeGraphBar.this.mSCREEN_HEIGHT) * (1.0f - TypeGraphBar.this.mREFLECTION_RATIO));
            }
            if (this.posX + this.width > TypeGraphBar.this.mSCREEN_WIDTH) {
                this.bAvailable = false;
            } else {
                this.bAvailable = true;
            }
        }

        public void updateBar(int value) {
            this.prevPos = this.currPos;
            this.livePos = this.currPos;
            this.currPos = value;
            this.liveStep = 0;
        }

        public boolean isAvailable() {
            return this.bAvailable;
        }

        public void draw(Canvas c) {
            if (this.bAvailable) {
                this.liveStep++;
                if (this.currPos < this.prevPos) {
                    this.livePos = this.prevPos + AnimationEffect.getValueFromSineData(this.liveStep, (TypeGraphBar.this.mMaxStep * 3) / 2, this.currPos - this.prevPos);
                } else {
                    this.livePos = this.prevPos + AnimationEffect.getValueFromSineData(this.liveStep, TypeGraphBar.this.mMaxStep, this.currPos - this.prevPos);
                }
                int intensity = (TypeGraphBar.this.mGRAPHBAR_HEIGHT * this.livePos) / 250;
                if (intensity < 5) {
                    intensity = 5;
                }
                if (this.imgColorUp == this.imgColorDown) {
                    this.mOrPaint.setColor(this.imgColorUp);
                    this.mRePaint.setColor(this.imgColorUp);
                    updateAlpha(TypeGraphBar.this.mAlphaValue);
                    this.mOrPaint.setShader(null);
                    this.mRePaint.setShader(null);
                } else {
                    this.mOrPaint.setShader(new LinearGradient(0.0f, (float) (this.posY - ((int) (((float) intensity) * (1.0f - TypeGraphBar.this.mREFLECTION_RATIO)))), 0.0f, (float) this.posY, this.imgColorUp, this.imgColorDown, TileMode.CLAMP));
                    this.mRePaint.setShader(new LinearGradient(0.0f, (float) (this.posY + 5), 0.0f, (float) (this.posY + 5 + ((int) (((float) intensity) * TypeGraphBar.this.mREFLECTION_RATIO))), this.imgColorDown, this.imgColorUp, TileMode.CLAMP));
                }
                if (TypeGraphBar.this.mViewSize == 0) {
                    this.mDstRect.set(this.posX, this.posY + 5, this.posX + (this.width - this.gap), this.posY + 5 + ((int) (((float) intensity) * TypeGraphBar.this.mREFLECTION_RATIO)));
                    c.drawRect(this.mDstRect, this.mRePaint);
                    this.mDstRect.set(this.posX, this.posY - ((int) (((float) intensity) * (1.0f - TypeGraphBar.this.mREFLECTION_RATIO))), this.posX + (this.width - this.gap), this.posY);
                    c.drawRect(this.mDstRect, this.mOrPaint);
                } else {
                    this.mDstRect.set(this.posX, this.posY - intensity, this.posX + (this.width - this.gap), this.posY);
                    c.drawRect(this.mDstRect, this.mOrPaint);
                }
                if (TypeGraphBar.this.mViewSize == 0 || TypeGraphBar.this.mShowStick) {
                    if (this.stayCnt > 0) {
                        this.stayCnt--;
                    } else {
                        this.velocity += 0.2f;
                        this.linePos = (int) (((float) this.linePos) + this.velocity);
                    }
                    if (this.linePos >= this.mDstRect.top - 10) {
                        this.linePos = this.mDstRect.top - 10;
                        this.stayCnt = 10;
                        this.velocity = 0.0f;
                    }
                    c.drawRect((float) (this.posX + 1), (float) (this.linePos - this.stickH), (float) ((this.posX + (this.width - this.gap)) - 1), (float) this.linePos, this.mOrPaint);
                }
            }
        }
    }

    class Woofer {
        private int currPos;
        private int livePos;
        private int liveStep;
        private Paint mWoPaint = new Paint();
        private int myIndex;
        private int prevPos;

        Woofer(int index) {
            this.myIndex = index;
        }

        /* access modifiers changed from: protected */
        public void updateBase(int value) {
            this.prevPos = this.currPos;
            this.livePos = this.currPos;
            this.currPos = value;
            this.liveStep = 0;
        }

        public void draw(Canvas c) {
            this.liveStep++;
            if (this.currPos <= this.prevPos) {
                this.livePos--;
            } else {
                this.livePos = this.currPos;
            }
            this.mWoPaint.setColor(SupportMenu.CATEGORY_MASK);
            c.drawCircle((float) ((TypeGraphBar.this.mSCREEN_WIDTH / 20) + ((this.myIndex * TypeGraphBar.this.mSCREEN_WIDTH) / 10)), (float) (TypeGraphBar.this.mSCREEN_HEIGHT / 2), (float) this.livePos, this.mWoPaint);
        }
    }

    public TypeGraphBar(Context context, int viewsize) {
        this.mContext = context;
        this.mViewSize = viewsize;
        this.mPaintBarLine = new Paint();
        this.mPaintBarLine.setARGB(255, 255, 255, 255);
        this.mAlphaValue = PrefVisualizerFullView.loadIntegerValue(this.mContext, PrefVisualizerFullView.KEY_VI_ALPHA, 255);
        this.mColorValue = PrefVisualizerFullView.loadIntegerValue(this.mContext, PrefVisualizerFullView.KEY_VI_COLOR, 0);
        this.mShowStick = PrefVisualizerFullView.loadBooleanValue(this.mContext, PrefVisualizerFullView.KEY_VI_STICK, true);
        this.mUseMic = PrefVisualizerFullView.loadBooleanValue(this.mContext, "deprecated", false);
        this.mMICSensitivity = PrefVisualizerFullView.loadIntegerValue(this.mContext, "deprecated", 80);
        this.mHeightRatio = 0.6f + ((((float) PrefVisualizerFullView.loadIntegerValue(this.mContext, PrefVisualizerFullView.KEY_VI_BARRATIO, 0)) * 0.3f) / 100.0f);
        setUseMic(this.mUseMic);
    }

    public void onSizeChanged(int width, int height, int density) {
        updateMaxStep(this.mHeightRatio);
        this.mSCREEN_WIDTH = width;
        this.mSCREEN_HEIGHT = height;
        this.mSCREEN_DENSITY = density;
        this.mGRAPHBAR_WIDTH = width / 40;
        this.mGRAPHBAR_WIDTH = (int) (((float) this.mGRAPHBAR_WIDTH) * (1.0f + ((this.mHeightRatio - 0.6f) * 3.0f)));
        if (this.mGRAPHBAR_WIDTH <= 0) {
            this.mGRAPHBAR_WIDTH = 1;
        }
        this.mGRAPHBAR_SHOW_COUNT = this.mSCREEN_WIDTH / this.mGRAPHBAR_WIDTH;
        if (this.mGRAPHBAR_SHOW_COUNT == 0) {
            this.mGRAPHBAR_SHOW_COUNT = 1;
        }
        this.mGRAPHBAR_HEIGHT = (int) (((float) height) * this.mHeightRatio);
        this.mGRAPH_WIDTH = this.mGRAPHBAR_WIDTH * this.mGRAPHBAR_SHOW_COUNT;
        this.mGRAPH_X = (width - this.mGRAPH_WIDTH) / 2;
        if (this.mViewSize == 1) {
            this.mGRAPH_Y = height - this.mGRAPHBAR_HEIGHT;
        } else {
            this.mGRAPH_Y = (height - this.mGRAPHBAR_HEIGHT) / 2;
        }
        if (this.mGraphBar == null) {
            this.mGraphBar = new GraphBar[40];
            for (int i = 0; i < 40; i++) {
                this.mGraphBar[i] = new GraphBar(i);
            }
        } else {
            for (int i2 = 0; i2 < 40; i2++) {
                this.mGraphBar[i2].updatePosition();
                this.mGraphBar[i2].updateColorSet(this.mColorValue);
                this.mGraphBar[i2].updateAlpha(this.mAlphaValue);
            }
        }
        if (this.mWoofer == null) {
            this.mWoofer = new Woofer[100];
            for (int i3 = 0; i3 < 100; i3++) {
                this.mWoofer[i3] = new Woofer(i3);
            }
        }
    }

    public void refreshChanged() {
        if (this.mSCREEN_WIDTH > 0 && this.mSCREEN_HEIGHT > 0) {
            onSizeChanged(this.mSCREEN_WIDTH, this.mSCREEN_HEIGHT, this.mSCREEN_DENSITY);
        }
    }

    public void update(byte[] bytes) {
        int midValue;
        double average;
        if (this.mGraphBar != null) {
            if (bytes == null) {
                for (int i = 0; i < 40; i++) {
                    this.mGraphBar[i].updateBar(0);
                }
                return;
            }
            double sum = 0.0d;
            for (int i2 = 1; i2 < bytes.length - 1; i2++) {
                if (bytes[i2] < 0) {
                    bytes[i2] = (byte) (-bytes[i2]);
                }
            }
            int availablecount = 0;
            for (int i3 = 0; i3 < 40; i3++) {
                if (this.mGraphBar[i3].isAvailable()) {
                    availablecount++;
                }
            }
            if (availablecount % 2 == 0) {
                midValue = availablecount - 1;
            } else {
                midValue = availablecount;
            }
            for (int i4 = 0; i4 < availablecount; i4++) {
                double average2 = 0.0d;
                int f = this.GRAPH_TUNING_START + (this.GRAPH_TUNING_GAP * i4);
                while (f < this.GRAPH_TUNING_START + (this.GRAPH_TUNING_GAP * i4) + 10 && f < bytes.length) {
                    average2 += (double) bytes[f];
                    f++;
                }
                if (this.mUseMic) {
                    average = Math.log(1.0d + average2) * (30.0d + (((double) this.mMICSensitivity) / 2.0d));
                } else {
                    average = Math.log(1.0d + (average2 / 10.0d)) * 150.0d;
                }
                if (average > 250.0d) {
                    average = 250.0d + (Math.log10(average - 250.0d) * 10.0d);
                } else if (average < 1.0d) {
                    average = 1.0d;
                }
                sum += average;
                if (i4 % 2 == 0) {
                    this.mGraphBar[(midValue - i4) / 2].updateBar((int) average);
                } else {
                    this.mGraphBar[(midValue + i4) / 2].updateBar((int) average);
                }
            }
            for (int i5 = 0; i5 < 5; i5++) {
                double average3 = 0.0d;
                for (int f2 = i5 * this.GRAPH_TUNING_GAP; f2 < (i5 + 1) * this.GRAPH_TUNING_GAP; f2++) {
                    average3 += (double) bytes[f2];
                }
                double average4 = Math.log(1.0d + (average3 / 10.0d)) * 20.0d;
                if (i5 == 0 || i5 == 1) {
                    this.mWoofer[i5].updateBase((int) average4);
                } else {
                    this.mWoofer[i5].updateBase((int) average4);
                }
            }
            int i6 = 5;
            while (i6 < 10 && this.GRAPH_TUNING_END + ((i6 + 1) * this.GRAPH_TUNING_GAP) < bytes.length) {
                double average5 = 0.0d;
                for (int f3 = this.GRAPH_TUNING_END + (this.GRAPH_TUNING_GAP * i6); f3 < this.GRAPH_TUNING_END + ((i6 + 1) * this.GRAPH_TUNING_GAP); f3++) {
                    average5 += (double) bytes[f3];
                }
                this.mWoofer[i6].updateBase(((int) average5) * 2);
                i6++;
            }
        }
    }

    public void draw(Canvas canvas) {
        for (int i = 0; i < 40; i++) {
            this.mGraphBar[i].draw(canvas);
        }
    }

    private void updateMaxStep(float ratio) {
        if (ratio <= 0.6f) {
            this.mMaxStep = 6;
        } else if (ratio <= 0.9f) {
            this.mMaxStep = 7;
        } else {
            this.mMaxStep = 8;
        }
    }

    public float plusRatio() {
        if (this.mHeightRatio >= 0.9f) {
            this.mHeightRatio = 0.6f;
        } else {
            this.mHeightRatio += 0.1f;
        }
        onSizeChanged(this.mSCREEN_WIDTH, this.mSCREEN_HEIGHT, this.mSCREEN_DENSITY);
        PrefVisualizerFullView.saveFloatValue(this.mContext, PrefVisualizerFullView.KEY_VISUALIZER_RATIO, this.mHeightRatio);
        return this.mHeightRatio;
    }

    public void setAlpha(int value) {
        if (this.mGraphBar != null) {
            for (int i = 0; i < 40; i++) {
                this.mGraphBar[i].updateAlpha(value);
            }
        }
    }

    public void setColorSet(int value) {
        if (this.mGraphBar != null) {
            this.mAlphaValue = PrefVisualizerFullView.loadIntegerValue(this.mContext, PrefVisualizerFullView.KEY_VI_ALPHA, 255);
            this.mColorValue = PrefVisualizerFullView.loadIntegerValue(this.mContext, PrefVisualizerFullView.KEY_VI_COLOR, 0);
            this.mColorValue = value;
            for (int i = 0; i < 40; i++) {
                this.mGraphBar[i].updateColorSet(value);
                this.mGraphBar[i].updateAlpha(this.mAlphaValue);
            }
        }
    }

    public void setBarSize(int value) {
        this.mHeightRatio = 0.6f + ((((float) value) * 0.3f) / 100.0f);
        onSizeChanged(this.mSCREEN_WIDTH, this.mSCREEN_HEIGHT, this.mSCREEN_DENSITY);
    }

    public void setStick(boolean show) {
        this.mShowStick = show;
    }

    public void setUseMic(boolean use) {
        this.mUseMic = use;
        if (this.mUseMic) {
            this.GRAPH_TUNING_START = 0;
            this.GRAPH_TUNING_END = 240;
        } else {
            this.GRAPH_TUNING_START = 80;
            this.GRAPH_TUNING_END = 600;
        }
        this.GRAPH_TUNING_GAP = (this.GRAPH_TUNING_END - this.GRAPH_TUNING_START) / 45;
    }

    public void setMICSensitivity(int value) {
        this.mMICSensitivity = value;
    }

    public int getCustomColorSet() {
        return 8;
    }
}
