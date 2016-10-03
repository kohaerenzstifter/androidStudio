package org.kohaerenzstiftung.xlatius.grammar.symbol;



public abstract class Symbol {
	public String mName;
	Symbol(String name) {
		mName = name;
	}
	abstract boolean isTerminal();
}
