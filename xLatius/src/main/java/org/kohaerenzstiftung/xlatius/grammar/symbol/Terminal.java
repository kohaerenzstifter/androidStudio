package org.kohaerenzstiftung.xlatius.grammar.symbol;

import org.kohaerenzstiftung.xlatius.grammar.backwardnode.BackwardTerminalNode;
import org.kohaerenzstiftung.xlatius.grammar.general.CompletionState;



public class Terminal extends Symbol {

	private String mValue;

	public Terminal(String value) {
		super(value);
		mValue = value;
	}

	CompletionState consume(CompletionState completionState,
			BackwardTerminalNode backwardTerminalNode) {
		CompletionState result = null;

		backwardTerminalNode.mPreviousBackwardTerminalNode =
				completionState.mLastBackwardTerminalNode;
		backwardTerminalNode.mStartIndex =
				(backwardTerminalNode.mPreviousBackwardTerminalNode == null) ? 0 :
					backwardTerminalNode.mPreviousBackwardTerminalNode.mStartIndex +
					backwardTerminalNode.mPreviousBackwardTerminalNode.mTerminal.
					mValue.length();

		if ((completionState.mHaltCompletions)&&
				(completionState.mRemainder.length() > 0)) {
			result = completionState;
		} else {
			int myLength = mValue.length();
			int otherLength = completionState.mRemainder.length();
			if ((!completionState.mHaltCompletions)&&(otherLength > myLength)) {
				if (completionState.mRemainder.startsWith(mValue)) {

					result = new CompletionState(backwardTerminalNode,
							completionState.mMatch + mValue,
							completionState.mRemainder.substring(myLength),
							false, false);
				}
			} else if ((completionState.mHaltCompletions)||
					(mValue.startsWith(completionState.mRemainder))) {
				result = new CompletionState(backwardTerminalNode,
						completionState.mMatch + completionState.mRemainder,
						mValue.substring(otherLength), true,
						mValue.equals(completionState.mRemainder));
			}
		}

		return result;
	}

	@Override
	boolean isTerminal() {
		return true;
	}
}
