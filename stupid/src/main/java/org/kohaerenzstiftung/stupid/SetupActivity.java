package org.kohaerenzstiftung.stupid;

import java.io.File;
import java.util.LinkedList;

import org.apache.http.message.BasicNameValuePair;
import org.kohaerenzstiftung.AsyncTaskResult;
import org.kohaerenzstiftung.HTTPServerRequest;
import org.kohaerenzstiftung.StandardActivity;
import org.kohaerenzstiftung.stupid.R;
import org.kohaerenzstiftung.stupid.Service;

import android.content.SharedPreferences.Editor;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

public class SetupActivity extends StandardActivity {

	public class MediaRecorderErrorRunnable implements Runnable {

		private int mWhat;
		private int mExtra;

		public MediaRecorderErrorRunnable(int what, int extra) {
			mWhat = what;
			mExtra = extra;
		}

		@Override
		public void run() {
			String errString = (mWhat == MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN) ? "MEDIA_RECORDER_ERROR_UNKNOWN"
					: "MEDIA_ERROR_SERVER_DIED";
			String text = "Error: " + errString + "(" + mExtra + ")";
			Toast.makeText(SetupActivity.this, text, Toast.LENGTH_SHORT).show();

		}

	}

	private HTTPServerRequest mHttpServerRequest;
	private Button mStartStopRecordingButton;
	private boolean mServiceConnected;
	private Handler mHandler = new Handler();
	private EditText mServerPortEditText;
	private CheckBox mSecureCheckBox;
	private EditText mUserEditText;
	private EditText mPasswordEditText;
	private Button mTestConnectionOkButton;
	private String mServerPort = "";
	private boolean mSecure = false;
	private String mUser = "";
	private String mPassword = "";
	private boolean mCanSave;
	private boolean mDisabled = false;

