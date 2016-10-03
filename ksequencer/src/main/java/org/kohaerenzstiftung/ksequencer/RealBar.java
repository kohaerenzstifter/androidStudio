package org.kohaerenzstiftung.ksequencer;

import java.util.List;

/**
 * Created by sancho on 13.01.16.
 */
class RealBar extends Bar {
    private RealPattern mPattern = null;

    RealBar(Bar parent, RealPattern pattern) {
        mPattern = pattern;
        for (Step step : parent.mSteps) {
            mSteps.add(new RealStep(pattern, null, step, 0));
        }
    }

    RealBar(List<Step> steps, RealPattern pattern) {
        for (Step step : steps) {
            mSteps.add(new RealStep(pattern, ((RealStep) step).mStepValue, ((RealStep) step).mParent, ((RealStep) step).mOffsetFromParent));
        }
    }

    void rebuild(int steps) {
        int haveSteps;
        if (steps > (haveSteps = mSteps.size())) {
            multiply(steps / haveSteps);
        } else {
            divide(haveSteps / steps);
        }
    }

    private void multiply(int by) {
        int addPer = (by - 1);
        int offset = 1;

        int stepsSize = mSteps.size();
        for (int i = 0; i < stepsSize; i++) {
            RealStep step = (RealStep) mSteps.get(i + offset - 1);
            int tmp = step.mOffsetFromParent;
            int offsetFromParent = 0;
            if (tmp == 0) {
                offsetFromParent = 0;
            } else {
                step.mOffsetFromParent = ++offsetFromParent;
            }
            Step parent = step.mParent;
            for (int j = 0; j < addPer; j++) {
                mSteps.add(i + offset, new RealStep(mPattern, null, parent, ++offsetFromParent));
                offset++;
            }
        }
    }

    private void divide(int by) {
        int stepsSize = mSteps.size() / by;
        int removePer = (by - 1);
        int offset = 1;

        for (int i = 0; i < stepsSize; i++) {
            RealStep step = (RealStep) mSteps.get(offset - 1);
            int offsetFromParent = 0;
            if (step.mOffsetFromParent == 0) {
                offsetFromParent = 0;
            } else {
                step.mOffsetFromParent = ++offsetFromParent;
            }
            for (int j = 0; j < removePer; j++) {
                mSteps.remove(offset);
            }
            offset++;
        }
    }
}
