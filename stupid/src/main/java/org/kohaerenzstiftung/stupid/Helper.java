package org.kohaerenzstiftung.stupid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.FileEntity;
import org.apache.http.message.BasicNameValuePair;
import org.kohaerenzstiftung.FingerprintTrustChecker;
import org.kohaerenzstiftung.HTTP;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class Helper {
	private static final int STANDARD_PORT = 8080;

	private static int getPortNotv6(String serverString) {
		int result = 0;
		int colonIndex = serverString.indexOf(':');
		if (colonIndex == -1) {
			result = STANDARD_PORT;
		} else {
			String portPart = serverString.substring(colonIndex + 1, serverString.length());
			try {
				result = Integer.parseInt(portPart);
			} catch (Throwable t) {
				result = STANDARD_PORT;
			}
		}
		return result;
	}

	private static int getPort(String serverString) {
		int result = 0;
		int bracketIndex = serverString.indexOf(']');
		if (bracketIndex == -1) {
			result = getPortNotv6(serverString);
		} else {
			String substring = serverString.substring(bracketIndex + 1);
			int colonIndex;
			if ((colonIndex = substring.indexOf(':')) == -1) {
				result = STANDARD_PORT;
			} else {
				String portPart = substring.substring(colonIndex + 1);
				try {
					result = Integer.parseInt(portPart);
				} catch (Throwable t) {
					result = STANDARD_PORT;
				}
			}
		}
		return result;
	}

	private static String getServerNotv6(String serverString) {
		String result = null;
		int colonIndex = serverString.indexOf(':');
		if (colonIndex != -1) {
			result = serverString.substring(0, colonIndex);
		} else {
			result = serverString;
		}	
		return result;
	}

	private static String getServer(String serverString) {
		int openBracketIndex = serverString.indexOf('[');
		int closeBracketIndex = serverString.indexOf(']');
		String result = null;
		if ((openBracketIndex == -1)||(closeBracketIndex == -1)) {
			result = getServerNotv6(serverString);
		} else {
			result = serverString.substring(openBracketIndex + 1, closeBracketIndex);
		}
		return result;
	}

	private static ArrayList<String> getFingerprints(Context context) {
		ArrayList<String> result = new ArrayList<String>();
		BufferedReader bufferedReader = null;
		FileReader fileReader = null;

		try {
			File filesDir = context.getFilesDir();
			String path = filesDir.getAbsoluteFile() + File.separator + "fingerprints";
			File file = new File(path);
			if (file.exists()) {
				fileReader = new FileReader(path);
				bufferedReader = new BufferedReader(fileReader);

				String line = null;
				while ((line = bufferedReader.readLine()) != null) {
					result.add(line);
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return result;
	}

	static boolean performHttp(Context context, List<BasicNameValuePair> headers,
			FileEntity fileEntity) {
		boolean result = false;

		String serverPort = PreferenceManager
				.getDefaultSharedPreferences(context).getString("serverPort", null);
		String server = getServer(serverPort);
		String username = PreferenceManager
				.getDefaultSharedPreferences(context).getString("username", null);
		String password = PreferenceManager
				.getDefaultSharedPreferences(context).getString("password", null);
		int port = getPort(serverPort);
		boolean secure = PreferenceManager
				.getDefaultSharedPreferences(context).getBoolean("secure", false);

		try {
			if (performHttp(context, secure, server, port, username, password, headers, fileEntity) == null) {
				result = true;
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		return result;

	}

	static String performHttp(Context context, List<BasicNameValuePair> headers,
			FileEntity fileEntity, boolean secure, String serverPort,
			String username, String password) throws Throwable {
		String result = null;

		String server = getServer(serverPort);
		int port = getPort(serverPort);

		Throwable throwable = null;
		try {
			result = performHttp(context, secure, server, port,
					username, password, headers, fileEntity);
		} catch (Throwable t) {
			throwable = t;
			t.printStackTrace();
		}

		if (throwable != null) {
			throw throwable;
		}

		return result;

	}

	private static String performHttp(Context context, boolean secure, String server,
			int port, String username, String password, List<BasicNameValuePair> headers,
			FileEntity fileEntity) throws Throwable {
		String result = null;
		ArrayList<String> fingerprints = getFingerprints(context);
		FingerprintTrustChecker fingerprintTrustChecker = new FingerprintTrustChecker(fingerprints);
		HttpResponse httpResponse;
		Throwable throwable = null;
		try {
			if (secure) {
				httpResponse = HTTP.doHttps(server, port, "sfm/", username, password, headers, null,
						fingerprintTrustChecker, fileEntity, HTTP.HTTP_PUT);
			} else {
				httpResponse = HTTP.doHttp(server, port, "sfm/", username, password, headers, null,
						fileEntity, HTTP.HTTP_PUT);
			}
			if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new Throwable("(httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK)");
			}
		} catch (Throwable t) {
			result = fingerprintTrustChecker.getFingerprint();
			throwable = t;
		}

		if ((result == null)&&(throwable != null)) {
			throw throwable;
		}
	
		return result;
	}

	public static boolean haveConfig(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).contains("serverPort");
	}

	public static boolean recordingEnabled(Context context) {
		return PreferenceManager
				.getDefaultSharedPreferences(context).getBoolean("is_recording", false);
	}

	public static void setRecordingEnabled(Context context, boolean enabled) {
		Editor editor = PreferenceManager
				.getDefaultSharedPreferences(context).edit();
		editor.putBoolean("is_recording", enabled);
		editor.commit();

		
	}
}
