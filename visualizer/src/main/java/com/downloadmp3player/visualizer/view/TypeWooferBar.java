package com.downloadmp3player.visualizer.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class TypeWooferBar implements ITypeView {
    private static final int SPEAKER_HIGH = 3;
    private static final int SPEAKER_LOW = 1;
    private static final int SPEAKER_MEDIUM = 2;
    private static final int SPEAKER_NONE = 0;
    private static final int SPEAKER_SUPER = 4;
    private final int WOOFERBAR_BASE = 5;
    /* access modifiers changed from: private */
    public int WOOFERBAR_CENTER = 0;
    private final int WOOFERBAR_GAP = 1;
    private final int WOOFERBAR_SHOW_MAX = 46;
    private final int WOOFER_TUNING_END = 600;
    private final int WOOFER_TUNING_GAP = 11;
    private final int WOOFER_TUNING_MAX = 250;
    private final int WOOFER_TUNING_MIN = 1;
    private final int WOOFER_TUNING_START = 80;
    /* access modifiers changed from: private */
    public Bitmap[] mBmpSpeakerHigh;
    /* access modifiers changed from: private */
    public Bitmap[] mBmpSpeakerLow;
    /* access modifiers changed from: private */
    public Bitmap[] mBmpSpeakerMedium;
    /* access modifiers changed from: private */
    public Bitmap[] mBmpSpeakerSuper;
    /* access modifiers changed from: private */
    public Bitmap mBmpWooferBG;
    /* access modifiers changed from: private */
    public Bitmap[] mBmpWooferBar;
    /* access modifiers changed from: private */
    public int mFULL_HEIGHT = 0;
    /* access modifiers changed from: private */
    public int mFULL_WIDTH = 0;
    private double mMaxAverage;
    private int mSCREEN_DENSITY;
    private int mSCREEN_HEIGHT;
    private int mSCREEN_WIDTH;
    /* access modifiers changed from: private */
    public int mWOOFERBAR_EDGE = 0;
    /* access modifiers changed from: private */
    public int mWOOFERBAR_WIDTH = 0;
    /* access modifiers changed from: private */
    public int mWOOFER_HEIGHT = 0;
    /* access modifiers changed from: private */
    public int mWOOFER_WIDTH = 0;
    /* access modifiers changed from: private */
    public int mWOOFER_X = 0;
    /* access modifiers changed from: private */
    public int mWOOFER_Y = 0;
    private WooferBar[] mWooferBar;
    private WooferSpeaker mWooferSpeaker;

    class WooferBar {
        private int base;
        private int bmpIndex;
        private int currPos;
        private int edge;
        private int intensity;
        private int livePos;
        private int liveStep;
        private Rect mDstRect = new Rect();
        private Rect mSrcRect = new Rect();
        private int myIndex;
        private int posX;
        private int posY;
        private int prevPos;
        private double ratio;

        WooferBar(int index) {
            this.myIndex = index;
            if (this.myIndex >= 46) {
                this.bmpIndex = TypeWooferBar.this.mBmpWooferBar.length - 1;
            } else {
                this.bmpIndex = this.myIndex;
            }
            this.posX = TypeWooferBar.this.mWOOFER_X + (TypeWooferBar.this.mWOOFERBAR_WIDTH * index);
            this.posY = TypeWooferBar.this.mWOOFER_Y + (TypeWooferBar.this.mWOOFER_HEIGHT / 2);
            this.base = 5;
            if (index < 23) {
                this.ratio = (1.0d - Math.cos(Math.toRadians(((((double) index) / 4.0d) * 360.0d) / 46.0d))) + 0.9d;
            } else {
                this.ratio = (1.0d - Math.cos(Math.toRadians(((((double) (46 - index)) / 4.0d) * 360.0d) / 46.0d))) + 0.9d;
            }
        }

        public void updateBar(int value) {
            this.prevPos = this.currPos;
            this.livePos = this.currPos;
            this.currPos = value;
            this.liveStep = 0;
        }

        public void reset() {
            this.prevPos = 1;
            this.livePos = 1;
            this.currPos = 1;
            this.liveStep = 0;
        }

        public void draw(Canvas c) {
            this.liveStep++;
            this.livePos = this.prevPos + AnimationEffect.getValueFromSineData(this.liveStep, 5, this.currPos - this.prevPos);
            this.intensity = this.base + ((((TypeWooferBar.this.WOOFERBAR_CENTER - this.base) / 2) * this.livePos) / 250);
            this.edge = (TypeWooferBar.this.mWOOFERBAR_EDGE / 2) + (((TypeWooferBar.this.mWOOFERBAR_EDGE / 2) * this.livePos) / 250);
            this.mSrcRect.set(0, (TypeWooferBar.this.mWOOFER_HEIGHT / 2) - this.intensity, TypeWooferBar.this.mBmpWooferBar[this.bmpIndex].getWidth(), (TypeWooferBar.this.mWOOFER_HEIGHT / 2) + this.intensity);
            this.mDstRect.set(this.posX, this.posY - ((int) (((double) this.intensity) * this.ratio)), this.posX + (TypeWooferBar.this.mWOOFERBAR_WIDTH - 1), this.posY + ((int) (((double) this.intensity) * this.ratio)));
            c.drawBitmap(TypeWooferBar.this.mBmpWooferBar[this.bmpIndex], this.mSrcRect, this.mDstRect, null);
            this.mSrcRect.set(0, 0, TypeWooferBar.this.mBmpWooferBar[this.bmpIndex].getWidth(), TypeWooferBar.this.mWOOFERBAR_EDGE);
            this.mDstRect.set(this.posX, (this.posY - ((int) (((double) this.intensity) * this.ratio))) - ((int) (((double) this.edge) * this.ratio)), this.posX + (TypeWooferBar.this.mWOOFERBAR_WIDTH - 1), this.posY - ((int) (((double) this.intensity) * this.ratio)));
            c.drawBitmap(TypeWooferBar.this.mBmpWooferBar[this.bmpIndex], this.mSrcRect, this.mDstRect, null);
            this.mSrcRect.set(0, TypeWooferBar.this.mWOOFER_HEIGHT - TypeWooferBar.this.mWOOFERBAR_EDGE, TypeWooferBar.this.mBmpWooferBar[this.bmpIndex].getWidth(), TypeWooferBar.this.mWOOFER_HEIGHT);
            this.mDstRect.set(this.posX, this.posY + ((int) (((double) this.intensity) * this.ratio)), this.posX + (TypeWooferBar.this.mWOOFERBAR_WIDTH - 1), this.posY + ((int) (((double) this.intensity) * this.ratio)) + ((int) (((double) this.edge) * this.ratio)));
            c.drawBitmap(TypeWooferBar.this.mBmpWooferBar[this.bmpIndex], this.mSrcRect, this.mDstRect, null);
        }
    }

    class WooferSpeaker {
        private int bmpIndex;
        private Bitmap[] bmpSpeaker;
        private Rect dstRect = new Rect();
        private int duration;
        private int intensity;
        private int posX;
        private int posY;
        private Rect srcRect = new Rect();

        WooferSpeaker() {
            this.bmpSpeaker = TypeWooferBar.this.mBmpSpeakerLow;
        }

        /* access modifiers changed from: protected */
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        /* access modifiers changed from: 0000 */
        public void initPos() {
            this.posX = TypeWooferBar.this.mWOOFER_X + (TypeWooferBar.this.mWOOFER_WIDTH / 2);
            this.posY = TypeWooferBar.this.mWOOFER_Y + (TypeWooferBar.this.mWOOFER_HEIGHT / 2);
        }

        public void update(int newIntensity, double total) {
            if (newIntensity >= this.intensity) {
                this.intensity = newIntensity;
                this.bmpIndex = 0;
                if (newIntensity == 4) {
                    this.bmpSpeaker = TypeWooferBar.this.mBmpSpeakerSuper;
                    this.duration = this.bmpSpeaker.length * 2;
                } else if (newIntensity == 3) {
                    this.bmpSpeaker = TypeWooferBar.this.mBmpSpeakerHigh;
                    this.duration = this.bmpSpeaker.length;
                } else if (newIntensity == 2) {
                    this.bmpSpeaker = TypeWooferBar.this.mBmpSpeakerMedium;
                    this.duration = this.bmpSpeaker.length;
                } else {
                    this.bmpSpeaker = TypeWooferBar.this.mBmpSpeakerLow;
                    this.duration = this.bmpSpeaker.length;
                }
            }
        }

        public void draw(Canvas c) {
            if (this.intensity == 0) {
                c.drawBitmap(this.bmpSpeaker[0], (float) (this.posX - (this.bmpSpeaker[0].getWidth() / 2)), (float) (this.posY - (this.bmpSpeaker[0].getHeight() / 2)), null);
            } else {
                c.drawBitmap(this.bmpSpeaker[this.bmpIndex % this.bmpSpeaker.length], (float) (this.posX - (this.bmpSpeaker[this.bmpIndex % this.bmpSpeaker.length].getWidth() / 2)), (float) (this.posY - (this.bmpSpeaker[this.bmpIndex % this.bmpSpeaker.length].getHeight() / 2)), null);
                this.bmpIndex++;
                if (this.bmpIndex >= this.duration) {
                    this.intensity = 0;
                }
            }
            this.srcRect.set(0, 0, TypeWooferBar.this.mBmpWooferBG.getWidth() / 2, TypeWooferBar.this.mBmpWooferBG.getHeight());
            this.dstRect.set(0, (TypeWooferBar.this.mFULL_HEIGHT - TypeWooferBar.this.mBmpWooferBG.getHeight()) / 2, (TypeWooferBar.this.mFULL_WIDTH - TypeWooferBar.this.mWOOFER_WIDTH) / 2, (TypeWooferBar.this.mFULL_HEIGHT + TypeWooferBar.this.mBmpWooferBG.getHeight()) / 2);
            c.drawBitmap(TypeWooferBar.this.mBmpWooferBG, this.srcRect, this.dstRect, null);
            this.srcRect.set(TypeWooferBar.this.mBmpWooferBG.getWidth() / 2, 0, TypeWooferBar.this.mBmpWooferBG.getWidth(), TypeWooferBar.this.mBmpWooferBG.getHeight());
            this.dstRect.set((TypeWooferBar.this.mFULL_WIDTH + TypeWooferBar.this.mWOOFER_WIDTH) / 2, (TypeWooferBar.this.mFULL_HEIGHT - TypeWooferBar.this.mBmpWooferBG.getHeight()) / 2, TypeWooferBar.this.mFULL_WIDTH, (TypeWooferBar.this.mFULL_HEIGHT + TypeWooferBar.this.mBmpWooferBG.getHeight()) / 2);
            c.drawBitmap(TypeWooferBar.this.mBmpWooferBG, this.srcRect, this.dstRect, null);
        }
    }

    public TypeWooferBar(Context context) {
    }

    public void update(byte[] bytes) {
        double sum = 0.0d;
        for (int i = 1; i < bytes.length - 1; i++) {
            if (bytes[i] < 0) {
                bytes[i] = (byte) (-bytes[i]);
            }
        }
        if (this.mWooferBar != null && this.mWooferSpeaker != null) {
            for (int i2 = 0; i2 < 46; i2++) {
                double average = 0.0d;
                for (int f = (i2 * 11) + 80; f < (i2 * 11) + 80 + 10; f++) {
                    average += (double) bytes[f];
                }
                double average2 = Math.log((average / 10.0d) + 1.0d) * 150.0d;
                if (average2 > this.mMaxAverage) {
                    this.mMaxAverage = average2;
                }
                if (average2 > 250.0d) {
                    average2 = 250.0d;
                } else if (average2 < 1.0d) {
                    average2 = 1.0d;
                }
                sum += average2;
                if (i2 % 2 == 0) {
                    this.mWooferBar[(45 - i2) / 2].updateBar((int) average2);
                } else {
                    this.mWooferBar[(i2 + 45) / 2].updateBar((int) average2);
                }
            }
            if (sum > 7666.0d) {
                this.mWooferSpeaker.update(4, sum);
            } else if (sum > 5750.0d) {
                this.mWooferSpeaker.update(3, sum);
            } else if (sum > 3833.0d) {
                this.mWooferSpeaker.update(2, sum);
            } else if (sum > 1437.0d) {
                this.mWooferSpeaker.update(1, sum);
            }
        }
    }

    public void draw(Canvas canvas) {
        for (int i = 0; i < 46; i++) {
            this.mWooferBar[i].draw(canvas);
        }
        this.mWooferSpeaker.draw(canvas);
    }

    public void onSizeChanged(int width, int height, int density) {
        this.mFULL_WIDTH = width;
        this.mFULL_HEIGHT = height;
        this.mSCREEN_WIDTH = width;
        this.mSCREEN_HEIGHT = height;
        this.mSCREEN_DENSITY = density;
        this.mWOOFER_WIDTH = width < density * 350 ? width : density * 350;
        this.mWOOFER_HEIGHT = this.mBmpWooferBar[0].getHeight();
        this.mWOOFERBAR_WIDTH = this.mWOOFER_WIDTH / 46;
        this.mWOOFER_X = (width - this.mWOOFER_WIDTH) / 2;
        this.mWOOFER_Y = (height - this.mWOOFER_HEIGHT) / 2;
        this.WOOFERBAR_CENTER = density * 130;
        this.mWOOFERBAR_EDGE = (this.mWOOFER_HEIGHT - this.WOOFERBAR_CENTER) / 2;
        if (this.mWooferSpeaker == null) {
            this.mWooferSpeaker = new WooferSpeaker();
        }
        this.mWooferSpeaker.initPos();
        this.mWooferBar = new WooferBar[46];
        for (int i = 0; i < 46; i++) {
            this.mWooferBar[i] = new WooferBar(i);
        }
    }

    public void refreshChanged() {
        onSizeChanged(this.mSCREEN_WIDTH, this.mSCREEN_HEIGHT, this.mSCREEN_DENSITY);
    }

    public float plusRatio() {
        return 0.0f;
    }

    public void setAlpha(int value) {
    }

    public void setColorSet(int value) {
    }

    public void setBarSize(int value) {
    }

    public void setStick(boolean show) {
    }

    public void setUseMic(boolean use) {
    }

    public void setMICSensitivity(int value) {
    }

    public int getCustomColorSet() {
        return 0;
    }
}
