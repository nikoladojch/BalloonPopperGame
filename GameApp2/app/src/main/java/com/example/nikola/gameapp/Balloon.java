package com.example.nikola.gameapp;


import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import com.example.nikola.gameapp.utils.PixelHelper;

/**
 * Created by Nikola on 05.01.2018.
 */

public class Balloon extends android.support.v7.widget.AppCompatImageView implements Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {

    private ValueAnimator mAnimator;
    private BalloonListener mListener;
    private boolean mPopped;
    private int ballonColor;

    public Balloon(Context context) {
        super(context);
    }

    public Balloon(Context context, int color, int rawHeight) {
        super(context);

        mListener = (BalloonListener) context;

        this.setImageResource(R.drawable.balloon);
        this.setColorFilter(color);
        ballonColor = color;

        int rawWidth = rawHeight / 2;

        int dpHeight = PixelHelper.PixelsToDp(rawHeight, context);
        int dpWidth = PixelHelper.PixelsToDp(rawWidth, context);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(dpWidth, dpHeight);

        setLayoutParams(params);


    }

    public Balloon(Context context, int color, int rawHeight, boolean a) {
        super(context);

        mListener = (BalloonListener) context;

        this.setImageResource(R.drawable.heart);
        this.setColorFilter(color);
        ballonColor = color;

        int rawWidth = rawHeight / 2;

        int dpHeight = PixelHelper.PixelsToDp(rawHeight, context);
        int dpWidth = PixelHelper.PixelsToDp(rawWidth, context);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(dpWidth, dpHeight);

        setLayoutParams(params);
    }


    public void releaseBalloon(int screenHeight, int duration, float end){
        mAnimator = new ValueAnimator();
        mAnimator.setDuration(duration);
        mAnimator.setFloatValues(screenHeight, end);
        mAnimator.setInterpolator(new OvershootInterpolator());
        mAnimator.setTarget(this);
        mAnimator.addListener(this);
        mAnimator.addUpdateListener(this);
        mAnimator.start();
    }

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
        if(!mPopped){
            mListener.popBalloon(this, false);
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        setY((float) animation.getAnimatedValue());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!mPopped && event.getAction() == MotionEvent.ACTION_DOWN){
            mListener.popBalloon(this, true);
            mPopped = true;
            mAnimator.cancel();
        }
        return super.onTouchEvent(event);
    }

    public void setPopped(boolean popped) {
        mPopped = popped;
        if(popped){
            mAnimator.cancel();
        }
    }

    public int getBallonColor(){
        return ballonColor;
    }


    public interface BalloonListener{
        void popBalloon(Balloon balloon, boolean userTouch);
    }
}
