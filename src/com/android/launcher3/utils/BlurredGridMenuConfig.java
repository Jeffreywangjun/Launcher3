package com.android.launcher3.utils;

import android.graphics.Color;


public class BlurredGridMenuConfig {

    public static int BLUR_RADIUS = 10;

    public static int DOWNSAMPLE_FACTOR = 4;

    public static int OVERLAY_COLOR = Color.parseColor("#66000000");


    public static void build(Builder builder) {
        BLUR_RADIUS = builder.mRadius;
        DOWNSAMPLE_FACTOR = builder.mDownsample;
        OVERLAY_COLOR = builder.mOverlayColor;
    }


    public static class Builder {

        public int mRadius = BLUR_RADIUS;

        public int mDownsample = DOWNSAMPLE_FACTOR;

        public int mOverlayColor = OVERLAY_COLOR;

        public Builder radius(int radius) {
            this.mRadius = radius;
            return this;
        }

        public Builder downsample(int downsample) {
            this.mDownsample = downsample;
            return this;
        }

        public Builder overlayColor(int overlayColor) {
            this.mOverlayColor = overlayColor;
            return this;
        }

    }

}
