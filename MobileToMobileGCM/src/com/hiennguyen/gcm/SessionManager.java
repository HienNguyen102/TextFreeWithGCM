package com.hiennguyen.gcm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SessionManager {
	SharedPreferences pref;
	Editor editor;
	Context _context;
	int PRIVATE_MODE = 0;
	private static final String PREF_NAME = "LoginInfo";

	// All Shared Preferences Keys

	private static final String IS_REMEMBER = "IsRemember";
	// User name (make variable public to access from outside)
	public static final String KEY_NAME = "username_logged";
	public static final String KEY_PASSWORD = "password_logged";
	public static final String KEY_ID = "id";
	public static final String KEY_GCM_ID = "gcm_id";

	@SuppressLint("CommitPrefEdits")
	public SessionManager(Context context) {
		this._context = context;
		pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}

	public void createLoginSession(String name, String password,
			boolean isRemember, int id, String gcmId) {
		editor.putBoolean(IS_REMEMBER, isRemember);
		editor.putString(KEY_NAME, name);
		editor.putString(KEY_PASSWORD, password);
		editor.putInt(KEY_ID, id);
		editor.putString(KEY_GCM_ID, gcmId);
		editor.commit();
	}

	public void changePassword(String newPass) {
		editor.putString(KEY_PASSWORD, newPass);
		editor.commit();
	}

	public boolean isRememberLogin() {
		return pref.getBoolean(IS_REMEMBER, false);
	}

	// Get stored session data
	public UserData getUserDetails() {
		String nameUserLogged = pref.getString(KEY_NAME, null);
		String passwordUserLogged = pref.getString(KEY_PASSWORD, null);
		int idUserLogged = pref.getInt(KEY_ID, 0);
		String gcmIdUserLogged = pref.getString(KEY_GCM_ID, null);
		return new UserData(idUserLogged, gcmIdUserLogged, nameUserLogged,
				passwordUserLogged);
	}

	public void logoutUser() {
		editor.clear();
		editor.commit();

		// After logout redirect user to Loing Activity
		Intent i = new Intent(_context, LogRegActivity.class);
		// Closing all the Activities
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		// Add new Flag to start new Activity
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		// Staring Login Activity
		_context.startActivity(i);
	}
}
