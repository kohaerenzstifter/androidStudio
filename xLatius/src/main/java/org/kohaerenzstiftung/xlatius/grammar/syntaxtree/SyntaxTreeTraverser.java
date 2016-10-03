package org.kohaerenzstiftung.xlatius.grammar.syntaxtree;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.kohaerenzstiftung.xlatius.grammar.backwardnode.*;
import org.kohaerenzstiftung.xlatius.grammar.general.CompletionState;

public class SyntaxTreeTraverser {

	private class ObjectAndLevel {
		public ObjectAndLevel(Object object, int level) {
			mObject = object;
			mLevel = level;
		}
		Object mObject;
		int mLevel;
	}

	private SymbolOperator mSymbolOperator;

	public SyntaxTreeTraverser(SymbolOperator symbolOperator) {
		mSymbolOperator = symbolOperator;
	}

	public List<Object> traverse(CompletionState completionState, Object rootData) {

		Map<BackwardSymbolNode, ObjectAndLevel> hashMap =
				new HashMap<BackwardSymbolNode, ObjectAndLevel>();

		List<Object> result =
				traverse(completionState.mLastBackwardTerminalNode,
				rootData,
				hashMap);

		return result;
	}

	private List<Object> traverse(BackwardTerminalNode btn,
			Object rootData,
			Map<BackwardSymbolNode, ObjectAndLevel> hashMap) {

		List<Object> result = new LinkedList<Object>();

		while (btn != null) {
			ObjectAndLevel objectAndLevel =
					getObjectAndLevel(btn, rootData, hashMap);
			result.add(0, objectAndLevel.mObject);

			BackwardNode bsn = null;
			do {
				bsn = btn.mPreviousBackwardNode;
				btn = btn.mPreviousBackwardTerminalNode;
			} while (bsn == btn);
		};

		return result;

	}

	private ObjectAndLevel getObjectAndLevel(BackwardSymbolNode bsn,
			Object rootData, Map<BackwardSymbolNode, ObjectAndLevel> hashMap) {

		ObjectAndLevel result = hashMap.get(bsn);
		if (result == null) {
			result = doGetObjectAndLevel(bsn, rootData, hashMap);
		}
		return result;
	}

	private ObjectAndLevel doGetObjectAndLevel(BackwardSymbolNode bsn,
			Object rootData, Map<BackwardSymbolNode, ObjectAndLevel> hashMap) {

		ObjectAndLevel result = null;
		Object object = null;
		int level = 0;

		if (bsn.mPreviousBackwardNode == null) {
			//this is the root node
			object = mSymbolOperator.operate(level, bsn.mSymbol, rootData);
			result = new ObjectAndLevel(object, level);
			hashMap.put(bsn, result);
		} else {
			List<BackwardSymbolNode> backwardSymbolNodes =
					new LinkedList<BackwardSymbolNode>();
			BackwardNode bn = bsn;
			do {
				bsn = (BackwardSymbolNode) bn;
				backwardSymbolNodes.add(0, bsn);
				bn = bsn.mPreviousBackwardNode;
			} while (!bn.startsRule());

			BackwardStartRuleNode bsrn = (BackwardStartRuleNode) bn;
			ObjectAndLevel objectAndLevel =
					getObjectAndLevel(bsrn.mParent, rootData, hashMap);
			rootData = objectAndLevel.mObject;
			level = objectAndLevel.mLevel + 1;
			for (BackwardSymbolNode backwardSymbolNode : backwardSymbolNodes) {
				object = mSymbolOperator.operate(level,
						backwardSymbolNode.mSymbol, rootData);
				result = new ObjectAndLevel(object, level);
				hashMap.put(backwardSymbolNode, result);
			}
		}

		return result;
	}
}
