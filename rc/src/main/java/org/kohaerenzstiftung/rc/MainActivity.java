package org.kohaerenzstiftung.rc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.kohaerenzstiftung.AsyncTaskResult;
import org.kohaerenzstiftung.ContextItemExecutor;
import org.kohaerenzstiftung.ContextMenuCreator;
import org.kohaerenzstiftung.Dialog;
import org.kohaerenzstiftung.HTTP;
import org.kohaerenzstiftung.HTTPServerRequest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


@SuppressWarnings("deprecation")
public class MainActivity extends org.kohaerenzstiftung.MenuActivity {

	public class ShowEditButtonRunnable implements Runnable {

		private Button mButton;

		public void setButton(Button button) {
			mButton = button;
		}

		@Override
		public void run() {
			MainActivity.this.doShowEditButtonDialog(mButton, true);
		}

	}

	public class ServerConfig extends Config {

		private String mServer;

		public ServerConfig(String server) {
			super(-1);
			mServer = server;
		}

		@Override
		public boolean isServerConfig() {
			return true;
		}

		@Override
		public boolean isSplitConfig() {
			return false;
		}

		@Override
		public boolean isInsertButtonConfig() {
			return false;
		}

	}

	public class SetServerDialog extends Dialog {

		private EditText mServerEditText;
		private Button mOkButton;

		public SetServerDialog() {
			super(MainActivity.this, R.layout.dialog_setserver, true);
		}

		@Override
		protected void prepareContextMenu(ContextMenu menu, int position) {

		}

		@Override
		protected void setContextItemExecutors() {

		}

		@Override
		protected void registerForContextMenus() {

		}

		@Override
		protected void updateViews() {
		}

		@Override
		protected void recoverResources() {
		}

		@Override
		protected void releaseResources() {
		}

		@Override
		protected void findElements() {
			mServerEditText = (EditText) findViewById(R.id.edittext_server);
			mOkButton = (Button) findViewById(R.id.button_ok);

		}

