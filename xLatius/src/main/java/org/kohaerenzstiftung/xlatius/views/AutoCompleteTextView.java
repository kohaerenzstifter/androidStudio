package org.kohaerenzstiftung.xlatius.views;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kohaerenzstiftung.xlatius.activities.MainActivity;
import org.kohaerenzstiftung.xlatius.grammar.general.CompletionState;
import org.kohaerenzstiftung.xlatius.misc.Helper;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.util.AttributeSet;

@SuppressWarnings("unused")
public class AutoCompleteTextView extends
	org.kohaerenzstiftung.AutoCompleteTextView {

	private MainActivity mMainActivity;
	private AttributeSet mAttrs;
	private Cursor mCursor;
	private int mDefStyle;

	public AutoCompleteTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mMainActivity = (MainActivity) context;
		mAttrs = attrs;
		setAdapter();
	}

	private Cursor doGetCursor(List<CompletionState> completionStates) {
		MatrixCursor result = null;

		result = new MatrixCursor(new String[]{ "_id", "value" });
		int id = 0;

		Map<String, Boolean> cache = new HashMap<String, Boolean>();

		for (CompletionState completionState : completionStates) {
			String row = completionState.mMatch + completionState.mRemainder;
			if (cache.get(row) != null) {
				continue;
			}
			if (completionState.mHaltCompletions) {
				result.addRow(new Object[]{
						id, row });
				id++;
				cache.put(row, true);
			}
		}

		setCursor(result);

		return result;
	}

	private synchronized void setCursor(Cursor cursor) {
		mCursor = cursor;
	}

	@Override
	protected synchronized Cursor getCursor(String string) {
		Cursor result = null;
		List<CompletionState> completionStates =
				Helper.getCompletionStates(string);
		if (completionStates != null) {
			result = doGetCursor(completionStates);
		}
		return result;
	}

	private void closeCursor() {
		if (mCursor != null) {
			if (!mCursor.isClosed()) {
				mCursor.close();
			}
			mCursor = null;
		}
	}

	@Override
	public
	String getColumnName() {
		return "value";
	}
}