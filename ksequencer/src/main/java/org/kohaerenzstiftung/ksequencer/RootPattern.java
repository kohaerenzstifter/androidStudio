package org.kohaerenzstiftung.ksequencer;

/**
 * Created by sancho on 02.01.16.
 */
class RootPattern extends Pattern {
    RootPattern() {
        mBars.add(new RootBar());
    }

    @Override
    boolean isRoot() {
        return true;
    }
}
