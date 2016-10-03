package org.kohaerenzstiftung.ksequencer;

/**
 * Created by sancho on 13.01.16.
 */
class RealStep extends Step {

    final RealPattern mPattern;
    StepValue mStepValue;
    int mOffsetFromParent;
    Step mParent;
    MainActivity.StepButton mButton;
    boolean mSlide = false;

    RealStep(RealPattern pattern, StepValue stepValue,
             Step parent, int offsetFromParent) {
        mPattern = pattern;
        mStepValue = stepValue;
        mParent = parent;
        mOffsetFromParent = offsetFromParent;
    }

    @Override
    boolean isSet() {
        return mStepValue != null;
    }

    boolean isSettable() {
        return !isSet() && mParent.isSet();
    }

    boolean isSlide() {
        boolean result = false;
        if (mStepValue.isNote()) {
            result = mSlide;
        }
        return result;
    }

}
