package org.kohaerenzstiftung.ksequencer;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by sancho on 12.01.16.
 */
class RealPattern extends Pattern {
    static final int TYPE_NOTE = 0;
    private static final int TYPE_CONTROLLER = 1;
    Pattern mParent;
    String mName;
    int mChannel = 1;
    int mType;
    int mController = 0;
    public List<StepValue> mStepValues = new LinkedList<StepValue>();


    void setup(String name, int channel) {
        mName = name;
        mChannel = channel;
        mType = TYPE_NOTE;
    }
    void setup(String name, int channel, int controller) {
        mName = name;
        mChannel = channel;
        mType = TYPE_CONTROLLER;
        mController = controller;
    }

    RealPattern(String name, Pattern parent) {
        this(name, parent, parent.getNumberOfBars(), parent.getStepsPerBar());
    }

    private RealPattern(String name, Pattern parent,
                        int numberOfBars, int stepsPerBar) {
        mParent = parent;
        mName = name;

        for (Bar bar : parent.mBars) {
            mBars.add(new RealBar(bar, this));
        }
        rebuild(numberOfBars, stepsPerBar);
        parent.mChildren.add(this);
    }

    private void rebuild(int numberOfBars, int stepsPerBar) {
        int haveBars = mBars.size();
        numberOfBars = (numberOfBars > 0) ? numberOfBars : haveBars;
        stepsPerBar = (stepsPerBar > 0) ? stepsPerBar : getStepsPerBar();

        for (int i = 0; i < numberOfBars; i++) {
            ((RealBar) getBar(haveBars, i)).rebuild(stepsPerBar);
        }

        mBars = mBars.subList(0, numberOfBars);
    }

    private Bar getBar(int haveBars, int index) {
        if (index >= mBars.size()) {
            Bar barToCopy = mBars.get(index % haveBars);
            mBars.add(new RealBar(barToCopy.mSteps, this));
        }
        return mBars.get(index);
    }

    int getNumberOfSteps() {
        return mBars.size() * mBars.get(0).mSteps.size();
    }


    Step getStep(int index) {
        Step result = null;
        int stepsPerBar = mBars.get(0).mSteps.size();
        int barIndex = (index / stepsPerBar);
        index = index % stepsPerBar;
        Bar bar = mBars.get(barIndex);
        result = bar.mSteps.get(index);

        return result;
    }

    void setNumberOfBars(int numberOfBars) {
        rebuild(numberOfBars, -1);
    }

    void setStepsPerBar(int stepsPerBar) {
        rebuild(-1, stepsPerBar);
    }

    @Override
    boolean isRoot() {
        return false;
    }


    public boolean isNote() {
        return (mType == TYPE_NOTE);
    }

    public void changeParent(RootPattern parent) {
        mParent.mChildren.remove(this);
        parent.mChildren.add(this);
        mParent = parent;
        for (Bar bar : mBars) {
            RealBar realBar = (RealBar) bar;
            int offset = 0;
            for (Step step : realBar.mSteps) {
                RealStep realStep = (RealStep) step;
                realStep.mParent = parent.mBars.get(0).mSteps.get(0);
                realStep.mOffsetFromParent = offset++;
            }
        }
    }
}
