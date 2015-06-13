package com.hiennguyen.gcm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import android.util.Log;

public class JSONParser {
	public JSONParser() {

	}

	public JSONObject performPostCall(String strUrl,
			List<NameValuePair> params, boolean returnJON) throws Exception {
		HttpURLConnection conn = null;
		URL url = new URL(strUrl);
		conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(100000);
		conn.setConnectTimeout(150000);
		conn.setDoInput(true);
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);

		OutputStream os = conn.getOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,
				"UTF-8"));
		writer.write(getQuery(params));
		writer.flush();
		writer.close();

		os.close();

		// conn.connect();

		int responseCode = conn.getResponseCode();

		if (returnJON) {
			String response = "";
			if (responseCode == HttpURLConnection.HTTP_OK) {
				String line;
				BufferedReader br = new BufferedReader(new InputStreamReader(
						conn.getInputStream()));
				while ((line = br.readLine()) != null) {
					response += line;
				}
				Log.e("JSONParser getJSONFromUrl", response);
			} else {
				Log.e("Exception performPostCall",
						"Error from HttpURLConnection, responseCode: "
								+ responseCode);
				response = "";
			}
			JSONObject jObj = new JSONObject(response);
			return jObj;
		} else {
			return null;
		}
		// return null when exception;
	}

	private String getQuery(List<NameValuePair> params)
			throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		boolean first = true;

		for (NameValuePair pair : params) {
			if (first)
				first = false;
			else
				result.append("&");

			result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
		}

		return result.toString();
	}

}
