package org.kohaerenzstiftung.xlatius.grammar.general;

import java.util.LinkedList;
import java.util.List;

public class OrAttributesFunction extends AttributesFunction {
	private List<AttributesFunction> mOred;

	public OrAttributesFunction(LinkedList<AttributesFunction> ored) {
		mOred = ored;
	}

	@Override
	public boolean isOr() {
		return true;
	}
}
