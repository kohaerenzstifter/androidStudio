package org.kohaerenzstiftung;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by sancho on 25.12.15.
 */
public class NumberPicker extends LinearLayout {
    private NextNumberGetter mNextNumberGetter;

    private final ViewGroup.LayoutParams mLayoutParams = new
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
    private final Context mContext;
    private final Button mDownButton;
    private final TextView mTextView;
    private final Button mUpButton;
    private final OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == mUpButton) {
                setValue(mNextNumberGetter.getNext(mValue, true));
            } else {
                setValue(mNextNumberGetter.getNext(mValue, false));
            }
        }
    };
    private int mValue;

    private Button getButton(String text) {
        Button result = new Button(mContext);
        result.setText(text);
        result.setLayoutParams(mLayoutParams);
        return result;
    }

    public NumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setOrientation(LinearLayout.HORIZONTAL);

        mDownButton = getButton("-");
        mDownButton.setOnClickListener(mOnClickListener);
        addView(mDownButton);

        mTextView = new TextView(mContext);
        mTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        mTextView.setText(Integer.toString(0));
        mTextView.setLayoutParams(mLayoutParams);
        addView(mTextView);

        mUpButton = getButton("+");
        mUpButton.setOnClickListener(mOnClickListener);
        addView(mUpButton);
    }

    public void setValue(int value) {
        mValue = value;
        mTextView.setText(Integer.toString(value));
        checkEnabled();
    }

    public int getValue() {
        return mValue;
    }

    public void setNextNumberGetter(NextNumberGetter nextNumberGetter) {
        mNextNumberGetter = nextNumberGetter;
        checkEnabled();
    }

    private void checkEnabled() {
        if (mNextNumberGetter != null) {
            mUpButton.setEnabled(mNextNumberGetter.getNext(mValue, true) != mValue);
            mDownButton.setEnabled(mNextNumberGetter.getNext(mValue, false) != mValue);
        }
    }
}