		@Override
		protected void assignHandlers() {
			mOkButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					SetServerDialog.this.onOk();
					dismiss();
				}
			});
		}

		protected void onOk() {
			String server = mServerEditText.getText().toString();
			MainActivity.this.setServer(server);

		}

	}

	public class KeysSpinnerAdapter extends BaseAdapter implements
	SpinnerAdapter {

		private LinkedList<String> mKeys;
		public KeysSpinnerAdapter(LinkedList<String> keys) {
			mKeys = keys;
		}

		@Override
		public int getCount() {
			return mKeys.size();
		}

		@Override
		public Object getItem(int position) {
			return mKeys.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			String key = (String) getItem(position);
			LayoutInflater inflater = (LayoutInflater)
					getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View result = inflater.inflate(android.R.layout.simple_spinner_item, null);
			TextView item = ((TextView) result.findViewById(android.R.id.text1));
			item.setText(key);
			return result;
		}

	}

	public class SplitConfig extends Config {

		private boolean mHorizontally;

		public SplitConfig(int index, boolean horizontally) {
			super(index);
			mHorizontally = horizontally;
		}

		@Override
		public boolean isServerConfig() {
			return false;
		}

		@Override
		public boolean isSplitConfig() {
			return true;
		}

		@Override
		public boolean isInsertButtonConfig() {
			return false;
		}

	}
	public class InsertButtonConfig extends Config {

		public InsertButtonConfig(int index) {
			super(index);
		}

		@Override
		public boolean isServerConfig() {
			return false;
		}

		@Override
		public boolean isSplitConfig() {
			return false;
		}

		@Override
		public boolean isInsertButtonConfig() {
			return true;
		}
		
	}

	public class RadioButtonConfig extends ButtonConfig {

		private String mSysCode;
		private int mSocket;
		private int mOnOff;

		public RadioButtonConfig(int index, String text, String sysCode, int socket,
				int onOff) {
			super(index, text);
			mSysCode = sysCode;
			mSocket = socket;
			mOnOff = onOff;
		}

		@Override
		public boolean isInfrared() {
			return false;
		}

	}

	public class InfraredButtonConfig extends ButtonConfig {

		private String mDevice;
		private String mKey;

		public InfraredButtonConfig(int index, String text, String device, String key) {
			super(index, text);
			mDevice = device;
			mKey = key;
		}

		@Override
		public boolean isInfrared() {
			return true;
		}

	}

	public abstract class ButtonConfig extends Config {

		private String mText;

		public ButtonConfig(int index, String text) {
			super(index);
			mText = text;
		}

		@Override
		public boolean isServerConfig() {
			return false;
		}

		@Override
		public boolean isSplitConfig() {
			return false;
		}

		@Override
		public boolean isInsertButtonConfig() {
			return false;
		}

		public abstract boolean isInfrared();

	}

	public abstract class Config {
		protected int mIndex;

		public Config(int index) {
			super();
			this.mIndex = index;
		}

		public abstract boolean isServerConfig();

		public abstract boolean isSplitConfig();

		public abstract boolean isInsertButtonConfig();
	}

	public abstract class ButtonDialog extends Dialog {
		protected Button mButton;
		public ButtonDialog(int layout) {
			super(MainActivity.this, layout, true);
		}
		public void setButton(Button button) {
			mButton = button;
		}
		public void setConfig(ButtonConfig buttonConfig) {
			reset();
		}
		public abstract void reset();
	}

	public class EditRadioDialog extends ButtonDialog {

		private EditText mTextEditText;
		private CheckBox mSyscodeCheckBoxes[];
		private RadioButton mSocketRadioButtons[];
		private CheckBox mOnOffCheckBox;
		private Button mOkButton;

		public EditRadioDialog() {
			super(R.layout.dialog_editradio);

		}

		@Override
		protected void prepareContextMenu(ContextMenu menu, int position) {

		}

		@Override
		protected void setContextItemExecutors() {

		}

		@Override
		protected void registerForContextMenus() {

		}

		@Override
		protected void updateViews() {
		}

		@Override
		protected void recoverResources() {
		}

		@Override
		protected void releaseResources() {
		}

		@Override
		protected void findElements() {
			mTextEditText = (EditText) findViewById(R.id.edittext_text);
			mSyscodeCheckBoxes = new CheckBox[5];
			mSyscodeCheckBoxes[0] = (CheckBox) findViewById(R.id.checkbox_syscode1);
			mSyscodeCheckBoxes[1] = (CheckBox) findViewById(R.id.checkbox_syscode2);
			mSyscodeCheckBoxes[2] = (CheckBox) findViewById(R.id.checkbox_syscode3);
			mSyscodeCheckBoxes[3] = (CheckBox) findViewById(R.id.checkbox_syscode4);
			mSyscodeCheckBoxes[4] = (CheckBox) findViewById(R.id.checkbox_syscode5);
			mSocketRadioButtons = new RadioButton[5];
			mSocketRadioButtons[0] = (RadioButton) findViewById(R.id.radiobutton_socket1);
			mSocketRadioButtons[1] = (RadioButton) findViewById(R.id.radiobutton_socket2);
			mSocketRadioButtons[2] = (RadioButton) findViewById(R.id.radiobutton_socket3);
			mSocketRadioButtons[3] = (RadioButton) findViewById(R.id.radiobutton_socket4);
			mSocketRadioButtons[4] = (RadioButton) findViewById(R.id.radiobutton_socket5);

			mOnOffCheckBox = (CheckBox) findViewById(R.id.checkbox_onOff);
			mOkButton = (Button) findViewById(R.id.button_ok);


		}

		@Override
		protected void assignHandlers() {
			mOkButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					EditRadioDialog.this.onOk();
					dismiss();
				}
			});

		}

		protected void onOk() {
			String text = mTextEditText.getText().toString();
			String sysCode = "";
			for (int i = 0; i < mSyscodeCheckBoxes.length; i++) {
				String append = mSyscodeCheckBoxes[i].isChecked() ? "1" : "0";
				sysCode += append;
			}
			int socket = 0;
			for (int i = 0; i < mSocketRadioButtons.length; i++) {
				if (mSocketRadioButtons[i].isChecked()) {
					socket = i + 1;
					break;
				}
			}
			int onOff = mOnOffCheckBox.isChecked() ? 1 : 0;

			int index = getIndex(mButton);
			MainActivity.this.configureRadioButton(index, text, sysCode, socket, onOff);

		}

		@Override
		public void setConfig(ButtonConfig buttonConfig) {
			super.setConfig(buttonConfig);
			if ((buttonConfig != null)&&(!buttonConfig.isInfrared())) {
				doSetConfig((RadioButtonConfig) buttonConfig);
			}			
		}

		private void doSetConfig(RadioButtonConfig radioButtonConfig) {
			for (int i = 0; i < mSyscodeCheckBoxes.length; i++) {
				mSyscodeCheckBoxes[i].setChecked(radioButtonConfig.mSysCode.charAt(i) != '0');
			}
			mSocketRadioButtons[radioButtonConfig.mSocket - 1].setChecked(true);
			mOnOffCheckBox.setChecked(radioButtonConfig.mOnOff != 0);
			mTextEditText.setText(mButton.getText());
		}

		@Override
		public void reset() {
			for (int i = 0; i < mSyscodeCheckBoxes.length; i++) {
				mSyscodeCheckBoxes[i].setChecked(false);
			}
			for (int i = 0; i < mSocketRadioButtons.length; i++) {
				mSocketRadioButtons[i].setChecked(i == 0);
			}
			mOnOffCheckBox.setChecked(true);
			mTextEditText.setText("");
		}

	}

	@SuppressLint({ "InflateParams", "ViewHolder" })
	public class DevicesSpinnerAdapter extends BaseAdapter implements SpinnerAdapter {


		private Object[] mDevices;

		public DevicesSpinnerAdapter(HashMap<String, LinkedList<String>> devices) {
			mDevices = devices.keySet().toArray();
		}

		@Override
		public int getCount() {
			return mDevices.length;
		}

		@Override
		public Object getItem(int position) {
			return mDevices[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			String device = (String) getItem(position);
			LayoutInflater inflater = (LayoutInflater)
					getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View result = inflater.inflate(android.R.layout.simple_spinner_item, null);
			TextView item = ((TextView) result.findViewById(android.R.id.text1));
			item.setText(device);
			return result;
		}

	}

	public class EditInfraredDialog extends ButtonDialog {

		private EditText mTextEditText;
		private Spinner mDeviceSpinner;
		private Spinner mKeySpinner;
		private Button mOkButton;
		private HashMap<String, LinkedList<String>> mKeys;
		private SpinnerAdapter mDevicesSpinnerAdapter;
		private int mDeviceIndex = -1;
		private int mKeyIndex = -1;
		protected String mLastSelectedDevice = null;

		public EditInfraredDialog(HashMap<String, LinkedList<String>> keys) {
			super(R.layout.dialog_editinfrared);
			mKeys = keys;
		}

		@Override
		protected void prepareContextMenu(ContextMenu menu, int position) {

		}

		@Override
		protected void setContextItemExecutors() {

		}

		@Override
		protected void registerForContextMenus() {

		}

		@Override
		protected void updateViews() {
			mDeviceSpinner.setAdapter(getDevicesSpinnerAdapter());
			if (mDeviceIndex != -1) {
				mDeviceSpinner.setSelection(mDeviceIndex, true);
				mDeviceIndex = -1;
			}
		}

		@Override
		protected void recoverResources() {
		}

		@Override
		protected void releaseResources() {
		}

		@Override
		protected void findElements() {
			mTextEditText = (EditText) findViewById(R.id.edittext_text);
			mDeviceSpinner = (Spinner) findViewById(R.id.spinner_device);
			mKeySpinner = (Spinner) findViewById(R.id.spinner_key);
			mOkButton = (Button) findViewById(R.id.button_ok);
		}

		private SpinnerAdapter getDevicesSpinnerAdapter() {
			if (mDevicesSpinnerAdapter == null) {
				mDevicesSpinnerAdapter = new DevicesSpinnerAdapter(mKeys);
			}
			return mDevicesSpinnerAdapter;
		}

		@Override
		protected void assignHandlers() {
			mOkButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					EditInfraredDialog.this.onOk();
					dismiss();
				}
			});

			mDeviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					String device = (String) mDeviceSpinner.getSelectedItem();
					if ((mLastSelectedDevice == null)||(!device.equals(mLastSelectedDevice))) {
						mLastSelectedDevice = device;
						LinkedList<String> keys = EditInfraredDialog.this.mKeys.get(device);
						KeysSpinnerAdapter keysSpinnerAdapter = new KeysSpinnerAdapter(keys);
						mKeySpinner.setAdapter(keysSpinnerAdapter);
					}
					if (mKeyIndex != -1) {
						mKeySpinner.setSelection(mKeyIndex, true);
						mKeyIndex = -1;
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
				}
			});
			mKeySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					mKeySpinner.getSelectedItemPosition();
					
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					// TODO Auto-generated method stub
					
				}
			});
		}

		protected void onOk() {
			String text = mTextEditText.getText().toString();
			String device = (String) mDeviceSpinner.getSelectedItem();
			String key = (String) mKeySpinner.getSelectedItem();

			int index = getIndex(mButton);
			MainActivity.this.configureInfraredButton(index, text, device, key);

		}

		@Override
		public void setConfig(ButtonConfig buttonConfig) {
			super.setConfig(buttonConfig);
			if ((buttonConfig != null)&&(buttonConfig.isInfrared())) {
				doSetConfig((InfraredButtonConfig) buttonConfig);
			}			
		}

		private void doSetConfig(InfraredButtonConfig infraredButtonConfig) {
			List<String> devices = new LinkedList<String>();
			devices.addAll(mKeys.keySet());
			for (int i = 0; i < devices.size(); i++) {
				if (devices.get(i).equals(infraredButtonConfig.mDevice)) {
					mDeviceIndex = i;
				}
			}
			LinkedList<String> keys = mKeys.get(infraredButtonConfig.mDevice);
			for (int i = 0; i < keys.size(); i++) {
				if (keys.get(i).equals(infraredButtonConfig.mKey)) {
					mKeyIndex = i;
				}
			}
			CharSequence text = mButton.getText();
			mTextEditText.setText(text != null ? text : "");
		}

		@Override
		public void reset() {
			mDeviceSpinner.setSelection(0);
			mKeySpinner.setSelection(0);
			mTextEditText.setText("");
		}


	}

	public class HTTPServerRequestWorker implements HTTPServerRequest.Worker {

		public static final int ACTION_BUTTON = 0;
		public static final int ACTION_KEYS = 1;
		private int mAction;
		private ButtonConfig mButtonConfig;

		@Override
		public AsyncTaskResult work() {
			AsyncTaskResult result = new AsyncTaskResult();
			InputStream inputStream = null;
			String fingerprint = null;
			Throwable throwable = null;
			try {
				List<BasicNameValuePair> headers = null;

				int method;
				if (mAction == ACTION_BUTTON) {
					headers = doHeadersButtonAction();
					method = org.kohaerenzstiftung.HTTP.HTTP_PUT;
				} else {
					method = org.kohaerenzstiftung.HTTP.HTTP_GET;
				}

				HttpResponse httpResponse =
						HTTP.doHttp(mServer, mPort, "infraradio", null, null,
								headers, null, null, method);
				int code = httpResponse.getStatusLine().getStatusCode();
				if (code != HttpStatus.SC_OK) {
					throw new Exception("HTTP Status: " + code);
				}
				if (mAction == ACTION_KEYS) {
					inputStream = httpResponse.getEntity().getContent();
					getKeysFromInputStream(inputStream);
				}
			} catch (Throwable t) {
				throwable = t;
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
					}
				}
			}
			result.setThrowable(throwable);
			result.setFingerprint(fingerprint);

			return result;
		}

		private void getKeysFromInputStream(InputStream inputStream) throws Throwable {
			InputStreamReader inputStreamReader = null;
			BufferedReader bufferedReader = null;
			try {
				inputStreamReader = new InputStreamReader(inputStream);
				bufferedReader = new BufferedReader(inputStreamReader);
				String line;
				StringBuffer stringBuffer = new StringBuffer();
				while ((line = bufferedReader.readLine()) != null) {
					stringBuffer.append(line);
				}
				HashMap<String, LinkedList<String>> keys =
						new HashMap<String, LinkedList<String>>();
				JSONArray devices = (JSONArray) new JSONTokener(stringBuffer.toString()).nextValue();
				for (int i = 0; i < devices.length(); i++) {
					LinkedList<String> value = new LinkedList<String>();
					JSONObject device = devices.getJSONObject(i);
					String name = device.getString("name");
					
					JSONArray codes = device.getJSONArray("codes");
					for (int j = 0; j < codes.length(); j++) {
						value.add(codes.getString(j));
					}
					keys.put(name, value);
				}
				mKeys = keys;
			} finally {
				if (bufferedReader != null) {
					try {
						bufferedReader.close();
					} catch (IOException e) {
					}
				}
				if (inputStreamReader != null) {
					try {
						inputStreamReader.close();
					} catch (IOException e) {
					}
				}
			}
		}

		private List<BasicNameValuePair> doHeadersButtonAction() {
			List<BasicNameValuePair> result = new LinkedList<BasicNameValuePair>();

			String value = mButtonConfig.isInfrared() ? "infrared" : "radio";
			BasicNameValuePair basicNameValuePair =
					new BasicNameValuePair("type", value);
			result.add(basicNameValuePair);
			if (mButtonConfig.isInfrared()) {
				InfraredButtonConfig infraredButtonConfig =
						(InfraredButtonConfig) mButtonConfig;
				basicNameValuePair =
						new BasicNameValuePair("device", infraredButtonConfig.mDevice);
				result.add(basicNameValuePair);
				basicNameValuePair =
						new BasicNameValuePair("key", infraredButtonConfig.mKey);
				result.add(basicNameValuePair);
			} else {
				RadioButtonConfig radioButtonConfig =
						(RadioButtonConfig) mButtonConfig;
				basicNameValuePair =
						new BasicNameValuePair("bitmapString", radioButtonConfig.mSysCode);
				result.add(basicNameValuePair);
				basicNameValuePair =
						new BasicNameValuePair("socketNumber", radioButtonConfig.mSocket + "");
				result.add(basicNameValuePair);
				basicNameValuePair =
						new BasicNameValuePair("onOff", radioButtonConfig.mOnOff + "");
				result.add(basicNameValuePair);
			}
			return result;
		}

		public void setAction(int action) {
			mAction = action;
		}

		public void setButtonConfig(ButtonConfig buttonConfig) {
			mButtonConfig = buttonConfig;
		}

	}

	private Runnable mCancelledRunnable = new Runnable() {
		@Override
		public void run() {
		}
	};
	private Runnable mButtonSuccessfulRunnable = new Runnable() {

		@Override
		public void run() {
		}
	};
	private ShowEditButtonRunnable mShowEditButtonRunnable =
			new ShowEditButtonRunnable();

	private HTTPServerRequest.ThrowableRunnable mFailedRunnable =
			new HTTPServerRequest.ThrowableRunnable() {

		private Throwable mThrowable;

		@Override
		public void run() {
			String message = mThrowable.getMessage();
			String text;
			if (message != null) {
				text = mThrowable.getClass().getName() + ": " + message;
			} else {
				text = mThrowable.getClass().getName();
			}
			Toast.makeText(MainActivity.this,
					text, Toast.LENGTH_LONG).show();	
		}

		@Override
		public void setThrowable(Throwable throwable) {
			mThrowable = throwable;
		}

	};
	private HTTPServerRequestWorker mHTTPServerRequestWorker =
			new HTTPServerRequestWorker();
	private Dialog mSetServerDialog;
	private LinearLayout mLinearLayout;
	protected View mContextMenuView;
	private ContextMenuCreator mLayoutContextMenuCreator;
	private OnLongClickListener mOnLongClickListener;
	private ContextMenuCreator mButtonContextMenuCreator;
	private EditInfraredDialog mEditInfraredDialog;
	private EditRadioDialog mEditRadioDialog;
	private OnClickListener mButtonOnClickListener;

	private HashMap<Button, ButtonConfig> mButtonConfigs = null;
	private LinkedList<Object> mViewsList = null;
	private HashMap<String, LinkedList<String>> mKeys = null;
	private int mPort = -1;
	private String mServer = null;
	private JSONArray mJsonConfigurations = null;
	private ActivityReturner mFileChosenReturner;
	private Intent mChooseFileIntent;

	protected void reset() {
		mButtonConfigs = null;
		mViewsList = null;
		mKeys = null;
		mPort = -1;
		mServer = null;
		mJsonConfigurations = null;

		mLinearLayout.removeAllViews();

		getViewsList().add(mLinearLayout);

		registerForContextMenu(mLinearLayout, mLayoutContextMenuCreator);
		mLinearLayout.setOnLongClickListener(mOnLongClickListener);
	}

	public void doShowEditButtonDialog(Button button, boolean infrared) {
		ButtonDialog buttonDialog = null;
		ButtonConfig buttonConfig = getButtonConfigs().get(button);

		if (infrared) {
			if (mEditInfraredDialog == null) {
				mEditInfraredDialog = new EditInfraredDialog(mKeys);
			}
			buttonDialog = mEditInfraredDialog;
		} else {
			if (mEditRadioDialog == null) {
				mEditRadioDialog = new EditRadioDialog();
			}
			buttonDialog = mEditRadioDialog;
		}

		buttonDialog.setButton(button);
		buttonDialog.setConfig(buttonConfig);
		showDialog(buttonDialog);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		boolean load = true;
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if (intent != null) {
			String action = intent.getAction();
			String type = intent.getType();
			if (Intent.ACTION_SEND.equals(action)) {
				if (type != null) {
					if (type.equals("application/json")) {
						Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
						importFromUri(uri);
						load = false;
					}
				}
			}
		}
		if (load) {
			String path = getConfigurationsPath();
			File file = new File(path);
			if (file.exists()) {
				loadFromFile(file);
			}	
		}
	}

	private void loadFromInputStream(InputStream inputStream) {
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		try {
			inputStreamReader =
					new InputStreamReader(inputStream);
			bufferedReader = new BufferedReader(inputStreamReader);
			String line;
			StringBuffer stringBuffer = new StringBuffer();
			while ((line = bufferedReader.readLine()) != null) {
				stringBuffer.append(line);
			}
			JSONArray jsonConfigurations  =
					(JSONArray) new JSONTokener(stringBuffer.toString()).nextValue();
			reset();
			for (int i = 0; i < jsonConfigurations.length(); i++) {
				JSONObject configuration =
						jsonConfigurations.getJSONObject(i);
				perform(configuration);
			}
		} catch (Throwable t) {
			Toast.makeText(this, t.toString(), Toast.LENGTH_SHORT).show();
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (Throwable t) {
				}
			}
			if (inputStreamReader != null) {
				try {
					inputStreamReader.close();
				} catch (Throwable t) {
				}
			}
		}
	}

	private void loadFromFile(File file) {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
			loadFromInputStream(inputStream);
		} catch (Throwable t) {
			Toast.makeText(this, t.toString(), Toast.LENGTH_SHORT).show();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Throwable t) {
				}
			}
		}
	}

	private void perform(JSONObject configuration) throws JSONException {
		String type = configuration.getString("type");
		if (type.equals("insert_button")) {
			int index = configuration.getInt("index");
			insertButton(index);
		} else if (type.equals("server")) {
			String server = configuration.getString("server");
			setServer(server);
		} else if (type.equals("split")) {
			int index = configuration.getInt("index");
			boolean horizontally = configuration.getBoolean("horizontally");
			split(horizontally, index);
		} else {
			int index = configuration.getInt("index");
			boolean infrared = configuration.getBoolean("infrared");
			String text = configuration.getString("text");
			if (infrared) {
				String device = configuration.getString("device");
				String key = configuration.getString("key");
				configureInfraredButton(index, text, device, key);
			} else {
				int onOff = configuration.getInt("onOff");
				int socket = configuration.getInt("socket");
				String sysCode = configuration.getString("sysCode");
				configureRadioButton(index, text, sysCode, socket, onOff);
			}
		}

	}

	@Override
	protected int getOptionsMenu() {
		return R.menu.menu_options;
	}

	public void setServer(String server) {
		ServerConfig serverConfig =
				new ServerConfig(server);

		logConfiguration(serverConfig);
		doSetServer(server);
	}

	private void doSetServer(String server) {
		try {
			int colonAt = server.indexOf(":");
			int port;
			if (colonAt < 0) {
				port = 8080;
			} else {
				port = Integer.parseInt(server.substring(colonAt + 1));
				server = server.substring(0, colonAt);
			}

			mServer = server;
			mPort = port;	
		} catch (Throwable t) {
			Toast.makeText(this, t.toString(), Toast.LENGTH_SHORT).show();
		}
	}

	public void configureInfraredButton(int index, String text, String device,
			String key) {
		InfraredButtonConfig buttonConfig =
				new InfraredButtonConfig(index, text, device, key);

		logConfiguration(buttonConfig);
		Button button = (Button) getViewsList().get(index);
		configureButton(buttonConfig, button);
	}

	public void configureRadioButton(int index, String text, String sysCode,
			int socket, int onOff) {
		RadioButtonConfig buttonConfig =
				new RadioButtonConfig(index, text, sysCode, socket, onOff);

		logConfiguration(buttonConfig);
		Button button = (Button) getViewsList().get(index);
		configureButton(buttonConfig, button);
	}

	private void logConfiguration(Config config) {
		try {
			JSONObject logMe = new JSONObject();
			if (config.isInsertButtonConfig()) {
				InsertButtonConfig insertButtonConfig =
						(InsertButtonConfig) config;
				logMe.put("type", "insert_button");
				logMe.put("index", insertButtonConfig.mIndex);
			} else if (config.isServerConfig()) {
				ServerConfig serverConfig = (ServerConfig) config;
				logMe.put("type", "server");
				logMe.put("server", serverConfig.mServer);
			} else if (config.isSplitConfig()) {
				SplitConfig splitConfig = (SplitConfig) config;
				logMe.put("type", "split");
				logMe.put("index", splitConfig.mIndex);
				logMe.put("horizontally", splitConfig.mHorizontally);
			} else {
				ButtonConfig buttonConfig = (ButtonConfig) config;
				logMe.put("type", "button");
				logMe.put("index", buttonConfig.mIndex);
				logMe.put("text", buttonConfig.mText);
				logMe.put("infrared", buttonConfig.isInfrared());
				if (buttonConfig.isInfrared()) {
					InfraredButtonConfig infraredButtonConfig =
							(InfraredButtonConfig) buttonConfig;
					logMe.put("device", infraredButtonConfig.mDevice);
					logMe.put("key", infraredButtonConfig.mKey);
				} else {
					RadioButtonConfig radioButtonConfig =
							(RadioButtonConfig) buttonConfig;
					logMe.put("onOff", radioButtonConfig.mOnOff);
					logMe.put("socket", radioButtonConfig.mSocket);
					logMe.put("sysCode", radioButtonConfig.mSysCode);
				}
			}
			logConfiguration(logMe);
		} catch (Throwable t) {
			Toast.makeText(this, t.toString(), Toast.LENGTH_SHORT).show();
		}
	}

	private void logConfiguration(JSONObject logMe) {
		if (mJsonConfigurations == null) {
			mJsonConfigurations = new JSONArray();
		}
		mJsonConfigurations.put(logMe);
	}

	protected void save() {
		if (mJsonConfigurations == null) {
			Toast.makeText(this, R.string.nothing_to_save, Toast.LENGTH_SHORT).show();
		} else {
			doSave(mJsonConfigurations);
		}
	}

	private void doSave(JSONArray jsonConfigurations) {
		String path = getConfigurationsPath();
		save(path, jsonConfigurations);
	}

	private String getConfigurationsPath() {
		File filesDir = getFilesDir();
		String result = filesDir.getAbsolutePath() +
				File.separator + "configurations.json";
		return result;
	}

	private void save(String path, JSONArray jsonConfigurations) {
		File file = new File(path);
		if ((file.exists())&&(!file.delete())) {
			Toast.makeText(this, R.string.error_delete, Toast.LENGTH_SHORT).show();
		} else {
			doSave(path, jsonConfigurations);
		}
	}

	private void doSave(String path, JSONArray jsonConfigurations) {
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;

		try {
			fileWriter = new FileWriter(path);
			bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(jsonConfigurations.toString());
		} catch (Throwable t) {
			Toast.makeText(this, t.toString(), Toast.LENGTH_SHORT).show();
		} finally {
			if (bufferedWriter != null) {
				try {
					bufferedWriter.close();
				} catch (Throwable t) {
				}
			}
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (Throwable t) {
				}
			}
		}
	}

	private void configureButton(ButtonConfig buttonConfig, Button button) {
		getButtonConfigs().put(button, buttonConfig);
		button.setText(buttonConfig.mText);

	}

	private HashMap<Button, ButtonConfig> getButtonConfigs() {
		if (mButtonConfigs == null) {
			mButtonConfigs = new HashMap<Button, MainActivity.ButtonConfig>();
		}
		return mButtonConfigs;
	}

	@Override
	protected void setOptionItemExecutors() {
		setOptionItemExecutor(R.id.menuitem_set_server, new OptionItemExecutor() {
			@Override
			public void execute() {
				MainActivity.this.setServer();

			}
		});
		setOptionItemExecutor(R.id.menuitem_import_settings, new OptionItemExecutor() {
			@Override
			public void execute() {
				MainActivity.this.importSettings();

			}
		});
		setOptionItemExecutor(R.id.menuitem_export_settings, new OptionItemExecutor() {
			@Override
			public void execute() {
				MainActivity.this.exportSettings();

			}
		});
		setOptionItemExecutor(R.id.menuitem_reset, new OptionItemExecutor() {
			@Override
			public void execute() {
				MainActivity.this.reset();
			}
		});
		setOptionItemExecutor(R.id.menuitem_save, new OptionItemExecutor() {
			@Override
			public void execute() {
				MainActivity.this.save();
			}
		});
	}

	protected void exportSettings() {
		if (mJsonConfigurations == null) {
			Toast.makeText(this, R.string.nothing_to_export, Toast.LENGTH_SHORT).show();
		} else {
			doExport(mJsonConfigurations);
		}

	}

	private void doExport(JSONArray jsonConfigurations) {
		String path = getExportPath();
		save(path, jsonConfigurations);

		Intent shareIntent = new Intent();
		shareIntent.setAction(Intent.ACTION_SEND);
		Uri contentUri = FileProvider.getUriForFile(this,
				"org.kohaerenzstiftung.rc.fileprovider", new File(path));
		shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
		shareIntent.setType("application/json");
		startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));
	}

	private String getExportPath() {
		File shareDir = new File(getFilesDir().getAbsolutePath() +
				File.separator + "shared");
		if (!shareDir.exists()) {
			shareDir.mkdir();
		}
		String result = shareDir.getAbsolutePath() +
				File.separator + "export.json";
		return result;
	}

	protected void importSettings() {
		if (mFileChosenReturner == null) {
			mFileChosenReturner = new ActivityReturner(null) {
				
				@Override
				protected void handleError(String message) {
				}

				@Override
				protected void handleResult(Intent intent) {
					Uri uri;
					if ((intent != null)&&((uri = intent.getData()) != null)) {
						MainActivity.this.importFromUri(uri);
					}
				}
			};
		}
		if (mChooseFileIntent == null) {
			mChooseFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
			mChooseFileIntent.setType("application/json");
		}
		startActivityForResult(Intent.createChooser(mChooseFileIntent, "Select JSON"), mFileChosenReturner);
	}

	private void importFromUri(Uri uri) {
		InputStream inputStream = null;
		try {
			inputStream = getContentResolver().openInputStream(uri);
			loadFromInputStream(inputStream);
		} catch (Throwable t) {
			Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected void setServer() {
		org.kohaerenzstiftung.Dialog dialog = getSetServerDialog();
		showDialog(dialog);
	}

	private Dialog getSetServerDialog() {
		if (mSetServerDialog == null) {
			mSetServerDialog = new SetServerDialog();
		}
		return mSetServerDialog;
	}

	private void contextMenuSplit(boolean horizontally, View view) {
		int index = getIndex(view);

		split(horizontally, index);
	}

	private void contextMenuInsertButton(View view) {
		int index = getIndex(view);
		MainActivity.this.insertButton(index);
	}

	private int getIndex(View view) {
		int result = -1;

		LinkedList<Object> viewsList = getViewsList();
		for (int i = 0; i < viewsList.size(); i++) {
			if (viewsList.get(i) == mContextMenuView) {
				result = i;
			}
		}
		return result;
	}

	@Override
	protected void setContextItemExecutors() {
		setContextItemExecutor(R.id.menuitem_split_horizontally, new ContextItemExecutor() {

			@Override
			public void execute(MenuItem item) {
				contextMenuSplit(true, mContextMenuView);
			}
		});
		setContextItemExecutor(R.id.menuitem_split_vertically, new ContextItemExecutor() {

			@Override
			public void execute(MenuItem item) {
				contextMenuSplit(false, mContextMenuView);
			}
		});
		setContextItemExecutor(R.id.menuitem_insert_button, new ContextItemExecutor() {

			@Override
			public void execute(MenuItem item) {
				contextMenuInsertButton(mContextMenuView);
			}

		});
		setContextItemExecutor(R.id.menuitem_edit_infrared, new ContextItemExecutor() {

			@Override
			public void execute(MenuItem item) {
				MainActivity.this.editButton((Button) mContextMenuView, true);
			}
		});
		setContextItemExecutor(R.id.menuitem_edit_radio, new ContextItemExecutor() {

			@Override
			public void execute(MenuItem item) {
				MainActivity.this.editButton((Button) mContextMenuView, false);
			}
		});
		setContextItemExecutor(R.id.menuitem_set_server2, new ContextItemExecutor() {
			@Override
			public void execute(MenuItem item) {
				MainActivity.this.setServer();

			}
		});
		setContextItemExecutor(R.id.menuitem_import_settings2, new ContextItemExecutor() {
			@Override
			public void execute(MenuItem item) {
				MainActivity.this.importSettings();

			}
		});
		setContextItemExecutor(R.id.menuitem_export_settings2, new ContextItemExecutor() {
			@Override
			public void execute(MenuItem item) {
				MainActivity.this.exportSettings();

			}
		});
		setContextItemExecutor(R.id.menuitem_reset2, new ContextItemExecutor() {
			@Override
			public void execute(MenuItem item) {
				MainActivity.this.reset();
			}
		});
		setContextItemExecutor(R.id.menuitem_save2, new ContextItemExecutor() {
			@Override
			public void execute(MenuItem item) {
				MainActivity.this.save();
			}
		});
	}

	protected void editButton(Button button, boolean infrared) {
		showEditButtonDialog(button, infrared);
	}

	private void showEditButtonDialog(Button button, boolean infrared) {
		if ((infrared)&&(mKeys == null)) {
			mShowEditButtonRunnable.setButton(button);

			mHTTPServerRequestWorker.setAction(HTTPServerRequestWorker.ACTION_KEYS);
			mHTTPServerRequestWorker.setButtonConfig(null);

			HTTPServerRequest httpServerRequest = new HTTPServerRequest(this,
					mShowEditButtonRunnable, mCancelledRunnable, 
					mHTTPServerRequestWorker, mFailedRunnable, null,
					R.string.ok, R.string.cancel, R.string.server_certificate);
			httpServerRequest.execute();
		} else {
			doShowEditButtonDialog(button, infrared);
		}
	}


	private void insertButton(int index) {
		logConfiguration(new InsertButtonConfig(index));
		LinearLayout linearLayout = (LinearLayout) getViewsList().get(index);
		insertButton(linearLayout);
	}

	protected void split(boolean horizontally, int index) {

		logConfiguration(new SplitConfig(index, horizontally));
		LinearLayout linearLayout = (LinearLayout) getViewsList().get(index);
		split(linearLayout, horizontally);
	}

	private LinkedList<Object> getViewsList() {
		if (mViewsList == null) {
			mViewsList = new LinkedList<Object>();
		}
		return mViewsList;
	}

	private void split(LinearLayout linearLayout, boolean horizontally) {
		linearLayout.setOrientation(horizontally ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);

		addChild(horizontally, linearLayout);
		addChild(horizontally, linearLayout);
	}

	private void addChild(boolean horizontally, LinearLayout linearLayout) {
		addChild(horizontally, linearLayout, false);
	}

	protected void insertButton(LinearLayout linearLayout) {
		boolean horizontally = linearLayout.getOrientation() == LinearLayout.HORIZONTAL;
		addChild(horizontally, linearLayout, true);
	}

	private void addChild(boolean horizontally, LinearLayout parent, boolean button) {
		View child;
		if (button) {
			child = new Button(this);

			child.setOnClickListener(getOnClickListener());

			registerForContextMenu(child, mButtonContextMenuCreator);
		} else {
			child = new LinearLayout(this);

			GradientDrawable border = new GradientDrawable();
			border.setColor(0xFFFFFFFF); //white background
			border.setStroke(1, 0xFF000000); //black border with full opacity
			if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				child.setBackgroundDrawable(border);
			} else {
				child.setBackground(border);
			}

			registerForContextMenu(child, mLayoutContextMenuCreator);
		}
		getViewsList().add(child);

		int height;
		int width;
		if (horizontally) {
			height = LayoutParams.MATCH_PARENT;
			width = 0;
		} else {
			width = LayoutParams.MATCH_PARENT;
			height = 0;
		}

		child.setLayoutParams(new
				LinearLayout.LayoutParams(width, height, 1));
		unregisterForContextMenu(parent);
		parent.setOnLongClickListener(null);
		child.setOnLongClickListener(mOnLongClickListener);



		parent.addView(child);
	}

	private View.OnClickListener getOnClickListener() {
		if (mButtonOnClickListener == null) {
			mButtonOnClickListener = new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					MainActivity.this.onButtonClick((Button) view);
				}
			};
		}

		return mButtonOnClickListener;
	}

	protected void onButtonClick(Button button) {
		ButtonConfig buttonConfig = getButtonConfigs().get(button);
		if (buttonConfig == null) {
			Toast.makeText(this, getString(R.string.button_not_setup), Toast.LENGTH_SHORT).show();
		} else {
			performButtonAction(buttonConfig);
		}

	}

	private void performButtonAction(ButtonConfig buttonConfig) {
		mHTTPServerRequestWorker.setAction(HTTPServerRequestWorker.ACTION_BUTTON);
		mHTTPServerRequestWorker.setButtonConfig(buttonConfig);

		HTTPServerRequest httpServerRequest = new HTTPServerRequest(this,
				mButtonSuccessfulRunnable, mCancelledRunnable, 
				mHTTPServerRequestWorker, mFailedRunnable, null,
				R.string.ok, R.string.cancel, R.string.server_certificate);
		httpServerRequest.execute();
	}

	@Override
	protected void registerForContextMenus() {
		mLayoutContextMenuCreator = new ContextMenuCreator() {
			@Override
			public int createContextMenu(ContextMenuInfo menuInfo) {
				return R.menu.menu_context;
			}
		};
		registerForContextMenu(mLinearLayout, mLayoutContextMenuCreator);

		mButtonContextMenuCreator = new ContextMenuCreator() {
			@Override
			public int createContextMenu(ContextMenuInfo menuInfo) {
				return R.menu.menu_context_button;
			}
		};
	}

	@Override
	protected void prepareContextMenu(ContextMenu menu, int position) {

	}

	@Override
	protected void assignHandlers() {
		mOnLongClickListener = new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				mContextMenuView = v;
				return false;
			}
		};
		mLinearLayout.setOnLongClickListener(mOnLongClickListener);
	}

	@Override
	protected void findElements() {
		mLinearLayout = (LinearLayout) findViewById(R.id.linearlayout);

		getViewsList().add(mLinearLayout);
	}

	@Override
	protected void readArguments(Bundle extras) {
	}

	@Override
	protected void recoverResources() {
	}

	@Override
	protected void releaseResources() {
	}

	@Override
	protected void updateViews() {
	}

	@Override
	protected int getLayout() {
		return R.layout.activity_main;
	}

	@Override
	protected void initialise() {
	}

	@Override
	protected void uninitialise() {
	}

	@Override
	protected void onServiceUnbound() {
	}

	@Override
	protected void onServiceBound() {
	}

	@Override
	protected Class<?> getServiceToStart() {
		return null;
	}

	@Override
	protected Class<?> getServiceToBind() {
		return null;
	}



}
