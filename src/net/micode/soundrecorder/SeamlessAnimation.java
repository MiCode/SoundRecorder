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

import android.view.animation.Animation;
import android.view.animation.Transformation;

public class SeamlessAnimation extends Animation {

    private float mFromDegrees;

    private float mToDegrees;

    private float mPivotX;

    private float mPivotY;

    private int mPivotXType;

    private float mPivotXValue;

    private int mPivotYType;

    private float mPivotYValue;

    private boolean mCancelled;

    private float mDegree;

    public SeamlessAnimation(float fromDegrees, float toDegrees, int pivotXType, float pivotXValue,
            int pivotYType, float pivotYValue) {
        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;
        mPivotXType = pivotXType;
        mPivotXValue = pivotXValue;
        mPivotYType = pivotYType;
        mPivotYValue = pivotYValue;
        mCancelled = false;
        mDegree = fromDegrees;
    }

    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mPivotX = resolveSize(mPivotXType, mPivotXValue, width, parentWidth);
        mPivotY = resolveSize(mPivotYType, mPivotYValue, height, parentHeight);
    }

    public float getDegree() {
        return mDegree;
    }

    @Override
    public void cancel() {
        super.cancel();
        mCancelled = true;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        if (!mCancelled) {
            mDegree = mFromDegrees + ((mToDegrees - mFromDegrees) * interpolatedTime);
        }
        t.getMatrix().setRotate(mDegree, mPivotX, mPivotY);
    }

}
