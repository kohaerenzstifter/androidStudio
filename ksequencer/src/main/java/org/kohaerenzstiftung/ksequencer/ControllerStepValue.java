package org.kohaerenzstiftung.ksequencer;

/**
 * Created by sancho on 16.01.16.
 */
public class ControllerStepValue extends StepValue {
    int mValue = 0;

    @Override
    boolean isNote() {
        return false;
    }

    @Override
    String getRepresentation() {
        return "" + mValue;
    }
}
