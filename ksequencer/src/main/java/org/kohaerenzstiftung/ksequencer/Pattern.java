package org.kohaerenzstiftung.ksequencer;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by sancho on 19.12.15.
 */
abstract class Pattern {

    List<Bar> mBars = new LinkedList<Bar>();
    List<RealPattern> mChildren = new LinkedList<RealPattern>();

    abstract boolean isRoot();
    
    int getNumberOfBars() {
        return mBars.size();
    }

    int getStepsPerBar() {
        return mBars.get(0).mSteps.size();
    }
}
