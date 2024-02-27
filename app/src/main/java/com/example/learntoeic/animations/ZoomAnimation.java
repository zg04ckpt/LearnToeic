package com.example.learntoeic.animations;

import android.animation.ObjectAnimator;
import android.view.View;

public class ZoomAnimation {
    public static void animateZoomIn(View view) {
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(view, View.SCALE_X, 0.5f, 1f);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(view, View.SCALE_Y, 0.5f, 1f);

        ObjectAnimator fadeInAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f);

        scaleXAnimator.setDuration(500);
        scaleYAnimator.setDuration(500);
        fadeInAnimator.setDuration(500);

        scaleXAnimator.start();
        scaleYAnimator.start();
        fadeInAnimator.start();

        view.setVisibility(View.VISIBLE);
    }

    public static void animateZoomOut(View view) {
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 0.5f);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 0.5f);

        ObjectAnimator fadeOutAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f);

        scaleXAnimator.setDuration(500);
        scaleYAnimator.setDuration(500);
        fadeOutAnimator.setDuration(500);

        scaleXAnimator.start();
        scaleYAnimator.start();
        fadeOutAnimator.start();

        view.setVisibility(View.INVISIBLE);
    }
}
