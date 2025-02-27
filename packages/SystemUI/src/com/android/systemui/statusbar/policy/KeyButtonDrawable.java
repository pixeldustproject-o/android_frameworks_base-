/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.systemui.statusbar.policy;

import com.android.systemui.navigation.DarkIntensity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.Nullable;
import android.app.ActivityManager;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.Gravity;

/**
 * Drawable for {@link KeyButtonView}s which contains an asset for both normal mode and light
 * navigation bar mode.
 */
public class KeyButtonDrawable extends LayerDrawable implements DarkIntensity {

    private float mRotation;
    private Animator mCurrentAnimator;
    private boolean mIsBackButton;

    private static final int ANIMATION_DURATION = 200;

    private final boolean mHasDarkDrawable;

    public static KeyButtonDrawable create(Drawable lightDrawable,
            @Nullable Drawable darkDrawable) {
        if (darkDrawable != null) {
            return new KeyButtonDrawable(
                    new Drawable[] { lightDrawable.mutate(), darkDrawable.mutate() });
        } else {
            return new KeyButtonDrawable(new Drawable[] { lightDrawable.mutate() });
        }
    }

    private KeyButtonDrawable(Drawable[] drawables) {
        super(drawables);
        for (int i = 0; i < drawables.length; i++) {
            setLayerGravity(i, Gravity.CENTER);
        }
        mutate();
        mHasDarkDrawable = drawables.length > 1;
        setDarkIntensity(0f);
    }

    @Override
    public void setDarkIntensity(float intensity) {
        if (!mHasDarkDrawable) {
            return;
        }
        getDrawable(0).setAlpha((int) ((1 - intensity) * 255f));
        getDrawable(1).setAlpha((int) (intensity * 255f));
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        if (mIsBackButton) {
            final Rect bounds = getBounds();
            final int boundsCenterX = bounds.width() / 2;
            final int boundsCenterY = bounds.height() / 2;

            canvas.save();
            canvas.translate(boundsCenterX, boundsCenterY);
            canvas.rotate(mRotation);
            canvas.translate(-boundsCenterX, -boundsCenterY);

            super.draw(canvas);
            canvas.restore();
        } else {
            super.draw(canvas);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        super.setAlpha(alpha);
        if (mCurrentAnimator != null) {
            mCurrentAnimator.end();
        }
    }

    public void setRotation(float rotation) {
        mRotation = rotation;
        invalidateSelf();
    }

    public float getRotation() {
        return mRotation;
    }

    public void setImeVisible(boolean ime) {
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        final float nextRotation = ime ? -90 : 0;
        if (mRotation == nextRotation) {
            return;
        }

        if (isVisible() && ActivityManager.isHighEndGfx()) {
            mCurrentAnimator = ObjectAnimator.ofFloat(this, "rotation", nextRotation)
                    .setDuration(ANIMATION_DURATION);
            mCurrentAnimator.start();
        } else {
            setRotation(nextRotation);
        }
    }

    public void setIsBackButton(boolean isBackButton) {
        mIsBackButton = isBackButton;
    }
}
