package org.kohaerenzstiftung.ksequencer;

/**
 * Created by sancho on 16.01.16.
 */
public class NoteStepValue extends StepValue {
    static final int NOTE_C = 0;
    static final int NOTE_D = 1;
    static final int NOTE_E = 2;
    static final int NOTE_F = 3;
    static final int NOTE_G = 4;
    static final int NOTE_A = 5;
    static final int NOTE_B = 6;

    private static final char NOTE_REPRESENTATIONS[] = {'c', 'd', 'e', 'f', 'g', 'a', 'b'};
    static final boolean NOTE_MAY_BE_SHARP[] = {true, true, false, true, true, true, false};
    static final int NOTE_OFFSET[] = {0, 2, 4, 5, 7, 9, 11};
    int mNotee = NOTE_C;
    boolean mSharp = false;
    int mOctave = 0;

    @Override
    public boolean isNote() {
        return true;
    }

    @Override
    public String getRepresentation() {
        return "" + NOTE_REPRESENTATIONS[mNotee] + (mSharp ? "#" : "") + mOctave;
    }
}