	private OnCheckedChangeListener mCheckedChangedListener = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (!mDisabled) {
				mSecure = isChecked;
				mTestConnectionOkButton.setText(getResources().getString(
						R.string.test_connection));
				mCanSave = false;
			}
		}
	};

	private TextWatcher mTextWatcher = new TextWatcher() {


		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {

		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {

		}

		@Override
		public void afterTextChanged(Editable arg0) {
			if (!mDisabled) {
				mUser = mUserEditText.getText().toString();
				mPassword = mPasswordEditText.getText().toString();
				mServerPort = mServerPortEditText.getText().toString();

				mTestConnectionOkButton.setText(getResources().getString(
						R.string.test_connection));
				mCanSave = false;
			}
		}
	};
	private Runnable mConnectionSuccessfulRunnable = new Runnable() {

		@Override
		public void run() {
			mTestConnectionOkButton.setText(getResources().getString(
					R.string.save));
			mCanSave = true;
			String text = getResources().getString(R.string.ok);
			Toast.makeText(SetupActivity.this, text, Toast.LENGTH_LONG)
			.show();
		}
	};
	private Runnable mConnectionCancelledRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub

		}
	};

	private HTTPServerRequest.Worker mTestConnectionWorker = new HTTPServerRequest.Worker() {

		@Override
		public AsyncTaskResult work() {
			Throwable throwable = null;
			String fingerprint = null;
			AsyncTaskResult result = new AsyncTaskResult();

			try {
				LinkedList<BasicNameValuePair> headers = new LinkedList<BasicNameValuePair>();
				BasicNameValuePair basicNameValuePair = new BasicNameValuePair("action", "test");
				headers.add(basicNameValuePair);

				fingerprint = Helper.performHttp(SetupActivity.this, headers,
						null, mSecure, mServerPort, mUser, mPassword);

			} catch (Throwable t) {
				throwable = t;
			}

			result.setFingerprint(fingerprint);
			result.setThrowable(throwable);

			return result;
		}
	};
	private HTTPServerRequest.ThrowableRunnable mConnectionFailedRunnable =
			new HTTPServerRequest.ThrowableRunnable() {

		private Throwable mThrowable;

		@Override
		public void run() {
			mTestConnectionOkButton.setText(getResources().getString(
					R.string.test_connection));
			String message =
					mThrowable.getClass().getName() + ": " + mThrowable.getMessage();
			Toast.makeText(SetupActivity.this, message, Toast.LENGTH_LONG)
			.show();
		}

		@Override
		public void setThrowable(Throwable throwable) {
			mThrowable = throwable;
		}
	};
	private Button mNewFileButton;

	@Override
	protected int getLayout() {
		return R.layout.activity_setup;
	}

	@Override
	protected void findElements() {
		mUserEditText = (EditText) (EditText) findViewById(R.id.edittext_user);
		mPasswordEditText = (EditText) (EditText) findViewById(R.id.edittext_password);
		mServerPortEditText = (EditText) (EditText) findViewById(R.id.edittext_serverport);
		mStartStopRecordingButton = (Button) findViewById(R.id.button_recording_startstop);
		mNewFileButton = (Button) findViewById(R.id.button_newfile);
		mTestConnectionOkButton = (Button) findViewById(R.id.button_testconnection_ok);
		mSecureCheckBox = (CheckBox) findViewById(R.id.checkbox_secure);
	}

	@Override
	protected void assignHandlers() {
		mStartStopRecordingButton
		.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SetupActivity.this.handleStartStopRecording();
			}
		});
		mTestConnectionOkButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SetupActivity.this.handleTestConnectionOk();
			}
		});
		mNewFileButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SetupActivity.this.handleNewFile();
				
			}
		});

		mUserEditText.addTextChangedListener(mTextWatcher);
		mPasswordEditText.addTextChangedListener(mTextWatcher);
		mServerPortEditText.addTextChangedListener(mTextWatcher);
		mSecureCheckBox.setOnCheckedChangeListener(mCheckedChangedListener);
	}

	protected void handleNewFile() {
		Service service = (Service) mService;

		service.rotateFile();
		String text = getResources().getString(R.string.ok);
		Toast.makeText(SetupActivity.this, text, Toast.LENGTH_LONG).show();
	}

	protected void handleTestConnectionOk() {
		if (mCanSave) {
			Editor editor = PreferenceManager.getDefaultSharedPreferences(this)
					.edit();
			editor.putString("username", mUser);
			editor.putString("password", mPassword);
			editor.putString("serverPort", mServerPort);
			editor.putBoolean("secure", mSecure);
			editor.commit();

			Service service = (Service) mService;
			boolean isRecording = service.isRecording();
			updateRecordingButton(isRecording);
		} else {
			if (mHttpServerRequest == null) {
				String pathToFingerprints =
						getFilesDir().getAbsoluteFile() + File.separator + "fingerprints";
				mHttpServerRequest = new HTTPServerRequest(this,
						mConnectionSuccessfulRunnable, mConnectionCancelledRunnable, 
						mTestConnectionWorker, mConnectionFailedRunnable, pathToFingerprints,
						R.string.ok, R.string.cancel, R.string.server_certificate);
			}
			mHttpServerRequest.execute();
		}
	}

	protected void handleStartStopRecording() {
		Service service = (Service) mService;

		boolean isRecording = service.toggleRecording();

		updateRecordingButton(isRecording);
	}

	private void updateRecordingButton(boolean isRecording) {
		int stringId = isRecording ? R.string.stop_recording
				: R.string.start_recording;
		String string = getResources().getString(stringId);
		mStartStopRecordingButton.setText(string);

		boolean enabled = (mService != null);

		if (!isRecording) {
			enabled = (PreferenceManager.getDefaultSharedPreferences(this)
					.contains("username")
					&& PreferenceManager.getDefaultSharedPreferences(this)
					.contains("password") && PreferenceManager
					.getDefaultSharedPreferences(this).contains("serverPort"));
		}
		mStartStopRecordingButton.setEnabled(enabled);

		if ((enabled)&&(isRecording)) {
			mNewFileButton.setEnabled(true);
		} else {
			mNewFileButton.setEnabled(false);
		}
	}

	public void onMediaRecorderError(int what, int extra) {
		mHandler.post(new MediaRecorderErrorRunnable(what, extra));
	}

	@Override
	protected void initialise() {
		if (PreferenceManager.getDefaultSharedPreferences(this)
				.contains("username")) {
			mUser = PreferenceManager.getDefaultSharedPreferences(this)
					.getString("username", "");
		}
		if (PreferenceManager.getDefaultSharedPreferences(this).contains(
				"password")) {
			mPassword = PreferenceManager.getDefaultSharedPreferences(this)
					.getString("password", "");
		}
		if (PreferenceManager.getDefaultSharedPreferences(this).contains(
				"serverPort")) {
			mServerPort = PreferenceManager.getDefaultSharedPreferences(this)
					.getString("serverPort", "<server>:<port>");
		}
		if (PreferenceManager.getDefaultSharedPreferences(this).contains(
				"secure")) {
			mSecure = PreferenceManager.getDefaultSharedPreferences(this)
					.getBoolean("secure", false);
		}

		mTestConnectionOkButton.setText(getResources().getString(
				R.string.test_connection));
		mCanSave = false;

		boolean isRecording = false;

		if (mService != null) {
			Service service = (Service) mService;
			isRecording = service.isRecording();
		}

		updateRecordingButton(isRecording);
	}

	private synchronized void onServiceConnectionChanged(
			boolean serviceConnected) {
		mServiceConnected = serviceConnected;

		mStartStopRecordingButton.setEnabled(mServiceConnected);

		if (mServiceConnected) {
			Service service = (Service) mService;
			boolean isRecording = service.isRecording();
			updateRecordingButton(isRecording);
		}
	}

	@Override
	protected void recoverResources() {
	}

	@Override
	protected void releaseResources() {
	}

	@Override
	protected void uninitialise() {
	}

	@Override
	protected void onServiceUnbound() {
		onServiceConnectionChanged(false);
	}

	@Override
	protected void onServiceBound() {
		onServiceConnectionChanged(true);
	}

	@Override
	protected Class<?> getServiceToStart() {
		return Service.class;
	}

	@Override
	protected Class<?> getServiceToBind() {
		return Service.class;
	}

	@Override
	protected void readArguments(Bundle extras) {
	}

	@Override
	protected void updateViews() {
		boolean wasDisabled = mDisabled;
		mDisabled = true;
		if (mUser != null) {
			mUserEditText.setText(mUser);
		}
		if (mPassword != null) {
			mPasswordEditText.setText(mPassword);
		}
		if (mServerPort != null) {
			mServerPortEditText.setText(mServerPort);
		}
		mSecureCheckBox.setChecked(mSecure);
		mDisabled = wasDisabled;
	}

}
