package org.kohaerenzstiftung.xlatius.activities;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import org.kohaerenzstiftung.MenuActivity;
import org.kohaerenzstiftung.xlatius.R;
import org.kohaerenzstiftung.xlatius.misc.Helper;
import org.kohaerenzstiftung.xlatius.views.AutoCompleteTextView;

public class MainActivity extends MenuActivity {

	private static final int STATE_REFRESH_AUTOCOMPLETE = 0;
	private static final int STATE_CHECK_IT_OUT = 2;

	private AutoCompleteTextView mSourceAutoCompleteTextView;
	String grammar[][] = {
			{
				"S", "a", "<B"
			},
			{
				"B", "b"
			},
			{
				"S", "<Subjekt,VerbDativ", " ", "<ObjektDativ"
			},
			{
				"S", "<Subjekt,VerbAkkusativ", " ", "<ObjektAkkusativ"
			},
			{
				"Subjekt,VerbDativ", "ich", " ", "<VerbDativ1PsSg"
			},
			{
				"Subjekt,VerbDativ", "du", " ", "<VerbDativ2PsSg"
			},
			{
				"Subjekt,VerbDativ", "<Subjekt3PsSg", " ", "<VerbDativ3PsSg"
			},
			{
				"Subjekt,VerbAkkusativ", "ich", " ", "<VerbAkkusativ1PsSg"
			},
			{
				"Subjekt,VerbAkkusativ", "du", " ", "<VerbAkkusativ2PsSg"
			},
			{
				"Subjekt,VerbAkkusativ", "<Subjekt3PsSg", " ",
				"<VerbAkkusativ3PsSg"
			},
			{
				"ObjektDativ", "dem Mann"
			},
			{
				"ObjektDativ", "der Frau"
			},
			{
				"ObjektDativ", "dem Kind"
			},

			{
				"ObjektAkkusativ", "den Mann"
			},
			{
				"ObjektAkkusativ", "die Frau"
			},
			{
				"ObjektAkkusativ", "das Kind"
			},
			{
				"VerbDativ1PsSg", "helfe"
			},
			{
				"VerbDativ1PsSg", "biete"
			},
			{
				"VerbDativ1PsSg", "gebe"
			},
			{
				"VerbDativ2PsSg", "hilfst"
			},
			{
				"VerbDativ2PsSg", "bietest"
			},
			{
				"VerbDativ2PsSg", "gibst"
			},
			{
				"VerbDativ3PsSg", "hilft"
			},
			{
				"VerbDativ3PsSg", "bietet"
			},
			{
				"VerbDativ3PsSg", "gibt"
			},
			{
				"VerbAkkusativ1PsSg", "liebe"
			},
			{
				"VerbAkkusativ1PsSg", "mag"
			},
			{
				"VerbAkkusativ2PsSg", "liebst"
			},
			{
				"VerbAkkusativ2PsSg", "magst"
			},
			{
				"VerbAkkusativ3PsSg", "liebt"
			},
			{
				"VerbAkkusativ3PsSg", "mag"
			},
			{
				"Subjekt3PsSg", "er"
			},
			{
				"Subjekt3PsSg", "sie"
			},
			{
				"Subjekt3PsSg", "es"
			}
	};
	private Button mButton;
	private int mState = STATE_REFRESH_AUTOCOMPLETE;
	private String mText;

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
	protected void prepareContextMenu(ContextMenu menu, int position) {

	}

	@Override
	protected void assignHandlers() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void findElements() {
		mSourceAutoCompleteTextView =
				(AutoCompleteTextView)
				findViewById(R.id.autocompletetextview_source);
		mSourceAutoCompleteTextView.addTextChangedListener(new TextWatcher() {

			@Override
			public
			void onTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public
			void beforeTextChanged(CharSequence arg0,
					int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public
			void afterTextChanged(Editable arg0) {
				mText = arg0.toString();
				if (Helper.getCompleteCompletionStates(mText)
						!= null) {
					setState(STATE_CHECK_IT_OUT);
				} else {
					setState(STATE_REFRESH_AUTOCOMPLETE);
				}
			}
		});
		mButton = (Button) findViewById(R.id.button1);
		setState(STATE_REFRESH_AUTOCOMPLETE);
		mButton.setOnClickListener(new OnClickListener() {

			@Override
			public
			void onClick(View v) {
				MainActivity.this.handleButton();
			}
		});
	}

	protected void handleButton() {
		if (mState == STATE_REFRESH_AUTOCOMPLETE) {
			mSourceAutoCompleteTextView.refresh();
			mSourceAutoCompleteTextView.showDropDown();
		} else if (mState == STATE_CHECK_IT_OUT) {
			Helper.startSyntaxActivity(this, mText, 0);
		}
	}

	private void setState(int state) {
		mState = state;
		setButtonState();
	}

	private void setButtonState() {
		boolean enabled = true;

		if (mState == STATE_REFRESH_AUTOCOMPLETE) {
			mButton.setText("Autocomplete ...");
		} else {
			mButton.setText("!!! CHECK IT OUT !!!");
		}

		mButton.setEnabled(enabled);
	}

	@Override
	protected void readArguments(Bundle extras) {
		// TODO Auto-generated method stub

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
		return R.layout.activity_main;
	}

	@Override
	protected void initialise() {
		try {
			Helper.initialise(grammar);
		} catch (Throwable t) {
			Toast.makeText(this, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
			t.printStackTrace();
		}

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
