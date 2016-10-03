package org.kohaerenzstiftung.xlatius.grammar.general;

public class ValueAttributesFunction extends AttributesFunction {
	private String mValue;

	public ValueAttributesFunction(String value) {
		mValue = value;
	}

	@Override
	public boolean isValue() {
		return true;
	}
}
