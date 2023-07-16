package com.downloadmp3player.visualizer.view;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class AnimationEffect {
    private static final int[] CurveData = {0, 85, 159, 231, 295, 348, 401, 448, 489, 533, 566, 603, 633, 660, 688, 711, 734, 755, 774, 794, 812, 827, 843, 857, 872, 882, 892, 902, 913, 921, 929, 937, 944, 948, 955, 961, 965, 968, 975, 977, 981, 983, 987, 989, 991, 993, 995, 995, 997, 998, 1000, 1000};
    public static final int EFFECT_ADD_DISTANCE = 300;
    public static final int EFFECT_ADD_POSITION = 200;
    public static final int EFFECT_ADD_SIZE = 400;
    public static final int EFFECT_DISTANCE = 10;
    public static final int EFFECT_FLING = 5;
    public static final int EFFECT_NEXT = 1;
    public static final int EFFECT_NONE = 0;
    public static final int EFFECT_PLAY_FORCE = 100;
    public static final int EFFECT_PREV = 2;
    public static final int EFFECT_RELOCATE = 3;
    public static final int EFFECT_TO_FULL = 6;
    public static final int EFFECT_TO_NARROW = 9;
    public static final int EFFECT_TO_NAVI = 7;
    public static final int EFFECT_TO_WIDE = 8;
    private static final int MSG_EFFECT_END = 1;
    private static final int[] SineData = {0, 31, 62, 94, 125, 156, 187, 218, 248, 278, 309, 338, 368, 397, 425, 453, 481, 509, 535, 562, 587, 612, 637, 661, 684, 707, 728, 750, 770, 790, 809, 827, 844, 860, 876, 891, 904, 917, 929, 940, 951, 960, 968, 975, 982, 987, 992, 995, 998, 999, 1000, 1000};
    private static final String TAG = "AnimationEffect";
    private int curMoveX;
    private int curMoveY;
    private int curSize;
    private int curStep;
    private int dstPosX;
    private int dstPosY;
    private int dstSize;
    private int idx_of_move;
    private float mAcceleration;
    public callback mCallback;
    private int mGENERAL_MAXSTEP = 15;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (AnimationEffect.this.mCallback != null) {
                        AnimationEffect.this.mCallback.onAnimationEnd(AnimationEffect.this, msg.arg1, msg.arg2);
                        return;
                    }
                    return;
                default:
                    Log.e(AnimationEffect.TAG, "handleMessage() msg : " + msg);
                    return;
            }
        }
    };
    private double mTheta;
    private int mVelocity;
    private int maxMoveX;
    private int maxMoveY;
    private int maxSize;
    private int maxStep;
    private int movePosX;
    private int movePosY;
    private int next;
    private int srcPosX;
    private int srcPosY;
    private int srcSize;
    private int type = 0;

    class PosInfo {
        int posX;
        int posY;
        int size;

        PosInfo() {
        }
    }

    public interface callback {
        void onAnimationBegin(int i);

        void onAnimationEnd(AnimationEffect animationEffect, int i, int i2);
    }

    private int getData(int[] dataArray, int step, int maxStep2, int maxLen) {
        return (int) ((((long) dataArray[((step * 52) / maxStep2) - 1]) * ((long) maxLen)) / 1000);
    }

    public static int getValueFromCurveData(int step, int maxStep2, int maxLen) {
        return step >= maxStep2 ? maxLen : (CurveData[((step * 52) / maxStep2) - 1] * maxLen) / 1000;
    }

    public static int getValueFromSineData(int step, int maxStep2, int maxLen) {
        return step >= maxStep2 ? maxLen : (SineData[((step * 52) / maxStep2) - 1] * maxLen) / 1000;
    }

    private void OnEnd() {
        Log.v(TAG, "OnEnd(" + this.type + ")");
        Message msg = this.mHandler.obtainMessage(1);
        msg.arg1 = this.type;
        msg.arg2 = this.next;
        this.type = 0;
        this.next = 0;
        if (!this.mHandler.hasMessages(1)) {
            this.mHandler.sendMessage(msg);
        }
    }

    public void initIndex(int index) {
        this.idx_of_move = index;
    }

    public void initPosition(int oriPosX, int oriPosY, int dstPosX2, int dstPosY2, int maxStep2) {
        this.srcPosX = oriPosX;
        this.srcPosY = oriPosY;
        this.dstPosX = dstPosX2;
        this.dstPosY = dstPosY2;
        this.movePosX = oriPosX;
        this.movePosY = oriPosY;
        this.maxMoveX = dstPosX2 - oriPosX;
        this.maxMoveY = dstPosY2 - oriPosY;
        this.curStep = 0;
        this.maxStep = maxStep2;
        if (maxStep2 <= 0) {
            this.maxStep = this.mGENERAL_MAXSTEP;
        }
    }

    public void initVelocity(int posX, int posY, int velocity, float acceleration, double theta) {
        this.movePosX = posX;
        this.movePosY = posY;
        this.mTheta = theta;
        this.mVelocity = velocity;
        this.mAcceleration = acceleration;
        if (velocity < 0) {
            this.mAcceleration = acceleration;
        } else {
            this.mAcceleration = -acceleration;
        }
    }

    public void initSize(int oriSize, int dstSize2, int maxStep2) {
        this.srcSize = oriSize;
        this.dstSize = dstSize2;
        this.curSize = oriSize;
        this.maxSize = dstSize2 - oriSize;
        this.curStep = 0;
        this.maxStep = maxStep2;
        if (maxStep2 <= 0) {
            this.maxStep = this.mGENERAL_MAXSTEP;
        }
    }

    public void addDistance(int oriSize, int addSize, int maxStep2) {
        if (this.type != 300) {
            initSize(oriSize, oriSize + addSize, maxStep2);
        } else {
            initSize(oriSize, this.dstSize + addSize, maxStep2);
        }
    }

    public void addPosition(int oriPosX, int oriPosY, int addPosX, int addPosY, int maxStep2) {
        if (this.type != 200) {
            initPosition(oriPosX, oriPosY, oriPosX + addPosX, oriPosY + addPosY, maxStep2);
            return;
        }
        initPosition(oriPosX, oriPosY, this.dstPosX + addPosX, this.dstPosY + addPosY, maxStep2);
    }

    public void addSize(int oriSize, int addSize, int maxStep2) {
        if (this.type != 400) {
            initSize(oriSize, oriSize + addSize, maxStep2);
        } else {
            initSize(oriSize, this.dstSize + addSize, maxStep2);
        }
    }

    public void start(int effect) {
        Log.v(TAG, "start(" + effect + ")");
        this.type = effect;
        this.mHandler.removeMessages(1);
        if (this.mCallback != null) {
            this.mCallback.onAnimationBegin(effect);
        }
    }

    public void stop() {
        if (this.type != 0) {
            Log.v(TAG, "stop()");
            this.type = 0;
        }
    }

    public void setMoreAcceleration(int multiple) {
        this.mAcceleration *= (float) multiple;
    }

    public void next(int effect) {
        Log.v(TAG, "next(" + effect + ")");
        this.next = effect;
    }

    public void update() {
        switch (this.type) {
            case 1:
            case 2:
            case EFFECT_ADD_POSITION /*200*/:
                if (this.curStep == this.maxStep || (this.srcPosX == this.dstPosX && this.srcPosY == this.dstPosY)) {
                    OnEnd();
                    return;
                }
                this.curStep++;
                this.curMoveX = getData(CurveData, this.curStep, this.maxStep, this.maxMoveX);
                this.movePosX = this.srcPosX + this.curMoveX;
                this.curMoveY = getData(CurveData, this.curStep, this.maxStep, this.maxMoveY);
                this.movePosY = this.srcPosY + this.curMoveY;
                return;
            case 3:
                if (calculate_pos_step(SineData)) {
                    OnEnd();
                    return;
                }
                return;
            case 5:
                this.mVelocity = (int) (((float) this.mVelocity) + this.mAcceleration);
                if ((this.mVelocity <= 0 || this.mAcceleration <= 0.0f) && (this.mVelocity >= 0 || this.mAcceleration >= 0.0f)) {
                    this.movePosX = (int) (((double) this.movePosX) + (((double) this.mVelocity) * Math.cos(this.mTheta)));
                    this.movePosY = (int) (((double) this.movePosY) + (((double) this.mVelocity) * Math.sin(this.mTheta)));
                    return;
                }
                OnEnd();
                return;
            case 6:
            case 7:
            case EFFECT_ADD_SIZE /*400*/:
                if (calculate_both_step(CurveData, CurveData)) {
                    OnEnd();
                    return;
                }
                return;
            case 8:
            case 9:
            case 10:
            case 300:
                if (calculate_size_step(CurveData)) {
                    OnEnd();
                    return;
                }
                return;
            default:
                return;
        }
    }

    private boolean calculate_size_step(int[] data) {
        if (this.curStep == this.maxStep || this.srcSize == this.dstSize) {
            return true;
        }
        this.curStep++;
        this.curSize = this.srcSize + getData(CurveData, this.curStep, this.maxStep, this.maxSize);
        return false;
    }

    private boolean calculate_pos_step(int[] data) {
        if (this.curStep == this.maxStep || (this.srcPosX == this.dstPosX && this.srcPosY == this.dstPosY)) {
            return true;
        }
        this.curStep++;
        this.curMoveX = getData(data, this.curStep, this.maxStep, this.maxMoveX);
        this.movePosX = this.srcPosX + this.curMoveX;
        this.curMoveY = getData(data, this.curStep, this.maxStep, this.maxMoveY);
        this.movePosY = this.srcPosY + this.curMoveY;
        return false;
    }

    private boolean calculate_both_step(int[] data1, int[] data2) {
        if (this.curStep == this.maxStep) {
            return true;
        }
        this.curStep++;
        this.curSize = this.srcSize + getData(CurveData, this.curStep, this.maxStep, this.maxSize);
        this.curMoveX = getData(data1, this.curStep, this.maxStep, this.maxMoveX);
        this.movePosX = this.srcPosX + this.curMoveX;
        this.curMoveY = getData(data2, this.curStep, this.maxStep, this.maxMoveY);
        this.movePosY = this.srcPosY + this.curMoveY;
        return false;
    }

    public int moveX() {
        return this.movePosX;
    }

    public int moveY() {
        return this.movePosY;
    }

    public int getSize() {
        return this.curSize;
    }

    public int getIndex() {
        return this.idx_of_move;
    }

    public boolean isWorking() {
        return this.type != 0;
    }

    public boolean isMoving() {
        return this.type == 1 || this.type == 2 || this.type == 3 || this.type == 5 || this.type == 200;
    }

    public boolean isFling() {
        return this.type == 5;
    }

    public boolean isIndexMove() {
        return this.type == 6 || this.type == 7 || this.type == 400;
    }

    public boolean isResizing() {
        return this.type == 6 || this.type == 7 || this.type == 400;
    }

    public boolean isDistance() {
        return this.type == 8 || this.type == 9 || this.type == 10 || this.type == 300;
    }

    public int hasNext() {
        return this.next;
    }

    public void addCallback(callback callback2) {
        this.mCallback = callback2;
    }
}
