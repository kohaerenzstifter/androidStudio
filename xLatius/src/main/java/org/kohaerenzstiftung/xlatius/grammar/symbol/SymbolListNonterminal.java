package org.kohaerenzstiftung.xlatius.grammar.symbol;

import java.util.List;

public class SymbolListNonterminal extends Nonterminal {

	List<List<Symbol>> mRules;

	public void addRule(List<Symbol> rule) {
		mRules.add(rule);
	}

	public SymbolListNonterminal(String name, List<List<Symbol>> rules) {
		super(name);
		mRules = rules;
	}

	protected List<List<Symbol>> unfold() {
		return mRules;
	}

}
