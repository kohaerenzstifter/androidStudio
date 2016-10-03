package org.kohaerenzstiftung.xlatius.grammar.backwardnode;

import org.kohaerenzstiftung.xlatius.grammar.symbol.Nonterminal;

public class BackwardNonterminalNode extends BackwardSymbolNode {

	Nonterminal mNonterminal;

	public BackwardNonterminalNode(Nonterminal nonterminal,
			BackwardNode previousRuleTreeNode) {
		super(previousRuleTreeNode, nonterminal);
		mNonterminal = nonterminal;
	}

	@Override
	boolean isTerminal() {
		return false;
	}
}
