package org.kohaerenzstiftung.xlatius.grammar.backwardnode;

public class BackwardStartRuleNode extends BackwardNode {

	public BackwardNonterminalNode mParent;

	public BackwardStartRuleNode(BackwardNonterminalNode parent) {
		mParent = parent;
	}

	@Override
	public
	boolean startsRule() {
		return true;
	}

}
