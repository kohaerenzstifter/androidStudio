package org.kohaerenzstiftung.xlatius.grammar.symbol;

import java.util.LinkedList;
import java.util.List;

import org.kohaerenzstiftung.xlatius.grammar.backwardnode.BackwardNode;
import org.kohaerenzstiftung.xlatius.grammar.backwardnode.BackwardNonterminalNode;
import org.kohaerenzstiftung.xlatius.grammar.backwardnode.BackwardStartRuleNode;
import org.kohaerenzstiftung.xlatius.grammar.backwardnode.BackwardTerminalNode;
import org.kohaerenzstiftung.xlatius.grammar.general.CompletionState;

public abstract class Nonterminal extends Symbol {
	public Nonterminal(String name) {
		super(name);
	}


	private static List<CompletionState> consumeRule(List<Symbol> rule,
			CompletionState completionState, BackwardNode previousBackwardNode) {
		List<CompletionState> result = new LinkedList<CompletionState>();

		Symbol ruleHead = (rule.size() > 0) ? rule.get(0) : null;
		List<Symbol> ruleTail =
				(ruleHead != null) ? rule.subList(1, rule.size()) : null;

		if (((completionState.mHaltCompletions)&&
				(completionState.mRemainder.length() > 0))||
				(ruleHead == null)) {
			if (ruleHead != null) {
				completionState.mCompletelyValid = false;
			}
			result.add(completionState);
		} else if (ruleHead.isTerminal()) {
			previousBackwardNode =
					new BackwardTerminalNode((Terminal) ruleHead,
							previousBackwardNode);
			completionState =
					((Terminal) ruleHead).consume(completionState,
							(BackwardTerminalNode) previousBackwardNode);
			if (completionState != null) {
				result =
						consumeRule(ruleTail, completionState, previousBackwardNode);
			}
		} else {
			List<List<Symbol>> ruleHeadRules = ((Nonterminal) ruleHead).unfold();

			for (List<Symbol> ruleHeadRule : ruleHeadRules) {
				BackwardNode pbn = new BackwardNonterminalNode((Nonterminal) ruleHead,
								previousBackwardNode);
				BackwardStartRuleNode backwardstartRuleNode =
						new BackwardStartRuleNode((BackwardNonterminalNode)
								pbn);

				List<CompletionState> completionStates =
						consumeRule(ruleHeadRule, completionState, backwardstartRuleNode);
				for (CompletionState cs : completionStates) {
					result.addAll(consumeRule(ruleTail, cs, pbn));
				}
			}
		}

		return result;
	}

	
	protected abstract List<List<Symbol>> unfold();




	public List<CompletionState> consume(CompletionState completionState,
			BackwardNode treeNode) {
		List<CompletionState> result = new LinkedList<CompletionState>();

		List<List<Symbol>> rules = unfold();
		for (List<Symbol> rule : rules) {
			List<CompletionState> css =
					consumeRule(rule, completionState, treeNode);
			for (CompletionState cs : css) {
				if (!((cs.mCompletelyValid||cs.mHaltCompletions))) {
					continue;
				}
				result.add(cs);
			}
		}

		return result;
	}

	@Override
	boolean isTerminal() {
		return false;
	}
}
