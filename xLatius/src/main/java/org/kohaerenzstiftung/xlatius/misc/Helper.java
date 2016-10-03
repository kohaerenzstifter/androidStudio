package org.kohaerenzstiftung.xlatius.misc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.kohaerenzstiftung.Activity;
import org.kohaerenzstiftung.xlatius.activities.SyntaxActivity;
import org.kohaerenzstiftung.xlatius.grammar.backwardnode.BackwardNonterminalNode;
import org.kohaerenzstiftung.xlatius.grammar.backwardnode.BackwardStartRuleNode;
import org.kohaerenzstiftung.xlatius.grammar.general.CompletionState;
import org.kohaerenzstiftung.xlatius.grammar.symbol.Symbol;
import org.kohaerenzstiftung.xlatius.grammar.symbol.SymbolListNonterminal;
import org.kohaerenzstiftung.xlatius.grammar.symbol.Terminal;

import android.os.Bundle;

public class Helper {

	private static SymbolListNonterminal mStart;
	private static String mLastString;
	private static List<CompletionState> mCompletionStates;

	public static List<CompletionState> getCompletionStates(String string) {
		List<CompletionState> result = null;
		if (mStart != null) {
			result = doGetCompletionStates(string);
		}
		return result;		
	}

	private static List<CompletionState> calculateCompletionStates(
			String string) {
		BackwardNonterminalNode root = new BackwardNonterminalNode(mStart, null);
		BackwardStartRuleNode backwardStartRuleNode =
				new BackwardStartRuleNode(root);
		List<CompletionState> completionStates =
				mStart.consume(new CompletionState(null, "", string,
		false, false), backwardStartRuleNode);

		setCompletionStates(string, completionStates);

		return completionStates;
	}

	private synchronized static void setCompletionStates(String string,
			List<CompletionState> completionStates) {
		mLastString = string;
		mCompletionStates = completionStates;
		
	}

	private synchronized static List<CompletionState> getLastValidCompletionStates(
			String string) {
		List<CompletionState> result = null;

		if ((mLastString != null)&&(mLastString.equals(string))) {
			result = mCompletionStates;
		}

		return result;
	}

	private static List<CompletionState> doGetCompletionStates(
			String string) {
		List<CompletionState> result = null;

		result = getLastValidCompletionStates(string);
		if (result == null) {
			result = calculateCompletionStates(string);
		}
		return result;
	}

	public static void initialise(String[][] grammar) {
		Map<String, SymbolListNonterminal> nonterminals =
				new HashMap<String, SymbolListNonterminal>();
		Map<String, Terminal> terminals =
				new HashMap<String, Terminal>();
		for (String rule[] : grammar) {
			parseRule(rule, nonterminals, terminals);
		}
	}

	private static void parseRule(String[] rule,
			Map<String, SymbolListNonterminal> nonterminals,
			Map<String, Terminal> terminals) {
		SymbolListNonterminal nonterminal = getSymbolListNonterminal(rule[0], nonterminals);
		List<Symbol> symbols = new LinkedList<Symbol>();
		for (int i = 1; i < rule.length; i++) {
			Symbol symbol = null;
			if (rule[i].charAt(0) == '<') {
				symbol = getSymbolListNonterminal(rule[i].substring(1), nonterminals);
			/*} else if (rule[i].charAt(0) == ':') {
				symbol = new AttributesNonterminal(name);*/
			} else {
				symbol = getTerminal(rule[i], terminals);
			}
			symbols.add(symbol);
		}
		nonterminal.addRule(symbols);
	}

	private static Symbol getTerminal(String key,
			Map<String, Terminal> terminals) {
		Terminal result = terminals.get(key);
		if (result == null) {
			result = new Terminal(key);
			terminals.put(key, result);
		}
		return result;
	}

	private static SymbolListNonterminal getSymbolListNonterminal(String key,
			Map<String, SymbolListNonterminal> nonterminals) {
		SymbolListNonterminal result = nonterminals.get(key);
		if (result == null) {
			List<List<Symbol>> rules = new LinkedList<List<Symbol>>();
			result = new SymbolListNonterminal(key, rules);
			nonterminals.put(key, result);
			if (key.equals("S")) {
				mStart = result;
			}
		}
		return result;
	}

	public static List<CompletionState> getCompleteCompletionStates(String string) {
		List<CompletionState> result = null;
		List<CompletionState> completionStates =
				Helper.getCompletionStates(string);
		if (completionStates != null) {
			result = getCompleteCompletionStates(completionStates, string);
		}
		return result;
	}

	private static List<CompletionState> getCompleteCompletionStates(
			List<CompletionState> completionStates, String string) {
		List<CompletionState> result = null;
		if (completionStates != null) {
			result = doGetCompleteCompletionStates(completionStates,
					string);
		}
		return result;
	}

	private static List<CompletionState> doGetCompleteCompletionStates(
			List<CompletionState> completionStates, String string) {
		List<CompletionState> result = null;

		for (CompletionState completionState : completionStates) {
			if (!completionState.mCompletelyValid) {
				continue;
			}
			if (!string.equals(completionState.mMatch)) {
				continue;
			}
			if (result == null) {
				result = new LinkedList<CompletionState>();
			}
			result.add(completionState);
		}
	
		return result;
	}

	public static void startSyntaxActivity(Activity activity,
			String text, int index) {
		Bundle bundle = new Bundle();
		bundle.putString("text", text);
		bundle.putInt("index", index);
		activity.startActivity(SyntaxActivity.class, bundle);
	}
}
