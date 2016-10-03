package org.kohaerenzstiftung.xlatius.grammar.backwardnode;

import org.kohaerenzstiftung.xlatius.grammar.symbol.Symbol;

public abstract class BackwardSymbolNode extends BackwardNode {

	BackwardSymbolNode(BackwardNode previousRuleTreeNode, Symbol symbol) {
		super();
		mPreviousBackwardNode = previousRuleTreeNode;
		mSymbol = symbol;
	}

	public BackwardNode mPreviousBackwardNode;
	public Symbol mSymbol;

	@Override
	public
	boolean startsRule() {
		return false;
	}

	abstract boolean isTerminal();
}
