package org.kohaerenzstiftung.xlatius.grammar.general;

import org.kohaerenzstiftung.xlatius.grammar.backwardnode.BackwardTerminalNode;

public class CompletionState {

	public String mMatch;
	public String mRemainder;
	/*
	 * If n is the number of characters entered by the user, then mHaltCompletions
	 * means that at lease (n + 1) characters have been matched, so
	 * autocompletion will stop at this stage.
	 */
	public boolean mHaltCompletions;

	public BackwardTerminalNode mLastBackwardTerminalNode;

	/*
	 * mCompletelyValid means this completion state holds a complete valid phrase
	 * covered by the grammar
	 */
	public boolean mCompletelyValid = true;

	public CompletionState(BackwardTerminalNode lastTerminalTreeNode, String match,
			String remainder, boolean complete, boolean valid) {
		super();
		mMatch = match;
		mRemainder = remainder;
		mHaltCompletions = complete;
		mLastBackwardTerminalNode = lastTerminalTreeNode;
		mCompletelyValid = valid;
	}
}
