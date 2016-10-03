package org.kohaerenzstiftung.xlatius.grammar.symbol;

import java.util.List;

import org.kohaerenzstiftung.xlatius.grammar.general.AttributesFunction;

public class AttributesNonterminal extends Nonterminal {

	AttributesFunction mAttributesFunction;

	public AttributesNonterminal(String name,
			AttributesFunction attributesFunction) {
		super(name);
		mAttributesFunction = attributesFunction;
	}

	@Override
	protected List<List<Symbol>> unfold() {
		// TODO Auto-generated method stub
		return null;
	}

}
