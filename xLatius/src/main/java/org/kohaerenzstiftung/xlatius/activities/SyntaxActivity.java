package org.kohaerenzstiftung.xlatius.activities;

import java.util.List;

import org.kohaerenzstiftung.MenuActivity;
import org.kohaerenzstiftung.xlatius.R;
import org.kohaerenzstiftung.xlatius.grammar.general.CompletionState;
import org.kohaerenzstiftung.xlatius.grammar.symbol.Symbol;
import org.kohaerenzstiftung.xlatius.grammar.syntaxtree.SymbolOperator;
import org.kohaerenzstiftung.xlatius.grammar.syntaxtree.SyntaxTreeTraverser;
import org.kohaerenzstiftung.xlatius.misc.Helper;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

public class SyntaxActivity extends MenuActivity {

	class LinearLayout extends android.widget.LinearLayout {

		private int mMaxLevel;
		private LinearLayout mParent;
		private int mWeight;
		private boolean mHorizontal;
		private LinearLayout.LayoutParams mLayoutParams;

		LinearLayout(Context context, LinearLayout parent,
				int weight, boolean horizontal, int maxLevel) {
			super(context);

			mParent = parent;
			mWeight = weight;
			mHorizontal = horizontal;
			mMaxLevel = maxLevel;

			setOrientation(horizontal ?
					LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);

			if (parent != null) {
				parent.setMaxLevel(maxLevel);

				if (weight > -1) {
					int height;
					int width;

					if (parent.mHorizontal) {
						height = LayoutParams.MATCH_PARENT;
						width = 0;
					} else {
						width = LayoutParams.MATCH_PARENT;
						height = 0;
					}

					mLayoutParams = new
							LinearLayout.LayoutParams(width, height, weight);
					setLayoutParams(mLayoutParams);
				}

				parent.addView(this);
			}

		}

		LinearLayout(Context context, boolean horizontal) {
			this(context, null, -1, horizontal, -1);
		}

		private void setMaxLevel(int maxLevel) {
			if (maxLevel > mMaxLevel) {
				mMaxLevel = maxLevel;
				if (mParent != null) {
					if ((!mParent.mHorizontal)&&(mWeight > -1)) {
						mWeight++;
						mLayoutParams.weight = mWeight;
					}
					mParent.setMaxLevel(maxLevel);
				}
			}
		}

	}

	private android.widget.LinearLayout mLinearLayout;
	private String mText;
	private int mIndex;

	@Override
	protected int getOptionsMenu() {
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
	protected void setOptionItemExecutors() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setContextItemExecutors() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void registerForContextMenus() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void assignHandlers() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void findElements() {
		mLinearLayout =
				(android.widget.LinearLayout)
				findViewById(R.id.linearlayout);
	}

	@Override
	protected void readArguments(Bundle extras) {
		mText = extras.getString("text");
		mIndex = extras.getInt("index");
	}

	@Override
	protected void recoverResources() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void releaseResources() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateViews() {
		// TODO Auto-generated method stub

	}

	@Override
	protected int getLayout() {
		return R.layout.activity_syntax;
	}

	private void addViewToLayout(android.widget.LinearLayout layout,
			View view, int weight) {
		int orientation = layout.getOrientation();
		int height;
		int width;
		if (orientation == LinearLayout.HORIZONTAL) {
			height = LayoutParams.MATCH_PARENT;
			width = 0;
		} else {
			width = LayoutParams.MATCH_PARENT;
			height = 0;
		}
		view.setLayoutParams(new
				LinearLayout.LayoutParams(width, height, weight));
		layout.addView(view);
	}

	@Override
	protected void initialise() {
		List<CompletionState> completionStates =
				Helper.getCompleteCompletionStates(mText);
		boolean haveMore = ((mIndex) < (completionStates.size() - 1));
		mLinearLayout.removeAllViews();

		LinearLayout toplevel = new LinearLayout(this, false);
		addViewToLayout(mLinearLayout, toplevel, 1);

		CompletionState completionState = completionStates.get(mIndex);

		SymbolOperator symbolOperator = new SymbolOperator() {
			@Override
			public
			Object operate(int level, Symbol symbol, Object parentData) {
				return SyntaxActivity.this.operateOnSymbol(level, symbol, parentData);
			}
		};

		SyntaxTreeTraverser syntaxTreeTraverser =
				new SyntaxTreeTraverser(symbolOperator);

		LinearLayout panel = new LinearLayout(this, false);

		LinearLayout rootData = new LinearLayout(this, panel, 0, true, -1);

		syntaxTreeTraverser.traverse(completionState, rootData);

		Button button = new Button(this);
		button.setText("Next");
		button.setEnabled(haveMore);
		button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SyntaxActivity.this.startNext();
			}
		});

		addViewToLayout(toplevel, panel, 9);
		addViewToLayout(toplevel, button, 1);
	}

	protected Object operateOnSymbol(int level, Symbol symbol, Object parentData) {
		LinearLayout parentLayout =
				(LinearLayout) parentData;

		LinearLayout myLayout =
				new LinearLayout(this, parentLayout, 1, false, level);

		Button button = new Button(this);
		button.setText(symbol.mName);
		addViewToLayout(myLayout, button, 1);

		LinearLayout result = new LinearLayout(this, myLayout, 0, true, level);

		return result;
	}

	protected void startNext() {
		Helper.startSyntaxActivity(this, mText, (mIndex + 1));
	}

	@Override
	protected void uninitialise() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onServiceUnbound() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onServiceBound() {
		// TODO Auto-generated method stub

	}

	@Override
	protected Class<?> getServiceToStart() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Class<?> getServiceToBind() {
		// TODO Auto-generated method stub
		return null;
	}

}
