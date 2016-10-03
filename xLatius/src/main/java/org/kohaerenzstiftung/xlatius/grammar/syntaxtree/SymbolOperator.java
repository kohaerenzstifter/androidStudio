package org.kohaerenzstiftung.xlatius.grammar.syntaxtree;

import org.kohaerenzstiftung.xlatius.grammar.symbol.Symbol;

public abstract class SymbolOperator {

	public abstract Object operate(int level, Symbol symbol, Object parentData);

}
