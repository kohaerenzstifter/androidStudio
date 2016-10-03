package org.kohaerenzstiftung.xlatius.grammar.backwardnode;

import org.kohaerenzstiftung.xlatius.grammar.symbol.Terminal;


public class BackwardTerminalNode extends BackwardSymbolNode {

	public int mStartIndex;
	public Terminal mTerminal;
	public BackwardTerminalNode mPreviousBackwardTerminalNode;

	public BackwardTerminalNode(Terminal terminal, BackwardNode previousRuleTreeNode) {
		super(previousRuleTreeNode, terminal);
		mTerminal = terminal;
	}

	@Override
	boolean isTerminal() {
		return true;
	}
}
