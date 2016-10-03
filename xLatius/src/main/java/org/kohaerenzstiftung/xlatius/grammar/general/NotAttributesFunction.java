package org.kohaerenzstiftung.xlatius.grammar.general;


public class NotAttributesFunction extends AttributesFunction {
	private AttributesFunction mNegated;

	public NotAttributesFunction(AttributesFunction negated) {
		mNegated = negated;
	}

	@Override
	public boolean isNot() {
		return true;
	}
}
