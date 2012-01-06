/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.micode.soundrecorder;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

public class WheelImageView extends ImageView {

    SeamlessAnimation mAnimation;

    public WheelImageView(Context context) {
        super(context);
        mAnimation = null;
    }

    public WheelImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAnimation = null;
    }

    public WheelImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mAnimation = null;
    }

    private void initAnimation(long duration, boolean isForward, int repeatCount) {
        LinearInterpolator lir = new LinearInterpolator();
        float from;
        float to;
        if (isForward) {
            from = (mAnimation == null || mAnimation.getRepeatCount() != Animation.INFINITE) ? 0.0f
                    : mAnimation.getDegree();
            to = from + 360.0f;
        } else {
            from = (mAnimation == null || mAnimation.getRepeatCount() != Animation.INFINITE) ? 360.0f
                    : mAnimation.getDegree();
            to = from - 360.0f;
        }

        if (isForward) {
            mAnimation = new SeamlessAnimation(from, to, Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
        } else {
            mAnimation = new SeamlessAnimation(from, to, Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
        }
        mAnimation.setDuration(duration);
        mAnimation.setRepeatMode(Animation.RESTART);
        mAnimation.setRepeatCount(repeatCount);
        mAnimation.setInterpolator(lir);
    }

    public void startAnimation(long duration, boolean isForward) {
        startAnimation(duration, isForward, Animation.INFINITE);
    }

    public void startAnimation(long duration, boolean isForward, int repeatCount) {
        initAnimation(duration, isForward, repeatCount);
        startAnimation(mAnimation);
    }

    public void stopAnimation() {
        if (mAnimation != null) {
            mAnimation.cancel();
            mAnimation = null;
            clearAnimation();
        }
    }

}
