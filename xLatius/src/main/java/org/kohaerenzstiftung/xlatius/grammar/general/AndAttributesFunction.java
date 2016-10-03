package org.kohaerenzstiftung.xlatius.grammar.general;

import java.util.List;

public class AndAttributesFunction extends AttributesFunction {
	private List<AttributesFunction> mAnded;

	public AndAttributesFunction(List<AttributesFunction> anded) {
		mAnded = anded;
	}

	@Override
	public boolean isAnd() {
		return true;
	}
}
